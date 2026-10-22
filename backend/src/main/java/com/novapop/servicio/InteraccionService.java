package com.novapop.servicio;

import com.novapop.entidad.CalificacionContenido;
import com.novapop.entidad.CalificacionContenidoId;
import com.novapop.entidad.Contenido;
import com.novapop.entidad.EntidadAuditable;
import com.novapop.entidad.MiLista;
import com.novapop.entidad.MiListaId;
import com.novapop.repositorio.CalificacionContenidoRepository;
import com.novapop.repositorio.MiListaRepository;
import com.novapop.servicio.CatalogoService.Contexto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/** Acciones del perfil sobre un título: Mi Lista y calificaciones. */
@Service
@RequiredArgsConstructor
public class InteraccionService {

    private final CatalogoService catalogoService;
    private final MiListaRepository miListaRepository;
    private final CalificacionContenidoRepository calificacionRepository;

    @Transactional
    public void agregarAMiLista(Long idCuenta, Long idPerfil, Long idContenido) {
        Contexto ctx = catalogoService.contexto(idCuenta, idPerfil);
        Contenido contenido = catalogoService.verificarDisponible(ctx, idContenido);
        MiLista item = miListaRepository.findById(new MiListaId(ctx.idPerfil(), idContenido)).orElseGet(() -> {
            MiLista nuevo = new MiLista();
            nuevo.setPerfil(ctx.perfil());
            nuevo.setContenido(contenido);
            return nuevo;
        });
        item.setEstadoRegistro(EntidadAuditable.ACTIVO);
        item.setFechaAgregado(OffsetDateTime.now());
        miListaRepository.save(item);
    }

    @Transactional
    public void quitarDeMiLista(Long idCuenta, Long idPerfil, Long idContenido) {
        Contexto ctx = catalogoService.contexto(idCuenta, idPerfil);
        miListaRepository.deleteById(new MiListaId(ctx.idPerfil(), idContenido));
    }

    @Transactional
    public void calificar(Long idCuenta, Long idPerfil, Long idContenido, short valoracion) {
        Contexto ctx = catalogoService.contexto(idCuenta, idPerfil);
        Contenido contenido = catalogoService.verificarDisponible(ctx, idContenido);
        CalificacionContenido c = calificacionRepository.findById(new CalificacionContenidoId(ctx.idPerfil(), idContenido))
                .orElseGet(() -> {
                    CalificacionContenido nueva = new CalificacionContenido();
                    nueva.setPerfil(ctx.perfil());
                    nueva.setContenido(contenido);
                    return nueva;
                });
        c.setEstadoRegistro(EntidadAuditable.ACTIVO);
        c.setValoracion(valoracion);
        c.setFechaCalificacion(OffsetDateTime.now());
        calificacionRepository.save(c);
    }

    @Transactional
    public void quitarCalificacion(Long idCuenta, Long idPerfil, Long idContenido) {
        Contexto ctx = catalogoService.contexto(idCuenta, idPerfil);
        calificacionRepository.deleteById(new CalificacionContenidoId(ctx.idPerfil(), idContenido));
    }
}
