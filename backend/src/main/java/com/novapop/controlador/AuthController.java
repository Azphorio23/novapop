package com.novapop.controlador;

import com.novapop.dto.AuthDtos.LoginRequest;
import com.novapop.dto.AuthDtos.RegistroRequest;
import com.novapop.dto.AuthDtos.TokenResponse;
import com.novapop.dto.CuentaDtos.IdiomaDto;
import com.novapop.dto.CuentaDtos.PaisDto;
import com.novapop.dto.CuentaDtos.PlanDto;
import com.novapop.entidad.EntidadAuditable;
import com.novapop.repositorio.ContenidoRepository;
import com.novapop.repositorio.IdiomaRepository;
import com.novapop.repositorio.PaisRepository;
import com.novapop.repositorio.PlanSuscripcionRepository;
import com.novapop.servicio.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoints públicos: registro, login y catálogos para el formulario de registro. */
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PaisRepository paisRepository;
    private final IdiomaRepository idiomaRepository;
    private final PlanSuscripcionRepository planRepository;
    private final ContenidoRepository contenidoRepository;

    @PostMapping("/api/auth/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse registrar(@Valid @RequestBody RegistroRequest req) {
        return authService.registrar(req);
    }

    @PostMapping("/api/auth/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/api/publico/paises")
    public List<PaisDto> paises() {
        return paisRepository.findByEstadoRegistroOrderByNombrePais(EntidadAuditable.ACTIVO).stream()
                .map(p -> new PaisDto(p.getIdPais(), p.getCodigoIso(), p.getNombrePais()))
                .toList();
    }

    @GetMapping("/api/publico/idiomas")
    public List<IdiomaDto> idiomas() {
        return idiomaRepository.findByEstadoRegistroOrderByNombreIdioma(EntidadAuditable.ACTIVO).stream()
                .map(i -> new IdiomaDto(i.getIdIdioma(), i.getCodigoIso(), i.getNombreIdioma()))
                .toList();
    }

    @GetMapping("/api/publico/planes")
    public List<PlanDto> planes() {
        return planRepository.findActivos().stream().map(PlanDto::de).toList();
    }

    public record PosterDto(String titulo, String posterUrl) {
    }

    /** Imágenes para el mosaico decorativo de la página de bienvenida. */
    @GetMapping("/api/publico/posters")
    public List<PosterDto> posters() {
        return contenidoRepository.findAll().stream()
                .filter(c -> c.estaActivo() && c.getPosterUrl() != null)
                .map(c -> new PosterDto(c.getTituloOriginal(), c.getPosterUrl()))
                .toList();
    }
}
