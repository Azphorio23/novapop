package com.novapop.servicio;

import com.novapop.entidad.PlanSuscripcion;
import com.novapop.excepcion.NovaPopException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * Pasarela de pago Mercado Pago (Checkout Pro).
 * Con el token de PRUEBA (sandbox) no se cobra dinero real: el usuario paga con tarjetas de prueba en la página
 * de Mercado Pago. Si el token está vacío, la pasarela queda desactivada y NovaPop sigue con el pago simulado.
 *
 * Flujo:
 *   1. {@link #crearPreferencia}: se crea una "preferencia" con el plan y se obtiene la URL del checkout.
 *   2. El usuario paga en Mercado Pago y vuelve a NovaPop con el id del pago.
 *   3. {@link #estadoPago}: se consulta a Mercado Pago el estado real del pago antes de activar la suscripción.
 */
@Slf4j
@Service
public class MercadoPagoService {

    private final String token;
    private final RestClient mp = RestClient.create("https://api.mercadopago.com");

    public MercadoPagoService(@Value("${novapop.mercadopago.token:}") String token) {
        this.token = token.trim();
    }

    public boolean configurado() {
        return !token.isEmpty();
    }

    /** URL del checkout de Mercado Pago a la que se envía al usuario. */
    public record Preferencia(String url, String id) {
    }

    /** Estado real de un pago consultado a Mercado Pago. */
    public record EstadoPago(String estado, String referenciaExterna, String ultimosDigitos) {
        public boolean aprobado() {
            return "approved".equals(estado);
        }
    }

    /**
     * Crea la preferencia de pago de un plan y devuelve la URL del checkout.
     * referencia = "idCuenta:idPlan", para saber al volver qué cuenta pagó qué plan.
     */
    public Preferencia crearPreferencia(PlanSuscripcion plan, long idCuenta, String urlBase) {
        exigirConfigurado();
        String retorno = urlBase + "/cuenta.html";
        Map<String, Object> cuerpo = new java.util.HashMap<>(Map.of(
                "items", List.of(Map.of(
                        "title", "NovaPop — Plan " + plan.getNombrePlan(),
                        "quantity", 1,
                        "unit_price", plan.getPrecioMensual(),
                        "currency_id", "PEN")),
                "external_reference", idCuenta + ":" + plan.getIdPlan(),
                "statement_descriptor", "NOVAPOP",
                "back_urls", Map.of("success", retorno, "pending", retorno, "failure", retorno)));
        // auto_return solo cuando la URL de retorno es pública (https): Mercado Pago rechaza volver a http://localhost
        if (urlBase.startsWith("https")) {
            cuerpo.put("auto_return", "approved");
        }
        try {
            Map<?, ?> r = mp.post().uri("/checkout/preferences")
                    .headers(h -> h.setBearerAuth(token))
                    .body(cuerpo)
                    .retrieve().body(Map.class);
            if (r == null || r.get("id") == null) {
                throw NovaPopException.invalido("Mercado Pago no devolvió la preferencia de pago");
            }
            // init_point: con credenciales de prueba abre el checkout en modo sandbox (el sandbox_init_point heredado falla)
            Object url = r.get("init_point");
            if (url == null) {
                url = r.get("sandbox_init_point");
            }
            return new Preferencia(String.valueOf(url), String.valueOf(r.get("id")));
        } catch (RestClientException e) {
            log.warn("Mercado Pago no creó la preferencia: {}", e.getMessage());
            throw NovaPopException.invalido("No se pudo iniciar el pago con Mercado Pago. Inténtalo de nuevo");
        }
    }

    /** Consulta a Mercado Pago el estado real de un pago por su id. */
    public EstadoPago estadoPago(String idPago) {
        exigirConfigurado();
        try {
            Map<?, ?> r = mp.get().uri("/v1/payments/{id}", idPago)
                    .headers(h -> h.set(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .retrieve().body(Map.class);
            if (r == null) {
                throw NovaPopException.invalido("Mercado Pago no devolvió el pago");
            }
            Object tarjeta = r.get("card");
            String ultimos = tarjeta instanceof Map<?, ?> c && c.get("last_four_digits") != null
                    ? String.valueOf(c.get("last_four_digits")) : null;
            return new EstadoPago(String.valueOf(r.get("status")), String.valueOf(r.get("external_reference")), ultimos);
        } catch (RestClientException e) {
            log.warn("Mercado Pago no devolvió el pago {}: {}", idPago, e.getMessage());
            throw NovaPopException.invalido("No se pudo verificar el pago con Mercado Pago");
        }
    }

    private void exigirConfigurado() {
        if (!configurado()) {
            throw NovaPopException.invalido("La pasarela de pago no está configurada");
        }
    }
}
