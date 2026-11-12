package com.novapop.servicio;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filmografía de actores y directores consultada a TMDB (The Movie Database).
 * La clave se usa solo en el servidor; el navegador nunca la ve. Si no está configurada,
 * la ficha de la persona muestra únicamente sus títulos dentro de NovaPop.
 */
@Slf4j
@Service
public class TmdbService {

    private static final String IMAGEN = "https://image.tmdb.org/t/p/w342";
    private static final int MAXIMO_TRABAJOS = 30;
    /** Géneros de TMDB que no son obras de la persona: programas de entrevistas y noticieros. */
    private static final Set<Integer> GENEROS_EXCLUIDOS = Set.of(10767, 10763);
    /** Oficios detrás de cámara que se muestran, en español; los demás (agradecimientos, asistentes…) se omiten. */
    private static final Map<String, String> OFICIOS = Map.ofEntries(
            Map.entry("Director", "Director"), Map.entry("Screenplay", "Guion"), Map.entry("Writer", "Guionista"),
            Map.entry("Story", "Historia"), Map.entry("Novel", "Novela"), Map.entry("Characters", "Personajes"),
            Map.entry("Creator", "Creador"), Map.entry("Producer", "Productor"),
            Map.entry("Executive Producer", "Productor ejecutivo"), Map.entry("Original Music Composer", "Música"),
            Map.entry("Director of Photography", "Fotografía"), Map.entry("Editor", "Edición"));

    private final String clave;
    private final boolean esToken;
    private final RestClient tmdb = RestClient.create("https://api.themoviedb.org/3");
    /** La filmografía cambia poco: se guarda en memoria por nombre mientras la app esté encendida. */
    private final Map<String, List<Trabajo>> cache = new ConcurrentHashMap<>();

    public TmdbService(@Value("${novapop.tmdb.key:}") String clave) {
        this.clave = clave.trim();
        this.esToken = this.clave.startsWith("eyJ"); // el "API Read Access Token" (v4) es un JWT
    }

    public boolean configurado() {
        return !clave.isEmpty();
    }

    /** Una película o serie de TMDB en la que participó la persona. */
    public record Trabajo(String titulo, String tituloOriginal, Short anio, String tipo, String posterUrl, String rol,
                          int votos) {
    }

    /** Trabajos más conocidos de la persona (los de más votos primero); lista vacía si no se encuentra o TMDB falla. */
    public List<Trabajo> filmografia(String nombre) {
        if (!configurado() || nombre == null || nombre.isBlank()) {
            return List.of();
        }
        return cache.computeIfAbsent(CatalogoService.normalizar(nombre), k -> consultar(nombre));
    }

    private List<Trabajo> consultar(String nombre) {
        try {
            JsonNode resultados = get("/search/person?query={q}&language=es-MX", nombre).path("results");
            if (resultados.isEmpty()) {
                return List.of();
            }
            // Preferimos la coincidencia exacta del nombre; si no hay, la primera (la más popular)
            JsonNode persona = resultados.get(0);
            for (JsonNode r : resultados) {
                if (CatalogoService.normalizar(r.path("name").asText()).equals(CatalogoService.normalizar(nombre))) {
                    persona = r;
                    break;
                }
            }
            JsonNode creditos = get("/person/{id}/combined_credits?language=es-MX", persona.path("id").asLong());
            List<Trabajo> trabajos = new ArrayList<>();
            Set<String> vistos = new HashSet<>();
            // Para un director o guionista pesa más lo que hizo detrás de cámara que sus cameos
            boolean actua = "Acting".equals(persona.path("known_for_department").asText("Acting"));
            agregar(trabajos, vistos, creditos.path(actua ? "cast" : "crew"), actua);
            agregar(trabajos, vistos, creditos.path(actua ? "crew" : "cast"), !actua);
            trabajos.sort(Comparator.comparingInt(Trabajo::votos).reversed());
            return List.copyOf(trabajos.subList(0, Math.min(MAXIMO_TRABAJOS, trabajos.size())));
        } catch (RestClientException e) {
            log.warn("No se pudo consultar TMDB para {}: {}", nombre, e.getMessage());
            return List.of(); // no se guarda en caché: se reintenta en la próxima consulta
        }
    }

    private void agregar(List<Trabajo> trabajos, Set<String> vistos, JsonNode lista, boolean actuacion) {
        for (JsonNode c : lista) {
            String personaje = c.path("character").asText("");
            String oficio = actuacion ? null : OFICIOS.get(c.path("job").asText());
            boolean excluido = !actuacion && oficio == null;
            for (JsonNode g : c.path("genre_ids")) {
                excluido |= GENEROS_EXCLUIDOS.contains(g.asInt());
            }
            // Sin póster, apariciones como sí mismo o programas de entrevistas: no son parte de su obra
            if (c.path("poster_path").isMissingNode() || c.path("poster_path").isNull() || excluido
                    || personaje.matches("(?i).*\\b(self|himself|herself|narrator)\\b.*")
                    || !vistos.add(c.path("media_type").asText() + c.path("id").asText())) {
                continue;
            }
            boolean serie = "tv".equals(c.path("media_type").asText());
            String fecha = c.path(serie ? "first_air_date" : "release_date").asText("");
            trabajos.add(new Trabajo(
                    c.path(serie ? "name" : "title").asText(),
                    c.path(serie ? "original_name" : "original_title").asText(),
                    fecha.length() >= 4 ? Short.valueOf(fecha.substring(0, 4)) : null,
                    serie ? "SERIE" : "PELICULA",
                    IMAGEN + c.path("poster_path").asText(),
                    actuacion ? traducir(personaje) : oficio,
                    c.path("vote_count").asInt()));
        }
    }

    /** "Tony Stark (uncredited)" → "Tony Stark"; "(voice)" → "(voz)". */
    private static String traducir(String personaje) {
        return personaje.replaceAll("(?i)\\s*\\(uncredited\\)", "").replaceAll("(?i)\\(voice\\)", "(voz)").trim();
    }

    private JsonNode get(String ruta, Object... variables) {
        return tmdb.get()
                .uri(u -> {
                    var builder = u.path(ruta.substring(0, ruta.indexOf('?')));
                    for (String par : ruta.substring(ruta.indexOf('?') + 1).split("&")) {
                        String[] kv = par.split("=", 2);
                        builder.queryParam(kv[0], kv[1]);
                    }
                    if (!esToken) {
                        builder.queryParam("api_key", clave);
                    }
                    return builder.build(variables);
                })
                .headers(h -> {
                    if (esToken) {
                        h.setBearerAuth(clave);
                    }
                })
                .retrieve()
                .body(JsonNode.class);
    }
}
