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
import java.util.UUID;

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
    private final GoogleService googleService;

    /**
     * Crea la cuenta con su método de pago, suscripción, primer pago y perfil inicial.
     * Con credencialGoogle el correo es el verificado por Google y no se pide contraseña.
     */
    @Transactional
    public TokenResponse registrar(RegistroRequest req) {
        boolean conGoogle = req.credencialGoogle() != null && !req.credencialGoogle().isBlank();
        if (!conGoogle && (req.clave() == null || req.clave().isBlank())) {
            throw NovaPopException.invalido("clave: es obligatoria");
        }
        String correo = conGoogle
                ? googleService.verificar(req.credencialGoogle()).correo()
                : req.correo().trim().toLowerCase();
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
        // Las cuentas de Google reciben una clave aleatoria que nadie conoce: solo entran con Google
        cuenta.setClaveHash(passwordEncoder.encode(conGoogle ? UUID.randomUUID().toString() : req.clave()));
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
        return emitirToken(cuenta);
    }

    /** Inicia sesión con Google. Si el correo aún no tiene cuenta responde 404 para que el navegador vaya al registro. */
    @Transactional(readOnly = true)
    public TokenResponse loginGoogle(String credencial) {
        String correo = googleService.verificar(credencial).correo();
        Cuenta cuenta = cuentaRepository.findPorCorreo(correo)
                .orElseThrow(() -> NovaPopException.noEncontrado("Aún no tienes una cuenta NovaPop con " + correo));
        return emitirToken(cuenta);
    }

    private TokenResponse emitirToken(Cuenta cuenta) {
        if (cuenta.getSituacion() != SituacionCuenta.ACTIVA) {
            throw NovaPopException.prohibido("La cuenta está " + cuenta.getSituacion().name().toLowerCase());
        }
        return new TokenResponse(jwtService.generarToken(cuenta), cuenta.getCorreo());
    }
}
