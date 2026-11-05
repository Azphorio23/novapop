package com.novapop.servicio;

import com.novapop.excepcion.NovaPopException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.Set;

/**
 * Inicio de sesión con Google (Google Identity Services).
 * El navegador recibe de Google un ID token firmado; aquí se le pregunta a Google si es auténtico
 * y si fue emitido para NovaPop, antes de confiar en el correo que trae.
 */
@Service
public class GoogleService {

    private static final Set<String> EMISORES = Set.of("accounts.google.com", "https://accounts.google.com");

    private final String clientId;
    private final RestClient google = RestClient.create("https://oauth2.googleapis.com");

    public GoogleService(@Value("${novapop.google.client-id:}") String clientId) {
        this.clientId = clientId.trim();
    }

    /** Client ID público que usa el botón de Google; vacío si no está configurado. */
    public String clientId() {
        return clientId;
    }

    public record UsuarioGoogle(String correo, String nombre) {
    }

    /** Valida el ID token con Google y devuelve el correo verificado del usuario. */
    public UsuarioGoogle verificar(String credencial) {
        if (clientId.isEmpty()) {
            throw NovaPopException.invalido("El inicio de sesión con Google no está configurado");
        }
        Map<?, ?> datos;
        try {
            // tokeninfo comprueba la firma y la expiración; responde 400 si el token no es válido
            datos = google.get()
                    .uri(u -> u.path("/tokeninfo").queryParam("id_token", credencial).build())
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException e) {
            throw NovaPopException.noAutorizado("No se pudo validar tu cuenta de Google. Inténtalo de nuevo");
        }
        if (datos == null
                || !clientId.equals(datos.get("aud"))
                || !EMISORES.contains(String.valueOf(datos.get("iss")))
                || !"true".equals(String.valueOf(datos.get("email_verified")))
                || datos.get("email") == null) {
            throw NovaPopException.noAutorizado("La cuenta de Google no es válida para NovaPop");
        }
        Object nombre = datos.get("given_name") != null ? datos.get("given_name") : datos.get("name");
        return new UsuarioGoogle(String.valueOf(datos.get("email")).toLowerCase(), nombre == null ? null : String.valueOf(nombre));
    }
}
