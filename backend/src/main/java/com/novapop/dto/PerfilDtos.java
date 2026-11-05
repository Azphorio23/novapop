package com.novapop.dto;

import com.novapop.entidad.Perfil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Perfiles de una cuenta. */
public final class PerfilDtos {

    private PerfilDtos() {
    }

    public record PerfilDto(Long id, String nombre, String avatar, boolean esInfantil, boolean tienePin,
                            Long idIdiomaPreferido, String idiomaPreferido) {

        public static PerfilDto de(Perfil p) {
            return new PerfilDto(p.getIdPerfil(), p.getNombrePerfil(), p.getAvatarUrl(), p.getEsInfantil(),
                    p.getPinControlParental() != null, p.getIdiomaPreferido().getIdIdioma(),
                    p.getIdiomaPreferido().getNombreIdioma());
        }
    }

    /**
     * Crear o editar un perfil.
     * pin: null = no cambia, "" = quitar PIN, 4 dígitos = nuevo PIN.
     */
    public record PerfilRequest(
            @NotBlank @Size(max = 50) String nombre,
            @Size(max = 500) String avatar,
            boolean esInfantil,
            @Pattern(regexp = "^$|\\d{4}", message = "debe tener 4 dígitos") String pin,
            Long idIdiomaPreferido) {
    }

    public record PinRequest(@NotBlank String pin) {
    }
}
