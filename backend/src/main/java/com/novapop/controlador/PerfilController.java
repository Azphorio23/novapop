package com.novapop.controlador;

import com.novapop.dto.PerfilDtos.PerfilDto;
import com.novapop.dto.PerfilDtos.PerfilRequest;
import com.novapop.dto.PerfilDtos.PinRequest;
import com.novapop.servicio.PerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.novapop.controlador.CuentaActual.id;

@RestController
@RequestMapping("/api/perfiles")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    @GetMapping
    public List<PerfilDto> listar(@AuthenticationPrincipal Jwt jwt) {
        return perfilService.listar(id(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilDto crear(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PerfilRequest req) {
        return perfilService.crear(id(jwt), req);
    }

    @PutMapping("/{idPerfil}")
    public PerfilDto actualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long idPerfil,
                                @Valid @RequestBody PerfilRequest req) {
        return perfilService.actualizar(id(jwt), idPerfil, req);
    }

    @DeleteMapping("/{idPerfil}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long idPerfil) {
        perfilService.eliminar(id(jwt), idPerfil);
    }

    /** Verifica el PIN de control parental antes de entrar al perfil. */
    @PostMapping("/{idPerfil}/pin")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verificarPin(@AuthenticationPrincipal Jwt jwt, @PathVariable Long idPerfil,
                             @Valid @RequestBody PinRequest req) {
        perfilService.verificarPin(id(jwt), idPerfil, req.pin());
    }
}
