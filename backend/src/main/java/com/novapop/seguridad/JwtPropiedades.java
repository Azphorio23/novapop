package com.novapop.seguridad;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Propiedades novapop.jwt.* de application.properties. */
@ConfigurationProperties(prefix = "novapop.jwt")
public record JwtPropiedades(String secreto, long horasValidez) {

    public JwtPropiedades {
        if (secreto == null || secreto.length() < 32) {
            throw new IllegalArgumentException("novapop.jwt.secreto (NOVAPOP_JWT_SECRET) debe tener al menos 32 caracteres");
        }
    }
}
