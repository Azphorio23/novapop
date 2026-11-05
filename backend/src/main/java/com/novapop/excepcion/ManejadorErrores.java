package com.novapop.excepcion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/** Convierte las excepciones en respuestas JSON uniformes: {estado, mensaje, fecha}. */
@Slf4j
@RestControllerAdvice
public class ManejadorErrores {

    public record RespuestaError(int estado, String mensaje, OffsetDateTime fecha) {
    }

    @ExceptionHandler(NovaPopException.class)
    ResponseEntity<RespuestaError> negocio(NovaPopException e) {
        return responder(e.getEstado(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<RespuestaError> validacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, mensaje);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    ResponseEntity<RespuestaError> cabecera(MissingRequestHeaderException e) {
        return responder(HttpStatus.BAD_REQUEST, "Falta la cabecera " + e.getHeaderName());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<RespuestaError> integridad(DataIntegrityViolationException e) {
        log.warn("Violación de integridad: {}", e.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT, "La operación viola una regla de la base de datos");
    }

    private ResponseEntity<RespuestaError> responder(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(new RespuestaError(estado.value(), mensaje, OffsetDateTime.now()));
    }
}
