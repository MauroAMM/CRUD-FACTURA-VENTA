/**
 * Interfaces que espejan los DTOs de Spring Boot (paquete entidades.dto).
 * Correspondencia de tipos:  Long/Double/Integer -> number,  String -> string,
 * LocalDate -> string "yyyy-MM-dd",  Date -> string ISO-8601,  List<T> -> T[].
 * Los campos con "?" son los que el backend acepta como null / opcionales.
 */

// ---------------------------------------------------------------------------
// FacturaDetalleRequestDTO.java
// ---------------------------------------------------------------------------
export interface FacturaDetalleRequestDTO {
    articuloId: number;
    /** Si no se informa, el backend usa la denominación del artículo. */
    descripcion?: string;
    cantidad: number;
    /** Si no se informa, el backend usa el precio de la lista de precios. */
    precioUnitario?: number;
    /** 0 a 100. Si no se informa, se asume 0. */
    porcentajeBonificacion?: number;
}

// ---------------------------------------------------------------------------
// FacturaRequestDTO.java
//   POST: clienteId, condicionIvaId, puntoVentaId, tipoMonedaId y detalles son obligatorios.
//   PUT : actualización parcial; lo que no se envía (undefined) no se modifica.
// ---------------------------------------------------------------------------
export interface FacturaRequestDTO {
    /** "yyyy-MM-dd". Si no se informa en el alta, el backend usa la fecha de hoy. */
    fechaEmision?: string;
    clienteId?: number;
    condicionIvaId?: number;
    puntoVentaId?: number;
    tipoMonedaId?: number;
    /** Si no se informa, el backend usa la primera lista de precios (la general). */
    listaPrecioId?: number;
    observaciones?: string;
    /** Solo en PUT: "EMITIDA" o "ANULADA". */
    estado?: string;
    detalles?: FacturaDetalleRequestDTO[];
}

// ---------------------------------------------------------------------------
// FacturaDetalleReporteDTO.java
// ---------------------------------------------------------------------------
export interface FacturaDetalleReporteDTO {
    id: number;
    articuloId: number;
    descripcion: string;
    cantidad: number;
    precioUnitario: number;
    porcentajeBonificacion: number;
    importeNeto: number;
    importeIva: number;
    importeSubtotal: number;
}

// ---------------------------------------------------------------------------
// FacturaReporteDTO.java
// ---------------------------------------------------------------------------
export interface FacturaReporteDTO {
    id: number;
    numeroFactura: number;
    /** "yyyy-MM-dd" */
    fechaEmision: string;
    clienteId: number;
    clienteDenominacion: string;
    condicionIvaId: number;
    condicionIva: string;
    puntoVentaId: number;
    puntoVentaDescripcion: string;
    tipoMonedaId: number;
    tipoMoneda: string;
    listaPrecioId: number | null;
    importeNeto: number;
    importeIva: number;
    importeTotal: number;
    estado: string;
    /** ISO-8601. Solo tiene valor si la factura fue anulada. */
    fechaAnulacion: string | null;
    observaciones: string | null;
    detalles: FacturaDetalleReporteDTO[];
}

// ---------------------------------------------------------------------------
// Filtros del listado (query params de GET /api/facturas)
// ---------------------------------------------------------------------------
export interface FacturaFiltrosDTO {
    numeroFactura?: string;
    /** ID del cliente elegido en el explorador (lupa). */
    clienteId?: string;
    /** Búsqueda por texto en el nombre del cliente (alternativa a clienteId). */
    clienteDenominacion?: string;
    condicionIva?: string;
    estado?: string;
    /** "yyyy-MM-dd" */
    fechaDesde?: string;
    /** "yyyy-MM-dd" */
    fechaHasta?: string;
}

// ---------------------------------------------------------------------------
// Estados válidos de una factura
// ---------------------------------------------------------------------------
export const ESTADO_EMITIDA = 'EMITIDA';
export const ESTADO_ANULADA = 'ANULADA';
