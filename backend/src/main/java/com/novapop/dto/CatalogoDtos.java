package com.novapop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/** Catálogo, vitrina, detalle, Mi Lista y calificaciones. */
public final class CatalogoDtos {

    private CatalogoDtos() {
    }

    /** Lo que se muestra en cada tarjeta de las filas del catálogo. */
    public record TarjetaDto(Long idContenido, String titulo, String tipo, Short anio, String clasificacion,
                             String posterUrl, String generoPrincipal, String sinopsis, Integer duracionMinutos,
                             Integer totalEpisodios, boolean soloTrailer, String urlTrailer) {
    }

    public record FilaDto(Long idColeccion, String nombre, String tipo, String bannerUrl, List<TarjetaDto> contenidos) {
    }

    public record SeguirViendoDto(TarjetaDto contenido, Long idEpisodio, String etiquetaEpisodio, int segundo,
                                  int porcentaje) {
    }

    public record ProximamenteDto(TarjetaDto contenido, LocalDate disponibleDesde) {
    }

    public record InicioResponse(List<TarjetaDto> destacados, List<FilaDto> filas, List<SeguirViendoDto> seguirViendo,
                                 List<TarjetaDto> miLista, List<ProximamenteDto> proximamente) {
    }

    public record GeneroDto(Long id, String nombre) {
    }

    public record ClasificacionDto(String codigo, String nombre, int edadMinima) {
    }

    public record CreditoDto(String nombre, String rol, String personaje, String fotoUrl) {
    }

    public record EpisodioDto(Long id, int numero, String titulo, int duracionMinutos, int porcentajeVisto,
                              boolean completado, boolean disponible) {
    }

    public record TemporadaDto(Long id, int numero, String titulo, Short anio, String urlTrailer,
                               List<EpisodioDto> episodios) {
    }

    /** Punto desde el que el botón "Reproducir/Continuar" retoma el contenido. */
    public record ProgresoDto(Long idEpisodio, String etiqueta, int segundo, int porcentaje, boolean completado) {
    }

    public record DetalleResponse(
            Long idContenido, String titulo, String tituloOriginal, String tipo, Short anio, String sinopsis,
            String posterUrl, ClasificacionDto clasificacion, String idiomaOriginal, List<String> generos,
            List<CreditoDto> creditos, List<String> productoras, Integer duracionMinutos, String urlTrailer,
            List<TemporadaDto> temporadas, double calificacionPromedio, long totalCalificaciones,
            Short miCalificacion, boolean enMiLista, ProgresoDto progreso, LocalDate disponibleHasta,
            List<String> audios, List<String> subtitulos, boolean disponibleParaVer) {
    }

    public record CalificacionRequest(@NotNull @Min(1) @Max(5) Short valoracion) {
    }
}
