package com.novapop.seguridad;

import com.novapop.entidad.Cuenta;
import com.novapop.entidad.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Emite los tokens JWT de clientes (cuentas) y del personal del panel de administración. */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder encoder;
    private final JwtPropiedades propiedades;

    /** Token de un cliente: sub = id_cuenta. */
    public String generarToken(Cuenta cuenta) {
        return firmar(String.valueOf(cuenta.getIdCuenta()), cuenta.getCorreo(), List.of("CLIENTE"));
    }

    /** Token del personal del panel de administración: permisos STAFF + ROLE_&lt;rol&gt;. */
    public String generarTokenStaff(Usuario usuario) {
        return firmar("staff-" + usuario.getIdUsuario(), usuario.getNombreUsuario(),
                List.of("STAFF", "ROLE_" + usuario.getRol().getNombreRol()));
    }

    private String firmar(String sujeto, String usuarioAuditoria, List<String> permisos) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("novapop")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(propiedades.horasValidez(), ChronoUnit.HOURS))
                .subject(sujeto)
                .claim("correo", usuarioAuditoria) // lo usa la auditoría (usuario_creacion / usuario_modificacion)
                .claim("permisos", permisos)
                .build();
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();
    }
}
