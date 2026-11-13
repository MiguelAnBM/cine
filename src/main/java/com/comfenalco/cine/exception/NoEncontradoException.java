package com.comfenalco.cine.exception;

// Sin @ResponseStatus: el manejo global en WebExceptionHandler hace redirect con flash
public class NoEncontradoException extends RuntimeException {
    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
