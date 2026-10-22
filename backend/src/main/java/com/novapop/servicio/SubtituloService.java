package com.novapop.servicio;

import com.novapop.entidad.Subtitulo;
import com.novapop.excepcion.NovaPopException;
import com.novapop.repositorio.SubtituloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entrega los subtítulos en WebVTT desde nuestro dominio. Los archivos originales están en otro
 * servidor sin CORS, así que el navegador no podría cargarlos directamente en el &lt;track&gt;.
 */
@Service
@RequiredArgsConstructor
public class SubtituloService {

    private final SubtituloRepository subtituloRepository;
    private final Map<Long, String> cache = new ConcurrentHashMap<>();
    private final RestClient http = RestClient.builder()
            .defaultHeader("User-Agent", "NovaPop/1.0 (proyecto académico)")
            .build();

    public String obtenerVtt(Long idSubtitulo) {
        return cache.computeIfAbsent(idSubtitulo, this::descargar);
    }

    private String descargar(Long idSubtitulo) {
        Subtitulo s = subtituloRepository.findById(idSubtitulo)
                .filter(Subtitulo::estaActivo)
                .orElseThrow(() -> NovaPopException.noEncontrado("Subtítulo no encontrado"));
        try {
            String texto = http.get().uri(java.net.URI.create(s.getUrlArchivo())).retrieve().body(String.class);
            if (texto == null || texto.isBlank()) {
                throw new NovaPopException(HttpStatus.BAD_GATEWAY, "El archivo de subtítulos está vacío");
            }
            return aVtt(texto);
        } catch (RestClientException e) {
            throw new NovaPopException(HttpStatus.BAD_GATEWAY, "No se pudo descargar el subtítulo");
        }
    }

    /** Convierte SRT a WebVTT (si ya es VTT lo deja igual). */
    static String aVtt(String texto) {
        String limpio = texto.replace("﻿", "").replace("\r\n", "\n");
        if (limpio.startsWith("WEBVTT")) {
            return limpio;
        }
        // En SRT los milisegundos van con coma: 00:01:02,500 --> en VTT con punto
        return "WEBVTT\n\n" + limpio.replaceAll("(\\d{2}:\\d{2}:\\d{2}),(\\d{3})", "$1.$2");
    }
}
