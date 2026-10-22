package com.novapop.dto;

import com.novapop.entidad.TipoMetodoPago;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Registro e inicio de sesión de cuentas. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank @Email String correo,
            @NotBlank String clave) {
    }

    public record RegistroRequest(
            @NotBlank @Email @Size(max = 150) String correo,
            @NotBlank @Size(min = 8, max = 72, message = "debe tener entre 8 y 72 caracteres") String clave,
            @NotNull Long idPais,
            @NotNull Long idPlan,
            @NotBlank @Size(max = 50) String nombrePerfil,
            @NotNull TipoMetodoPago tipoMetodo,
            @NotBlank @Pattern(regexp = "\\d{6,19}", message = "debe tener entre 6 y 19 dígitos") String numeroPago) {
    }

    public record TokenResponse(String token, String correo) {
    }
}
