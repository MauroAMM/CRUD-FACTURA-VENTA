package com.utn.desarrollosoft.contable.entidades.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * Datos de entrada para crear (POST) o modificar (PUT) una factura de venta.
 * Las relaciones se indican con IDs (clienteId, condicionIvaId, ...).
 *
 * POST: clienteId, condicionIvaId, puntoVentaId, tipoMonedaId y detalles son obligatorios.
 * PUT : actualización parcial. Todo campo en null se deja sin cambios;
 *       si "detalles" viene informado, reemplaza a los ítems actuales;
 *       si "estado" es "ANULADA", se anula la factura.
 */
@Data
public class FacturaRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Formato JSON: "yyyy-MM-dd". Si no viene en el alta, se usa la fecha de hoy. */
    private LocalDate fechaEmision;
    private Long clienteId;
    private Long condicionIvaId;
    private Long puntoVentaId;
    private Long tipoMonedaId;
    /** Opcional. Si no viene, se usa la primera lista de precios (la general). */
    private Long listaPrecioId;
    private String observaciones;
    /** Solo se usa en PUT: "EMITIDA" o "ANULADA". */
    private String estado;
    private List<FacturaDetalleRequestDTO> detalles;
}
