package com.novapop.auditoria;

import com.novapop.entidad.EntidadAuditable;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.OffsetDateTime;

/** Completa los campos de auditoría con el usuario autenticado y la IP de la petición. */
public class AuditoriaListener {

    private static final String SISTEMA = "SISTEMA";

    @PrePersist
    public void alCrear(EntidadAuditable entidad) {
        entidad.setUsuarioCreacion(usuarioActual());
        entidad.setFechaCreacion(OffsetDateTime.now());
        entidad.setIpCreacion(ipActual());
        if (entidad.getEstadoRegistro() == null) {
            entidad.setEstadoRegistro(EntidadAuditable.ACTIVO);
        }
    }

    @PreUpdate
    public void alModificar(EntidadAuditable entidad) {
        entidad.setUsuarioModificacion(usuarioActual());
        entidad.setFechaModificacion(OffsetDateTime.now());
        entidad.setIpModificacion(ipActual());
    }

    /** Usuario que se guarda en la auditoría: el correo de la cuenta autenticada, o SISTEMA. */
    public static String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String correo = jwt.getClaimAsString("correo");
            return correo != null ? correo : jwt.getSubject();
        }
        return SISTEMA;
    }

    /** IP del cliente; respeta X-Forwarded-For si hay un proxy delante. */
    public static String ipActual() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos)) {
            return null;
        }
        HttpServletRequest request = atributos.getRequest();
        String reenviada = request.getHeader("X-Forwarded-For");
        String ip = reenviada != null && !reenviada.isBlank() ? reenviada.split(",")[0].trim() : request.getRemoteAddr();
        return ip != null && ip.length() > 45 ? ip.substring(0, 45) : ip;
    }
}
