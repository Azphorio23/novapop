package com.novapop.controlador;

import com.novapop.dto.CuentaDtos.CambioPlanRequest;
import com.novapop.dto.CuentaDtos.CuentaResponse;
import com.novapop.dto.CuentaDtos.DispositivoDto;
import com.novapop.dto.CuentaDtos.DispositivoRequest;
import com.novapop.dto.CuentaDtos.MetodoPagoDto;
import com.novapop.dto.CuentaDtos.MetodoPagoRequest;
import com.novapop.dto.CuentaDtos.PagoDto;
import com.novapop.dto.CuentaDtos.RenovacionRequest;
import com.novapop.servicio.CuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.novapop.controlador.CuentaActual.id;

@RestController
@RequestMapping("/api/cuenta")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @GetMapping
    public CuentaResponse obtener(@AuthenticationPrincipal Jwt jwt) {
        return cuentaService.obtener(id(jwt));
    }

    @PutMapping("/plan")
    public CuentaResponse cambiarPlan(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CambioPlanRequest req) {
        return cuentaService.cambiarPlan(id(jwt), req.idPlan());
    }

    @PutMapping("/renovacion")
    public CuentaResponse renovacion(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RenovacionRequest req) {
        return cuentaService.cambiarRenovacion(id(jwt), req.renovacionAutomatica());
    }

    @GetMapping("/pagos")
    public List<PagoDto> pagos(@AuthenticationPrincipal Jwt jwt) {
        return cuentaService.pagos(id(jwt));
    }

    @GetMapping("/metodos-pago")
    public List<MetodoPagoDto> metodosPago(@AuthenticationPrincipal Jwt jwt) {
        return cuentaService.metodosPago(id(jwt));
    }

    @PostMapping("/metodos-pago")
    @ResponseStatus(HttpStatus.CREATED)
    public MetodoPagoDto agregarMetodoPago(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MetodoPagoRequest req) {
        return cuentaService.agregarMetodoPago(id(jwt), req);
    }

    @PutMapping("/metodos-pago/{idMetodo}/predeterminado")
    public List<MetodoPagoDto> predeterminar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long idMetodo) {
        return cuentaService.predeterminar(id(jwt), idMetodo);
    }

    @DeleteMapping("/metodos-pago/{idMetodo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarMetodoPago(@AuthenticationPrincipal Jwt jwt, @PathVariable Long idMetodo) {
        cuentaService.eliminarMetodoPago(id(jwt), idMetodo);
    }

    @GetMapping("/dispositivos")
    public List<DispositivoDto> dispositivos(@AuthenticationPrincipal Jwt jwt) {
        return cuentaService.dispositivos(id(jwt));
    }

    @PostMapping("/dispositivos")
    @ResponseStatus(HttpStatus.CREATED)
    public DispositivoDto registrarDispositivo(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DispositivoRequest req) {
        return cuentaService.registrarDispositivo(id(jwt), req);
    }

    @DeleteMapping("/dispositivos/{idDispositivo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarDispositivo(@AuthenticationPrincipal Jwt jwt, @PathVariable Long idDispositivo) {
        cuentaService.eliminarDispositivo(id(jwt), idDispositivo);
    }
}
