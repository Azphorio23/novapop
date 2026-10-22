package com.novapop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Reproductor: inicio de sesión de reproducción y reporte de progreso. */
public final class ReproduccionDtos {

    private ReproduccionDtos() {
    }

    public record IniciarRequest(@NotNull Long idContenido, Long idEpisodio, @NotNull Long idDispositivo) {
    }

    public record FuenteDto(String calidad, String resolucion, int bitrateKbps, String url) {
    }

    public record PistaAudioDto(String idioma, String codigo, String tipo, String canal) {
    }

    public record SubtituloDto(Long id, String idioma, String codigo, String url) {
    }

    public record SiguienteDto(Long idEpisodio, String etiqueta, String titulo) {
    }

    public record ReproduccionResponse(
            Long idSesion, Long idContenido, Long idEpisodio, String titulo, String subtitulo,
            int duracionSegundos, int segundoInicial, String calidadMaximaPlan,
            List<FuenteDto> fuentes, List<PistaAudioDto> audios, List<SubtituloDto> subtitulos,
            SiguienteDto siguiente) {
    }

    public record ProgresoRequest(@NotNull @Min(0) Integer segundo) {
    }
}
