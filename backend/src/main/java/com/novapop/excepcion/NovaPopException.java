package com.novapop.excepcion;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Error de negocio con el código HTTP que debe recibir el cliente. */
@Getter
public class NovaPopException extends RuntimeException {

    private final HttpStatus estado;

    public NovaPopException(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public static NovaPopException noEncontrado(String mensaje) {
        return new NovaPopException(HttpStatus.NOT_FOUND, mensaje);
    }

    public static NovaPopException prohibido(String mensaje) {
        return new NovaPopException(HttpStatus.FORBIDDEN, mensaje);
    }

    public static NovaPopException conflicto(String mensaje) {
        return new NovaPopException(HttpStatus.CONFLICT, mensaje);
    }

    public static NovaPopException invalido(String mensaje) {
        return new NovaPopException(HttpStatus.BAD_REQUEST, mensaje);
    }

    public static NovaPopException noAutorizado(String mensaje) {
        return new NovaPopException(HttpStatus.UNAUTHORIZED, mensaje);
    }
}
