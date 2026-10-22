package com.novapop.dto;

import com.novapop.entidad.PlanSuscripcion;
import com.novapop.entidad.TipoDispositivo;
import com.novapop.entidad.TipoMetodoPago;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Cuenta, suscripción, pagos y dispositivos. */
public final class CuentaDtos {

    private CuentaDtos() {
    }

    public record PaisDto(Long id, String codigo, String nombre) {
    }

    public record IdiomaDto(Long id, String codigo, String nombre) {
    }

    public record PlanDto(Long id, String nombre, BigDecimal precioMensual, int pantallasSimultaneas,
                          boolean permiteDescarga, String calidadMaxima, String resolucionMaxima) {

        public static PlanDto de(PlanSuscripcion p) {
            return new PlanDto(p.getIdPlan(), p.getNombrePlan(), p.getPrecioMensual(), p.getPantallasSimultaneas(),
                    p.getPermiteDescarga(), p.getPerfilCalidadMaxima().getNombreCalidad(),
                    p.getPerfilCalidadMaxima().getResolucion());
        }
    }

    public record SuscripcionDto(Long id, PlanDto plan, LocalDate fechaInicio, String situacion,
                                 boolean renovacionAutomatica) {
    }

    public record CuentaResponse(String correo, PaisDto pais, OffsetDateTime fechaRegistro, String situacion,
                                 SuscripcionDto suscripcion) {
    }

    public record CambioPlanRequest(@NotNull Long idPlan) {
    }

    public record RenovacionRequest(@NotNull Boolean renovacionAutomatica) {
    }

    public record PagoDto(Long id, OffsetDateTime fechaPago, BigDecimal monto, String situacion, String plan,
                          String metodo) {
    }

    public record MetodoPagoDto(Long id, TipoMetodoPago tipo, String numeroEnmascarado, boolean predeterminado) {
    }

    public record MetodoPagoRequest(
            @NotNull TipoMetodoPago tipo,
            @NotBlank @Pattern(regexp = "\\d{6,19}", message = "debe tener entre 6 y 19 dígitos") String numero) {
    }

    public record DispositivoDto(Long id, TipoDispositivo tipo, String nombre, OffsetDateTime ultimaConexion) {
    }

    public record DispositivoRequest(@NotNull TipoDispositivo tipo, @NotBlank @Size(max = 100) String nombre) {
    }
}
