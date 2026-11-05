package com.novapop.servicio;

import com.novapop.auditoria.AuditoriaListener;
import com.novapop.dto.CatalogoDtos.ProgresoDto;
import com.novapop.dto.ReproduccionDtos.FuenteDto;
import com.novapop.dto.ReproduccionDtos.IniciarRequest;
import com.novapop.dto.ReproduccionDtos.PistaAudioDto;
import com.novapop.dto.ReproduccionDtos.ReproduccionResponse;
import com.novapop.dto.ReproduccionDtos.SiguienteDto;
import com.novapop.dto.ReproduccionDtos.SubtituloDto;
import com.novapop.entidad.Contenido;
import com.novapop.entidad.Dispositivo;
import com.novapop.entidad.Episodio;
import com.novapop.entidad.HistorialReproduccion;
import com.novapop.entidad.PlanSuscripcion;
import com.novapop.entidad.SesionReproduccion;
import com.novapop.entidad.Suscripcion;
import com.novapop.entidad.TipoContenido;
import com.novapop.entidad.Video;
import com.novapop.excepcion.NovaPopException;
import com.novapop.repositorio.DispositivoRepository;
import com.novapop.repositorio.EpisodioRepository;
import com.novapop.repositorio.HistorialReproduccionRepository;
import com.novapop.repositorio.PistaAudioRepository;
import com.novapop.repositorio.SesionReproduccionRepository;
import com.novapop.repositorio.SubtituloRepository;
import com.novapop.repositorio.SuscripcionRepository;
import com.novapop.repositorio.VideoCalidadRepository;
import com.novapop.servicio.CatalogoService.Contexto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Sesiones de reproducción: controla pantallas simultáneas, entrega las fuentes de video
 * permitidas por el plan y guarda el progreso en HISTORIAL_REPRODUCCION.
 */
@Service
@RequiredArgsConstructor
public class ReproduccionService {

    private final CatalogoService catalogoService;
    private final SuscripcionRepository suscripcionRepository;
    private final DispositivoRepository dispositivoRepository;
    private final EpisodioRepository episodioRepository;
    private final SesionReproduccionRepository sesionRepository;
    private final HistorialReproduccionRepository historialRepository;
    private final VideoCalidadRepository videoCalidadRepository;
    private final PistaAudioRepository pistaAudioRepository;
    private final SubtituloRepository subtituloRepository;

    @Value("${novapop.reproduccion.minutos-inactividad:2}")
    private long minutosInactividad;

    @Transactional
    public ReproduccionResponse iniciar(Long idCuenta, Long idPerfil, IniciarRequest req) {
        Contexto ctx = catalogoService.contexto(idCuenta, idPerfil);
        Contenido contenido = catalogoService.verificarDisponible(ctx, req.idContenido());
        Suscripcion suscripcion = suscripcionRepository.findActiva(idCuenta)
                .orElseThrow(() -> NovaPopException.prohibido("Necesitas una suscripción activa para reproducir"));
        PlanSuscripcion plan = suscripcion.getPlan();
        Dispositivo dispositivo = dispositivoRepository.findDeCuenta(idCuenta, req.idDispositivo())
                .orElseThrow(() -> NovaPopException.noEncontrado("Dispositivo no registrado en tu cuenta"));

        // Qué se reproduce: la película o un episodio de la serie
        Episodio episodio = null;
        Video video;
        Episodio siguiente = null;
        if (contenido.getTipoContenido() == TipoContenido.PELICULA) {
            video = catalogoService.pelicula(contenido.getIdContenido()).getVideo();
        } else {
            List<Episodio> episodios = episodioRepository.findPorSerie(contenido.getIdContenido());
            if (episodios.isEmpty()) {
                throw NovaPopException.noEncontrado("La serie aún no tiene episodios");
            }
            Long idEpisodio = req.idEpisodio() != null ? req.idEpisodio() : episodioParaContinuar(ctx, contenido, episodios);
            int indice = indiceDe(episodios, idEpisodio);
            if (indice < 0) {
                throw NovaPopException.invalido("El episodio no pertenece a esta serie");
            }
            episodio = episodios.get(indice);
            siguiente = indice + 1 < episodios.size() && episodios.get(indice + 1).getVideo() != null
                    ? episodios.get(indice + 1) : null;
            video = episodio.getVideo();
        }
        if (video == null) {
            throw NovaPopException.conflicto("Este título no se encuentra disponible en NovaPop por el momento");
        }

        // Pantallas simultáneas: al reproducir en un dispositivo se cierra lo que tuviera abierto
        OffsetDateTime ahora = OffsetDateTime.now();
        sesionRepository.findAbiertasDeDispositivo(dispositivo.getIdDispositivo()).forEach(s -> s.setFechaFin(ahora));
        sesionRepository.flush();
        long activas = sesionRepository.findActivasDeCuenta(idCuenta, ahora.minusMinutes(minutosInactividad)).size();
        if (activas >= plan.getPantallasSimultaneas()) {
            throw NovaPopException.conflicto("Tu plan " + plan.getNombrePlan() + " permite " + plan.getPantallasSimultaneas()
                    + " pantalla(s) a la vez y ya están todas en uso. Detén la reproducción en otro dispositivo"
                    + " o mejora tu plan.");
        }

        SesionReproduccion sesion = new SesionReproduccion();
        sesion.setPerfil(ctx.perfil());
        sesion.setDispositivo(dispositivo);
        sesion.setContenido(contenido);
        sesion.setEpisodio(episodio);
        sesion.setFechaInicio(ahora);
        sesion.setDireccionIp(AuditoriaListener.ipActual());
        sesion = sesionRepository.save(sesion);
        dispositivo.setUltimaConexion(ahora);

        int segundoInicial = historial(ctx.idPerfil(), contenido.getIdContenido(), episodio)
                .filter(h -> !h.getCompletado())
                .map(HistorialReproduccion::getSegundoVisto)
                .orElse(0);

        List<FuenteDto> fuentes = videoCalidadRepository
                .findPermitidas(video.getIdVideo(), plan.getPerfilCalidadMaxima().getBitrateKbps()).stream()
                .map(vc -> new FuenteDto(vc.getPerfilCalidad().getNombreCalidad(), vc.getPerfilCalidad().getResolucion(),
                        vc.getPerfilCalidad().getBitrateKbps(), vc.getUrlManifiesto()))
                .toList();
        List<PistaAudioDto> audios = pistaAudioRepository.findPorVideo(video.getIdVideo()).stream()
                .map(p -> new PistaAudioDto(p.getIdioma().getNombreIdioma(), p.getIdioma().getCodigoIso(),
                        p.getTipoPista(), p.getCanalAudio()))
                .toList();
        List<SubtituloDto> subtitulos = subtituloRepository.findPorVideo(video.getIdVideo()).stream()
                .map(s -> new SubtituloDto(s.getIdSubtitulo(), s.getIdioma().getNombreIdioma(),
                        s.getIdioma().getCodigoIso(), "/api/subtitulos/" + s.getIdSubtitulo()))
                .toList();

        return new ReproduccionResponse(sesion.getIdSesion(), contenido.getIdContenido(),
                episodio != null ? episodio.getIdEpisodio() : null, CatalogoService.titulo(contenido),
                episodio != null ? CatalogoService.etiqueta(episodio) : null, video.getDuracionSegundos(), segundoInicial,
                plan.getPerfilCalidadMaxima().getNombreCalidad(), fuentes, audios, subtitulos,
                siguiente != null ? new SiguienteDto(siguiente.getIdEpisodio(), CatalogoService.etiqueta(siguiente),
                        siguiente.getTituloEpisodio()) : null);
    }

    /** Guarda el segundo actual (el reproductor lo llama periódicamente). Si finalizar=true, cierra la sesión. */
    @Transactional
    public void registrarProgreso(Long idCuenta, Long idPerfil, Long idSesion, int segundo, boolean finalizar) {
        Contexto ctx = catalogoService.contexto(idCuenta, idPerfil);
        SesionReproduccion sesion = sesionRepository.findById(idSesion)
                .filter(s -> s.getPerfil().getIdPerfil().equals(ctx.idPerfil()))
                .orElseThrow(() -> NovaPopException.noEncontrado("Sesión de reproducción no encontrada"));
        if (sesion.getFechaFin() != null && !finalizar) {
            throw NovaPopException.conflicto("Esta reproducción se cerró porque se inició otra en el mismo dispositivo");
        }

        Contenido contenido = sesion.getContenido();
        Episodio episodio = sesion.getEpisodio();
        int duracion = episodio != null ? episodio.getVideo().getDuracionSegundos()
                : catalogoService.pelicula(contenido.getIdContenido()).getVideo().getDuracionSegundos();
        int visto = Math.min(segundo, duracion);
        OffsetDateTime ahora = OffsetDateTime.now();

        HistorialReproduccion h = historial(ctx.idPerfil(), contenido.getIdContenido(), episodio).orElseGet(() -> {
            HistorialReproduccion nuevo = new HistorialReproduccion();
            nuevo.setPerfil(ctx.perfil());
            nuevo.setContenido(contenido);
            nuevo.setEpisodio(episodio);
            return nuevo;
        });
        h.setSegundoVisto(visto);
        h.setCompletado(visto >= duracion * CatalogoService.UMBRAL_COMPLETADO);
        h.setUltimaActualizacion(ahora);
        historialRepository.save(h);

        // fecha_modificacion de la sesión funciona como "latido": indica que la pantalla sigue en uso
        sesion.setFechaModificacion(ahora);
        if (finalizar && sesion.getFechaFin() == null) {
            sesion.setFechaFin(ahora);
        }
        sesion.getDispositivo().setUltimaConexion(ahora);
    }

    /** Si no se indica episodio: el que toca según el historial, o el primero de la serie. */
    private Long episodioParaContinuar(Contexto ctx, Contenido serie, List<Episodio> episodios) {
        ProgresoDto progreso = catalogoService.progreso(serie,
                catalogoService.historialDe(ctx.idPerfil(), serie.getIdContenido()));
        if (progreso != null && !progreso.completado()) {
            return progreso.idEpisodio();
        }
        return episodios.stream().filter(e -> e.getVideo() != null).findFirst()
                .orElse(episodios.getFirst()).getIdEpisodio();
    }

    private Optional<HistorialReproduccion> historial(Long idPerfil, Long idContenido, Episodio episodio) {
        return episodio != null
                ? historialRepository.findDeEpisodio(idPerfil, episodio.getIdEpisodio())
                : historialRepository.findDePelicula(idPerfil, idContenido);
    }

    private static int indiceDe(List<Episodio> episodios, Long idEpisodio) {
        for (int i = 0; i < episodios.size(); i++) {
            if (episodios.get(i).getIdEpisodio().equals(idEpisodio)) {
                return i;
            }
        }
        return -1;
    }
}
