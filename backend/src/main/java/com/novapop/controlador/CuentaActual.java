package com.novapop.controlador;

import org.springframework.security.oauth2.jwt.Jwt;

/** Lee el id de la cuenta autenticada desde el token (claim "sub"). */
final class CuentaActual {

    /** Cabecera con la que el frontend indica qué perfil está usando. */
    static final String CABECERA_PERFIL = "X-Perfil-Id";

    private CuentaActual() {
    }

    static Long id(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
