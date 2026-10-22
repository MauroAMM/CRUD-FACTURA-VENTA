/**
 * Interfaces que espejan los DTOs de Spring Boot (paquete entidades.dto).
 * Correspondencia de tipos:  Long/Double/Integer -> number,  String -> string,
 * LocalDate -> string "yyyy-MM-dd",  Date -> string ISO-8601,  List<T> -> T[].
 * Los campos con "?" son los que el backend acepta como null / opcionales.
 */
// ---------------------------------------------------------------------------
// Estados válidos de una factura
// ---------------------------------------------------------------------------
export const ESTADO_EMITIDA = 'EMITIDA';
export const ESTADO_ANULADA = 'ANULADA';
