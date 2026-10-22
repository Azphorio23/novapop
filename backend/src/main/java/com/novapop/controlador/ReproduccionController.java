package com.novapop.controlador;

import com.novapop.dto.ReproduccionDtos.IniciarRequest;
import com.novapop.dto.ReproduccionDtos.ProgresoRequest;
import com.novapop.dto.ReproduccionDtos.ReproduccionResponse;
import com.novapop.servicio.ReproduccionService;
import com.novapop.servicio.SubtituloService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

import static com.novapop.controlador.CuentaActual.CABECERA_PERFIL;
import static com.novapop.controlador.CuentaActual.id;

@RestController
@RequiredArgsConstructor
public class ReproduccionController {

    private final ReproduccionService reproduccionService;
    private final SubtituloService subtituloService;

    @PostMapping("/api/reproduccion/iniciar")
    public ReproduccionResponse iniciar(@AuthenticationPrincipal Jwt jwt, @RequestHeader(CABECERA_PERFIL) Long idPerfil,
                                        @Valid @RequestBody IniciarRequest req) {
        return reproduccionService.iniciar(id(jwt), idPerfil, req);
    }

    @PutMapping("/api/reproduccion/{idSesion}/progreso")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void progreso(@AuthenticationPrincipal Jwt jwt, @RequestHeader(CABECERA_PERFIL) Long idPerfil,
                         @PathVariable Long idSesion, @Valid @RequestBody ProgresoRequest req) {
        reproduccionService.registrarProgreso(id(jwt), idPerfil, idSesion, req.segundo(), false);
    }

    @PostMapping("/api/reproduccion/{idSesion}/finalizar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finalizar(@AuthenticationPrincipal Jwt jwt, @RequestHeader(CABECERA_PERFIL) Long idPerfil,
                          @PathVariable Long idSesion, @Valid @RequestBody ProgresoRequest req) {
        reproduccionService.registrarProgreso(id(jwt), idPerfil, idSesion, req.segundo(), true);
    }

    @GetMapping("/api/subtitulos/{idSubtitulo}")
    public ResponseEntity<String> subtitulo(@PathVariable Long idSubtitulo) {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "vtt", java.nio.charset.StandardCharsets.UTF_8))
                .cacheControl(CacheControl.maxAge(Duration.ofHours(12)))
                .body(subtituloService.obtenerVtt(idSubtitulo));
    }
}
