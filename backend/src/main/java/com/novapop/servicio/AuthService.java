package com.novapop.servicio;

import com.novapop.dto.AuthDtos.LoginRequest;
import com.novapop.dto.AuthDtos.RegistroRequest;
import com.novapop.dto.AuthDtos.TokenResponse;
import com.novapop.entidad.Cuenta;
import com.novapop.entidad.Pais;
import com.novapop.entidad.PlanSuscripcion;
import com.novapop.entidad.SituacionCuenta;
import com.novapop.excepcion.NovaPopException;
import com.novapop.repositorio.CuentaRepository;
import com.novapop.repositorio.PaisRepository;
import com.novapop.repositorio.PlanSuscripcionRepository;
import com.novapop.seguridad.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CuentaRepository cuentaRepository;
    private final PaisRepository paisRepository;
    private final PlanSuscripcionRepository planRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CuentaService cuentaService;
    private final PerfilService perfilService;

    /** Crea la cuenta con su método de pago, suscripción, primer pago y perfil inicial. */
    @Transactional
    public TokenResponse registrar(RegistroRequest req) {
        String correo = req.correo().trim().toLowerCase();
        if (cuentaRepository.existsByCorreoIgnoreCase(correo)) {
            throw NovaPopException.conflicto("Ya existe una cuenta con ese correo");
        }
        Pais pais = paisRepository.findById(req.idPais())
                .orElseThrow(() -> NovaPopException.invalido("País no válido"));
        PlanSuscripcion plan = planRepository.findById(req.idPlan())
                .filter(PlanSuscripcion::estaActivo)
                .orElseThrow(() -> NovaPopException.invalido("Plan no válido"));

        Cuenta cuenta = new Cuenta();
        cuenta.setPais(pais);
        cuenta.setCorreo(correo);
        cuenta.setClaveHash(passwordEncoder.encode(req.clave()));
        cuenta.setFechaRegistro(OffsetDateTime.now());
        cuenta.setSituacion(SituacionCuenta.ACTIVA);
        cuenta = cuentaRepository.save(cuenta);

        var metodo = cuentaService.guardarMetodoPago(cuenta, req.tipoMetodo(), req.numeroPago(), true);
        cuentaService.suscribir(cuenta, plan, metodo);
        perfilService.crearInicial(cuenta, req.nombrePerfil());

        return new TokenResponse(jwtService.generarToken(cuenta), cuenta.getCorreo());
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        Cuenta cuenta = cuentaRepository.findPorCorreo(req.correo().trim())
                .filter(c -> passwordEncoder.matches(req.clave(), c.getClaveHash()))
                .orElseThrow(() -> NovaPopException.noAutorizado("Correo o contraseña incorrectos"));
        if (cuenta.getSituacion() != SituacionCuenta.ACTIVA) {
            throw NovaPopException.prohibido("La cuenta está " + cuenta.getSituacion().name().toLowerCase());
        }
        return new TokenResponse(jwtService.generarToken(cuenta), cuenta.getCorreo());
    }
}
