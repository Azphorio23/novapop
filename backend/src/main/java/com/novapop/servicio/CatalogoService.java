package com.novapop.servicio;

import com.novapop.dto.CatalogoDtos.ClasificacionDto;
import com.novapop.dto.CatalogoDtos.CreditoDto;
import com.novapop.dto.CatalogoDtos.DetalleResponse;
import com.novapop.dto.CatalogoDtos.EpisodioDto;
import com.novapop.dto.CatalogoDtos.FilaDto;
import com.novapop.dto.CatalogoDtos.GeneroDto;
import com.novapop.dto.CatalogoDtos.InicioResponse;
import com.novapop.dto.CatalogoDtos.ProgresoDto;
import com.novapop.dto.CatalogoDtos.ProximamenteDto;
import com.novapop.dto.CatalogoDtos.SeguirViendoDto;
import com.novapop.dto.CatalogoDtos.TarjetaDto;
import com.novapop.dto.CatalogoDtos.TemporadaDto;
import com.novapop.entidad.CalificacionContenidoId;
import com.novapop.entidad.Coleccion;
import com.novapop.entidad.Contenido;
import com.novapop.entidad.ContenidoGenero;
import com.novapop.entidad.ContenidoPaisId;
import com.novapop.entidad.EntidadAuditable;
import com.novapop.entidad.Episodio;
import com.novapop.entidad.HistorialReproduccion;
import com.novapop.entidad.MiListaId;
import com.novapop.entidad.Pelicula;
import com.novapop.entidad.Perfil;
import com.novapop.entidad.TipoColeccion;
import com.novapop.entidad.Temporada;
import com.novapop.entidad.TipoContenido;
import com.novapop.entidad.Video;
import com.novapop.excepcion.NovaPopException;
import com.novapop.repositorio.CalificacionContenidoRepository;
import com.novapop.repositorio.ColeccionContenidoRepository;
import com.novapop.repositorio.ColeccionRepository;
import com.novapop.repositorio.ContenidoGeneroRepository;
import com.novapop.repositorio.ContenidoPaisRepository;
import com.novapop.repositorio.ContenidoPersonaRepository;
import com.novapop.repositorio.ContenidoProductoraRepository;
import com.novapop.repositorio.ContenidoRepository;
import com.novapop.repositorio.EpisodioRepository;
import com.novapop.repositorio.GeneroRepository;
import com.novapop.repositorio.HistorialReproduccionRepository;
import com.novapop.repositorio.MiListaRepository;
import com.novapop.repositorio.PeliculaRepository;
import com.novapop.repositorio.PistaAudioRepository;
import com.novapop.repositorio.SubtituloRepository;
import com.novapop.repositorio.TemporadaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Catálogo visible para un perfil: solo contenido con licencia vigente en el país de la cuenta
 * y, en perfiles infantiles, con clasificación apta (edad mínima ≤ 7).
 */
@Service
@RequiredArgsConstructor
public class CatalogoService {

    static final short EDAD_MAXIMA_INFANTIL = 7;
    static final short SIN_LIMITE_EDAD = 99;
    /** Fracción del video a partir de la cual se considera "visto". */
    static final double UMBRAL_COMPLETADO = 0.95;
    private static final int MAXIMO_SEGUIR_VIENDO = 10;

    private final PerfilService perfilService;
    private final ContenidoRepository contenidoRepository;
    private final ContenidoGeneroRepository contenidoGeneroRepository;
    private final GeneroRepository generoRepository;
    private final PeliculaRepository peliculaRepository;
    private final TemporadaRepository temporadaRepository;
    private final EpisodioRepository episodioRepository;
    private final ContenidoPersonaRepository contenidoPersonaRepository;
    private final ContenidoProductoraRepository contenidoProductoraRepository;
    private final ContenidoPaisRepository contenidoPaisRepository;
    private final ColeccionRepository coleccionRepository;
    private final ColeccionContenidoRepository coleccionContenidoRepository;
    private final HistorialReproduccionRepository historialRepository;
    private final MiListaRepository miListaRepository;
    private final CalificacionContenidoRepository calificacionRepository;
    private final PistaAudioRepository pistaAudioRepository;
    private final SubtituloRepository subtituloRepository;

    /** Perfil validado + datos que filtran el catálogo. */
    public record Contexto(Perfil perfil, Long idCuenta, Long idPais, short edadMaxima) {

        public Long idPerfil() {
            return perfil.getIdPerfil();
        }
    }

    @Transactional(readOnly = true)
    public Contexto contexto(Long idCuenta, Long idPerfil) {
        Perfil perfil = perfilService.validar(idCuenta, idPerfil);
        short edadMaxima = perfil.getEsInfantil() ? EDAD_MAXIMA_INFANTIL : SIN_LIMITE_EDAD;
        return new Contexto(perfil, idCuenta, perfil.getCuenta().getPais().getIdPais(), edadMaxima);
    }

    /** Devuelve el contenido si el perfil puede verlo; si no, 404. */
    @Transactional(readOnly = true)
    public Contenido verificarDisponible(Contexto ctx, Long idContenido) {
        return disponibles(ctx).stream()
                .filter(c -> c.getIdContenido().equals(idContenido))
                .findFirst()
                .orElseThrow(() -> NovaPopException.noEncontrado("Este título no está disponible en tu país o perfil"));
    }

    // ------------------------------------------------------------------ vitrina de inicio

    @Transactional(readOnly = true)
    public InicioResponse inicio(Long idCuenta, Long idPerfil) {
        Contexto ctx = contexto(idCuenta, idPerfil);
        List<Contenido> disponibles = disponibles(ctx);
        Map<Long, TarjetaDto> tarjetas = tarjetas(disponibles);

        Map<Long, List<Long>> contenidosPorColeccion = coleccionContenidoRepository.findActivosOrdenados().stream()
                .collect(Collectors.groupingBy(cc -> cc.getId().getIdColeccion(), LinkedHashMap::new,
                        Collectors.mapping(cc -> cc.getId().getIdContenido(), Collectors.toList())));

        List<TarjetaDto> destacados = new ArrayList<>();
        List<FilaDto> filas = new ArrayList<>();
        for (Coleccion col : coleccionRepository.findByEstadoRegistroOrderByOrdenVitrina(EntidadAuditable.ACTIVO)) {
            List<TarjetaDto> items = contenidosPorColeccion.getOrDefault(col.getIdColeccion(), List.of()).stream()
                    .map(tarjetas::get).filter(Objects::nonNull).toList();
            if (items.isEmpty()) {
                continue;
            }
            if (col.getTipoColeccion() == TipoColeccion.DESTACADO) {
                destacados.addAll(items);
            } else {
                filas.add(new FilaDto(col.getIdColeccion(), col.getNombreColeccion(), col.getTipoColeccion().name(),
                        col.getBannerUrl(), items));
            }
        }
        if (destacados.isEmpty()) { // p. ej. perfil infantil sin estrenos aptos
            destacados = tarjetas.values().stream().limit(3).toList();
        }

        List<SeguirViendoDto> seguirViendo = seguirViendo(ctx, disponibles, tarjetas);
        List<TarjetaDto> miLista = miListaRepository.findIdsContenido(ctx.idPerfil()).stream()
                .map(tarjetas::get).filter(Objects::nonNull).toList();

        LocalDate hoy = LocalDate.now();
        List<Contenido> futuros = contenidoRepository.findProximamente(ctx.idPais(), hoy, ctx.edadMaxima());
        Map<Long, TarjetaDto> tarjetasFuturas = tarjetas(futuros);
        List<ProximamenteDto> proximamente = futuros.stream()
                .map(c -> new ProximamenteDto(tarjetasFuturas.get(c.getIdContenido()),
                        contenidoPaisRepository.findById(new ContenidoPaisId(c.getIdContenido(), ctx.idPais()))
                                .map(cp -> cp.getFechaDisponibleDesde()).orElse(null)))
                .toList();

        return new InicioResponse(destacados, filas, seguirViendo, miLista, proximamente);
    }

    // ------------------------------------------------------------------ explorar y buscar

    @Transactional(readOnly = true)
    public List<TarjetaDto> buscar(Long idCuenta, Long idPerfil, TipoContenido tipo, Long idGenero, String texto) {
        Contexto ctx = contexto(idCuenta, idPerfil);
        List<Contenido> resultado = disponibles(ctx).stream()
                .filter(c -> tipo == null || c.getTipoContenido() == tipo)
                .toList();
        if (idGenero != null && !resultado.isEmpty()) {
            Set<Long> conGenero = contenidoGeneroRepository.findActivosPorContenidos(ids(resultado)).stream()
                    .filter(cg -> cg.getId().getIdGenero().equals(idGenero))
                    .map(cg -> cg.getId().getIdContenido())
                    .collect(Collectors.toSet());
            resultado = resultado.stream().filter(c -> conGenero.contains(c.getIdContenido())).toList();
        }
        if (texto != null && !texto.isBlank()) {
            String buscado = normalizar(texto);
            resultado = resultado.stream()
                    .filter(c -> normalizar(c.getTituloOriginal()).contains(buscado)
                            || (c.getTituloLocalizado() != null && normalizar(c.getTituloLocalizado()).contains(buscado)))
                    .toList();
        }
        return new ArrayList<>(tarjetas(resultado).values());
    }

    @Transactional(readOnly = true)
    public List<GeneroDto> generos() {
        return generoRepository.findByEstadoRegistroOrderByNombreGenero(EntidadAuditable.ACTIVO).stream()
                .map(g -> new GeneroDto(g.getIdGenero(), g.getNombreGenero()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TarjetaDto> miLista(Long idCuenta, Long idPerfil) {
        Contexto ctx = contexto(idCuenta, idPerfil);
        Map<Long, TarjetaDto> tarjetas = tarjetas(disponibles(ctx));
        return miListaRepository.findIdsContenido(ctx.idPerfil()).stream()
                .map(tarjetas::get).filter(Objects::nonNull).toList();
    }

    // ------------------------------------------------------------------ detalle de un título

    @Transactional(readOnly = true)
    public DetalleResponse detalle(Long idCuenta, Long idPerfil, Long idContenido) {
        Contexto ctx = contexto(idCuenta, idPerfil);
        Contenido c = verificarDisponible(ctx, idContenido);
        List<HistorialReproduccion> historial = historialDe(ctx.idPerfil(), idContenido);

        List<String> generos = contenidoGeneroRepository.findActivosPorContenidos(List.of(idContenido)).stream()
                .map(cg -> cg.getGenero().getNombreGenero()).toList();
        List<CreditoDto> creditos = contenidoPersonaRepository.findCreditos(idContenido).stream()
                .map(cp -> new CreditoDto(cp.getPersona().getNombres() + " " + cp.getPersona().getApellidos(),
                        cp.getRolProduccion().getNombreRolProduccion(), cp.getNombrePersonaje(),
                        cp.getPersona().getFotoUrl()))
                .toList();
        List<String> productoras = contenidoProductoraRepository.findPorContenido(idContenido).stream()
                .map(cp -> cp.getProductora().getNombreProductora()).toList();

        Integer duracionMinutos = null;
        String urlTrailer = null;
        Video videoReferencia = null;
        List<TemporadaDto> temporadas = List.of();
        if (c.getTipoContenido() == TipoContenido.PELICULA) {
            Pelicula p = pelicula(idContenido);
            duracionMinutos = (int) p.getDuracionMinutos();
            urlTrailer = p.getUrlTrailer();
            videoReferencia = p.getVideo();
        } else {
            List<Episodio> episodios = episodioRepository.findPorSerie(idContenido);
            Map<Long, HistorialReproduccion> porEpisodio = historial.stream()
                    .filter(h -> h.getEpisodio() != null)
                    .collect(Collectors.toMap(h -> h.getEpisodio().getIdEpisodio(), h -> h, (a, b) -> a));
            temporadas = temporadaRepository.findActivasPorContenidos(List.of(idContenido)).stream()
                    .map(t -> new TemporadaDto(t.getIdTemporada(), t.getNumeroTemporada(), t.getTituloTemporada(),
                            t.getAnioEstreno(), t.getUrlTrailer(),
                            episodios.stream()
                                    .filter(e -> e.getTemporada().getIdTemporada().equals(t.getIdTemporada()))
                                    .map(e -> episodioDto(e, porEpisodio.get(e.getIdEpisodio())))
                                    .toList()))
                    .toList();
            urlTrailer = temporadas.stream().map(TemporadaDto::urlTrailer).filter(Objects::nonNull).findFirst().orElse(null);
            videoReferencia = episodios.stream().map(Episodio::getVideo).filter(Objects::nonNull).findFirst().orElse(null);
        }
        boolean disponibleParaVer = videoReferencia != null;

        Object[] resumen = calificacionRepository.resumen(idContenido).getFirst();
        double promedio = Math.round(((Number) resumen[0]).doubleValue() * 10) / 10.0;
        long total = ((Number) resumen[1]).longValue();
        Short miCalificacion = calificacionRepository.findById(new CalificacionContenidoId(ctx.idPerfil(), idContenido))
                .filter(EntidadAuditable::estaActivo).map(cc -> cc.getValoracion()).orElse(null);
        boolean enMiLista = miListaRepository.findById(new MiListaId(ctx.idPerfil(), idContenido))
                .filter(EntidadAuditable::estaActivo).isPresent();
        LocalDate disponibleHasta = contenidoPaisRepository.findById(new ContenidoPaisId(idContenido, ctx.idPais()))
                .map(cp -> cp.getFechaDisponibleHasta()).orElse(null);

        List<String> audios = videoReferencia == null ? List.of()
                : pistaAudioRepository.findPorVideo(videoReferencia.getIdVideo()).stream()
                .map(pa -> pa.getIdioma().getNombreIdioma()).distinct().toList();
        List<String> subtitulos = videoReferencia == null ? List.of()
                : subtituloRepository.findPorVideo(videoReferencia.getIdVideo()).stream()
                .map(s -> s.getIdioma().getNombreIdioma()).distinct().toList();

        return new DetalleResponse(c.getIdContenido(), titulo(c), c.getTituloOriginal(), c.getTipoContenido().name(),
                c.getAnioLanzamiento(), c.getSinopsis(), c.getPosterUrl(),
                new ClasificacionDto(c.getClasificacion().getCodigo(), c.getClasificacion().getNombreClasificacion(),
                        c.getClasificacion().getEdadMinima()),
                c.getIdiomaOriginal().getNombreIdioma(), generos, creditos, productoras, duracionMinutos, urlTrailer,
                temporadas, promedio, total, miCalificacion, enMiLista, progreso(c, historial), disponibleHasta,
                audios, subtitulos, disponibleParaVer);
    }

    // ------------------------------------------------------------------ progreso ("Seguir viendo")

    /**
     * Desde dónde retomar un contenido. En series: el último episodio a medias o, si se terminó,
     * el siguiente. Devuelve null si el perfil nunca lo reprodujo.
     */
    ProgresoDto progreso(Contenido c, List<HistorialReproduccion> historialDelContenido) {
        if (historialDelContenido.isEmpty()) {
            return null;
        }
        HistorialReproduccion ultimo = historialDelContenido.getFirst(); // ordenado por última actualización
        if (c.getTipoContenido() == TipoContenido.PELICULA) {
            Video video = pelicula(c.getIdContenido()).getVideo();
            if (video == null) {
                return null;
            }
            int duracion = video.getDuracionSegundos();
            return new ProgresoDto(null, null, ultimo.getCompletado() ? 0 : ultimo.getSegundoVisto(),
                    porcentaje(ultimo.getSegundoVisto(), duracion, ultimo.getCompletado()), ultimo.getCompletado());
        }
        List<Episodio> episodios = episodioRepository.findPorSerie(c.getIdContenido());
        int indice = -1;
        for (int i = 0; i < episodios.size(); i++) {
            if (episodios.get(i).getIdEpisodio().equals(ultimo.getEpisodio().getIdEpisodio())) {
                indice = i;
            }
        }
        if (indice < 0) {
            return null;
        }
        Episodio ep = episodios.get(indice);
        if (!ultimo.getCompletado()) {
            return new ProgresoDto(ep.getIdEpisodio(), etiqueta(ep), ultimo.getSegundoVisto(),
                    porcentaje(ultimo.getSegundoVisto(), duracionSegundos(ep), false), false);
        }
        if (indice + 1 < episodios.size()) {
            Episodio siguiente = episodios.get(indice + 1);
            return new ProgresoDto(siguiente.getIdEpisodio(), etiqueta(siguiente), 0, 0, false);
        }
        return new ProgresoDto(ep.getIdEpisodio(), etiqueta(ep), 0, 100, true); // serie terminada
    }

    private List<SeguirViendoDto> seguirViendo(Contexto ctx, List<Contenido> disponibles, Map<Long, TarjetaDto> tarjetas) {
        Map<Long, Contenido> porId = disponibles.stream()
                .collect(Collectors.toMap(Contenido::getIdContenido, c -> c));
        Map<Long, List<HistorialReproduccion>> porContenido = historialRepository.findPorPerfil(ctx.idPerfil()).stream()
                .collect(Collectors.groupingBy(h -> h.getContenido().getIdContenido(), LinkedHashMap::new,
                        Collectors.toList()));
        List<SeguirViendoDto> resultado = new ArrayList<>();
        for (var entrada : porContenido.entrySet()) {
            Contenido c = porId.get(entrada.getKey());
            if (c == null) {
                continue;
            }
            ProgresoDto p = progreso(c, entrada.getValue());
            boolean empezado = p != null && !p.completado() && (p.segundo() > 0 || p.idEpisodio() != null);
            if (empezado) {
                resultado.add(new SeguirViendoDto(tarjetas.get(c.getIdContenido()), p.idEpisodio(), p.etiqueta(),
                        p.segundo(), p.porcentaje()));
            }
            if (resultado.size() == MAXIMO_SEGUIR_VIENDO) {
                break;
            }
        }
        return resultado;
    }

    List<HistorialReproduccion> historialDe(Long idPerfil, Long idContenido) {
        return historialRepository.findPorPerfil(idPerfil).stream()
                .filter(h -> h.getContenido().getIdContenido().equals(idContenido))
                .toList();
    }

    // ------------------------------------------------------------------ auxiliares

    private List<Contenido> disponibles(Contexto ctx) {
        return contenidoRepository.findDisponibles(ctx.idPais(), LocalDate.now(), ctx.edadMaxima());
    }

    Pelicula pelicula(Long idContenido) {
        return peliculaRepository.findConVideo(List.of(idContenido)).stream().findFirst()
                .orElseThrow(() -> NovaPopException.noEncontrado("La película no tiene video asociado"));
    }

    /** Arma las tarjetas manteniendo el orden de la lista recibida. */
    private Map<Long, TarjetaDto> tarjetas(List<Contenido> contenidos) {
        if (contenidos.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = ids(contenidos);
        Map<Long, String> generoPrincipal = new HashMap<>();
        for (ContenidoGenero cg : contenidoGeneroRepository.findActivosPorContenidos(ids)) {
            generoPrincipal.putIfAbsent(cg.getId().getIdContenido(), cg.getGenero().getNombreGenero());
        }
        Map<Long, Pelicula> peliculas = peliculaRepository.findConVideo(ids).stream()
                .collect(Collectors.toMap(Pelicula::getIdContenido, p -> p));
        Map<Long, String> trailerDeSerie = new HashMap<>();
        for (Temporada t : temporadaRepository.findActivasPorContenidos(ids)) {
            if (t.getUrlTrailer() != null) {
                trailerDeSerie.putIfAbsent(t.getContenido().getIdContenido(), t.getUrlTrailer());
            }
        }

        Map<Long, TarjetaDto> resultado = new LinkedHashMap<>();
        for (Contenido c : contenidos) {
            Long id = c.getIdContenido();
            Integer totalEpisodios = null;
            Integer duracion = null;
            boolean soloTrailer;
            String trailer;
            if (c.getTipoContenido() == TipoContenido.SERIE) {
                List<Episodio> episodios = episodioRepository.findPorSerie(id);
                totalEpisodios = episodios.size();
                soloTrailer = episodios.stream().noneMatch(e -> e.getVideo() != null);
                trailer = trailerDeSerie.get(id);
            } else {
                Pelicula p = peliculas.get(id);
                duracion = p != null ? (int) p.getDuracionMinutos() : null;
                soloTrailer = p == null || p.getVideo() == null;
                trailer = p != null ? p.getUrlTrailer() : null;
            }
            resultado.put(id, new TarjetaDto(id, titulo(c), c.getTipoContenido().name(), c.getAnioLanzamiento(),
                    c.getClasificacion().getCodigo(), c.getPosterUrl(), generoPrincipal.get(id), c.getSinopsis(),
                    duracion, totalEpisodios, soloTrailer, trailer));
        }
        return resultado;
    }

    private EpisodioDto episodioDto(Episodio e, HistorialReproduccion h) {
        boolean completado = h != null && h.getCompletado();
        int pct = h == null ? 0 : porcentaje(h.getSegundoVisto(), duracionSegundos(e), completado);
        return new EpisodioDto(e.getIdEpisodio(), e.getNumeroEpisodio(), e.getTituloEpisodio(), e.getDuracionMinutos(),
                pct, completado, e.getVideo() != null);
    }

    private static int duracionSegundos(Episodio e) {
        return e.getVideo() != null ? e.getVideo().getDuracionSegundos() : e.getDuracionMinutos() * 60;
    }

    static String etiqueta(Episodio e) {
        return "T" + e.getTemporada().getNumeroTemporada() + ":E" + e.getNumeroEpisodio() + " · " + e.getTituloEpisodio();
    }

    static String titulo(Contenido c) {
        return c.getTituloLocalizado() != null ? c.getTituloLocalizado() : c.getTituloOriginal();
    }

    private static int porcentaje(int segundo, int duracion, boolean completado) {
        if (completado) {
            return 100;
        }
        return duracion <= 0 ? 0 : Math.min(100, (int) Math.round(segundo * 100.0 / duracion));
    }

    private static List<Long> ids(List<Contenido> contenidos) {
        return contenidos.stream().map(Contenido::getIdContenido).toList();
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
