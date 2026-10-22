package com.utn.desarrollosoft.contable.excepciones;

/**
 * Se lanza cuando la petición es inválida o viola una regla de negocio
 * (datos faltantes, cantidades negativas, factura ya anulada, etc.).
 * El manejador global la traduce a HTTP 400 Bad Request.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
