package com.novapop.controlador;

import com.novapop.dto.CatalogoDtos.CalificacionRequest;
import com.novapop.dto.CatalogoDtos.DetalleResponse;
import com.novapop.dto.CatalogoDtos.GeneroDto;
import com.novapop.dto.CatalogoDtos.InicioResponse;
import com.novapop.dto.CatalogoDtos.TarjetaDto;
import com.novapop.entidad.TipoContenido;
import com.novapop.servicio.CatalogoService;
import com.novapop.servicio.InteraccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.novapop.controlador.CuentaActual.CABECERA_PERFIL;
import static com.novapop.controlador.CuentaActual.id;

/** Catálogo visto por un perfil (cabecera X-Perfil-Id). */
@RestController
@RequestMapping("/api/catalogo")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService catalogoService;
    private final InteraccionService interaccionService;

    @GetMapping("/inicio")
    public InicioResponse inicio(@AuthenticationPrincipal Jwt jwt, @RequestHeader(CABECERA_PERFIL) Long idPerfil) {
        return catalogoService.inicio(id(jwt), idPerfil);
    }

    @GetMapping("/generos")
    public List<GeneroDto> generos() {
        return catalogoService.generos();
    }

    @GetMapping("/contenidos")
    public List<TarjetaDto> buscar(@AuthenticationPrincipal Jwt jwt, @RequestHeader(CABECERA_PERFIL) Long idPerfil,
                                   @RequestParam(required = false) TipoContenido tipo,
                                   @RequestParam(required = false) Long genero,
                                   @RequestParam(required = false) String q) {
        return catalogoService.buscar(id(jwt), idPerfil, tipo, genero, q);
    }

    @GetMapping("/contenidos/{idContenido}")
    public DetalleResponse detalle(@AuthenticationPrincipal Jwt jwt, @RequestHeader(CABECERA_PERFIL) Long idPerfil,
                                   @PathVariable Long idContenido) {
        return catalogoService.detalle(id(jwt), idPerfil, idContenido);
    }

    @GetMapping("/mi-lista")
    public List<TarjetaDto> miLista(@AuthenticationPrincipal Jwt jwt, @RequestHeader(CABECERA_PERFIL) Long idPerfil) {
        return catalogoService.miLista(id(jwt), idPerfil);
    }

    @PutMapping("/mi-lista/{idContenido}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void agregarAMiLista(@AuthenticationPrincipal Jwt jwt, @RequestHeader(CABECERA_PERFIL) Long idPerfil,
                                @PathVariable Long idContenido) {
        interaccionService.agregarAMiLista(id(jwt), idPerfil, idContenido);
    }

    @DeleteMapping("/mi-lista/{idContenido}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitarDeMiLista(@AuthenticationPrincipal Jwt jwt, @RequestHeader(CABECERA_PERFIL) Long idPerfil,
                                @PathVariable Long idContenido) {
        interaccionService.quitarDeMiLista(id(jwt), idPerfil, idContenido);
    }

    @PutMapping("/calificaciones/{idContenido}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void calificar(@AuthenticationPrincipal Jwt jwt, @RequestHeader(CABECERA_PERFIL) Long idPerfil,
                          @PathVariable Long idContenido, @Valid @RequestBody CalificacionRequest req) {
        interaccionService.calificar(id(jwt), idPerfil, idContenido, req.valoracion());
    }

    @DeleteMapping("/calificaciones/{idContenido}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitarCalificacion(@AuthenticationPrincipal Jwt jwt, @RequestHeader(CABECERA_PERFIL) Long idPerfil,
                                   @PathVariable Long idContenido) {
        interaccionService.quitarCalificacion(id(jwt), idPerfil, idContenido);
    }
}
