package com.novapop.servicio;

import com.novapop.dto.CuentaDtos.CuentaResponse;
import com.novapop.dto.CuentaDtos.DispositivoDto;
import com.novapop.dto.CuentaDtos.DispositivoRequest;
import com.novapop.dto.CuentaDtos.MetodoPagoDto;
import com.novapop.dto.CuentaDtos.MetodoPagoRequest;
import com.novapop.dto.CuentaDtos.PagoDto;
import com.novapop.dto.CuentaDtos.PaisDto;
import com.novapop.dto.CuentaDtos.PlanDto;
import com.novapop.dto.CuentaDtos.SuscripcionDto;
import com.novapop.entidad.Cuenta;
import com.novapop.entidad.Dispositivo;
import com.novapop.entidad.MetodoPago;
import com.novapop.entidad.PagoSuscripcion;
import com.novapop.entidad.PlanSuscripcion;
import com.novapop.entidad.SituacionPago;
import com.novapop.entidad.SituacionSuscripcion;
import com.novapop.entidad.Suscripcion;
import com.novapop.entidad.TipoMetodoPago;
import com.novapop.excepcion.NovaPopException;
import com.novapop.repositorio.CuentaRepository;
import com.novapop.repositorio.DispositivoRepository;
import com.novapop.repositorio.MetodoPagoRepository;
import com.novapop.repositorio.PagoSuscripcionRepository;
import com.novapop.repositorio.PlanSuscripcionRepository;
import com.novapop.repositorio.SuscripcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final PlanSuscripcionRepository planRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final PagoSuscripcionRepository pagoRepository;
    private final DispositivoRepository dispositivoRepository;

    // ------------------------------------------------------------------ cuenta y suscripción

    @Transactional(readOnly = true)
    public CuentaResponse obtener(Long idCuenta) {
        Cuenta c = cuentaRepository.findConPais(idCuenta)
                .orElseThrow(() -> NovaPopException.noEncontrado("Cuenta no encontrada"));
        SuscripcionDto suscripcion = suscripcionRepository.findActiva(idCuenta)
                .map(s -> new SuscripcionDto(s.getIdSuscripcion(), PlanDto.de(s.getPlan()), s.getFechaInicio(),
                        s.getSituacionSuscripcion().name(), s.getRenovacionAutomatica()))
                .orElse(null);
        PaisDto pais = new PaisDto(c.getPais().getIdPais(), c.getPais().getCodigoIso(), c.getPais().getNombrePais());
        return new CuentaResponse(c.getCorreo(), pais, c.getFechaRegistro(), c.getSituacion().name(), suscripcion);
    }

    /** Crea una suscripción activa y registra su primer pago (simulado, siempre aprobado). */
    @Transactional
    public Suscripcion suscribir(Cuenta cuenta, PlanSuscripcion plan, MetodoPago metodo) {
        Suscripcion s = new Suscripcion();
        s.setCuenta(cuenta);
        s.setPlan(plan);
        s.setFechaInicio(LocalDate.now());
        s.setSituacionSuscripcion(SituacionSuscripcion.ACTIVA);
        s.setRenovacionAutomatica(true);
        s = suscripcionRepository.save(s);

        PagoSuscripcion pago = new PagoSuscripcion();
        pago.setSuscripcion(s);
        pago.setMetodoPago(metodo);
        pago.setFechaPago(OffsetDateTime.now());
        pago.setMonto(plan.getPrecioMensual());
        pago.setSituacionPago(SituacionPago.APROBADO);
        pagoRepository.save(pago);
        return s;
    }

    /** Cancela la suscripción vigente y abre una nueva con el plan elegido. */
    @Transactional
    public CuentaResponse cambiarPlan(Long idCuenta, Long idPlan) {
        PlanSuscripcion plan = planRepository.findById(idPlan)
                .filter(PlanSuscripcion::estaActivo)
                .orElseThrow(() -> NovaPopException.invalido("Plan no válido"));
        MetodoPago metodo = metodoPagoRepository.findPredeterminado(idCuenta)
                .orElseThrow(() -> NovaPopException.conflicto("Agrega un método de pago antes de cambiar de plan"));

        suscripcionRepository.findActiva(idCuenta).ifPresent(actual -> {
            if (actual.getPlan().getIdPlan().equals(idPlan)) {
                throw NovaPopException.conflicto("Ya tienes el plan " + plan.getNombrePlan());
            }
            actual.setSituacionSuscripcion(SituacionSuscripcion.CANCELADA);
            actual.setFechaFin(LocalDate.now());
            // El índice único "una suscripción activa por cuenta" exige escribir esto antes del nuevo INSERT
            suscripcionRepository.saveAndFlush(actual);
        });
        suscribir(cuentaRepository.getReferenceById(idCuenta), plan, metodo);
        return obtener(idCuenta);
    }

    @Transactional
    public CuentaResponse cambiarRenovacion(Long idCuenta, boolean renovacionAutomatica) {
        Suscripcion s = suscripcionRepository.findActiva(idCuenta)
                .orElseThrow(() -> NovaPopException.noEncontrado("No tienes una suscripción activa"));
        s.setRenovacionAutomatica(renovacionAutomatica);
        return obtener(idCuenta);
    }

    @Transactional(readOnly = true)
    public List<PagoDto> pagos(Long idCuenta) {
        return pagoRepository.findPorCuenta(idCuenta).stream()
                .map(p -> new PagoDto(p.getIdPago(), p.getFechaPago(), p.getMonto(), p.getSituacionPago().name(),
                        p.getSuscripcion().getPlan().getNombrePlan(), describir(p.getMetodoPago())))
                .toList();
    }

    // ------------------------------------------------------------------ métodos de pago

    @Transactional(readOnly = true)
    public List<MetodoPagoDto> metodosPago(Long idCuenta) {
        return metodoPagoRepository.findActivos(idCuenta).stream().map(this::aDto).toList();
    }

    @Transactional
    public MetodoPagoDto agregarMetodoPago(Long idCuenta, MetodoPagoRequest req) {
        boolean esPrimero = metodoPagoRepository.findPredeterminado(idCuenta).isEmpty();
        MetodoPago m = guardarMetodoPago(cuentaRepository.getReferenceById(idCuenta), req.tipo(), req.numero(), esPrimero);
        return aDto(m);
    }

    /** Guarda el método de pago con el número enmascarado: nunca se almacena completo. */
    @Transactional
    public MetodoPago guardarMetodoPago(Cuenta cuenta, TipoMetodoPago tipo, String numero, boolean predeterminado) {
        MetodoPago m = new MetodoPago();
        m.setCuenta(cuenta);
        m.setTipoMetodo(tipo);
        m.setNumeroEnmascarado(enmascarar(tipo, numero));
        m.setEsPredeterminado(predeterminado);
        return metodoPagoRepository.save(m);
    }

    @Transactional
    public List<MetodoPagoDto> predeterminar(Long idCuenta, Long idMetodo) {
        MetodoPago elegido = metodoDeCuenta(idCuenta, idMetodo);
        metodoPagoRepository.findPredeterminado(idCuenta).ifPresent(actual -> {
            actual.setEsPredeterminado(false);
            metodoPagoRepository.saveAndFlush(actual); // respeta el índice único de predeterminado
        });
        elegido.setEsPredeterminado(true);
        metodoPagoRepository.saveAndFlush(elegido);
        return metodosPago(idCuenta);
    }

    @Transactional
    public void eliminarMetodoPago(Long idCuenta, Long idMetodo) {
        MetodoPago m = metodoDeCuenta(idCuenta, idMetodo);
        if (m.getEsPredeterminado()) {
            throw NovaPopException.conflicto("No puedes eliminar el método predeterminado; elige otro primero");
        }
        m.inactivar();
    }

    // ------------------------------------------------------------------ dispositivos

    @Transactional(readOnly = true)
    public List<DispositivoDto> dispositivos(Long idCuenta) {
        return dispositivoRepository.findActivos(idCuenta).stream().map(this::aDto).toList();
    }

    @Transactional
    public DispositivoDto registrarDispositivo(Long idCuenta, DispositivoRequest req) {
        Dispositivo d = new Dispositivo();
        d.setCuenta(cuentaRepository.getReferenceById(idCuenta));
        d.setTipoDispositivo(req.tipo());
        d.setNombreDispositivo(req.nombre().trim());
        d.setUltimaConexion(OffsetDateTime.now());
        return aDto(dispositivoRepository.save(d));
    }

    @Transactional
    public void eliminarDispositivo(Long idCuenta, Long idDispositivo) {
        dispositivoRepository.findDeCuenta(idCuenta, idDispositivo)
                .orElseThrow(() -> NovaPopException.noEncontrado("Dispositivo no encontrado"))
                .inactivar();
    }

    // ------------------------------------------------------------------ auxiliares

    private MetodoPago metodoDeCuenta(Long idCuenta, Long idMetodo) {
        return metodoPagoRepository.findById(idMetodo)
                .filter(m -> m.estaActivo() && m.getCuenta().getIdCuenta().equals(idCuenta))
                .orElseThrow(() -> NovaPopException.noEncontrado("Método de pago no encontrado"));
    }

    static String enmascarar(TipoMetodoPago tipo, String numero) {
        String ultimos = numero.substring(numero.length() - Math.min(4, numero.length()));
        return switch (tipo) {
            case TARJETA_CREDITO, TARJETA_DEBITO -> "**** **** **** " + ultimos;
            case YAPE -> "*** *** " + numero.substring(numero.length() - 3);
            case PAYPAL -> "PayPal ••" + ultimos;
        };
    }

    private static String describir(MetodoPago m) {
        return m.getTipoMetodo().name().replace('_', ' ') + " " + m.getNumeroEnmascarado();
    }

    private MetodoPagoDto aDto(MetodoPago m) {
        return new MetodoPagoDto(m.getIdMetodoPago(), m.getTipoMetodo(), m.getNumeroEnmascarado(), m.getEsPredeterminado());
    }

    private DispositivoDto aDto(Dispositivo d) {
        return new DispositivoDto(d.getIdDispositivo(), d.getTipoDispositivo(), d.getNombreDispositivo(), d.getUltimaConexion());
    }
}
