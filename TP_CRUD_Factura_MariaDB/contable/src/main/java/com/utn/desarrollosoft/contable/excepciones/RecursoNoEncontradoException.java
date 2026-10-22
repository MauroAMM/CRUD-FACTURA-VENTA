package com.utn.desarrollosoft.contable.excepciones;

/**
 * Se lanza cuando un recurso (factura, cliente, artículo, etc.) no existe.
 * El manejador global la traduce a HTTP 404 Not Found.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public RecursoNoEncontradoException(String recurso, Long id) {
        super(recurso + " con id " + id + " no existe");
    }
}
