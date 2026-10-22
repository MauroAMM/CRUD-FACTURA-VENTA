package com.utn.desarrollosoft.contable.entidades.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Renglón de una factura tal como lo envía el cliente (POST / PUT).
 * Vincula el artículo por ID; no se reciben objetos entidad anidados.
 * Los importes (neto, IVA, subtotal) NO se reciben: los calcula el servidor.
 */
@Data
public class FacturaDetalleRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long articuloId;
    /** Opcional. Si no viene, se usa la denominación del artículo. */
    private String descripcion;
    private Double cantidad;
    /** Opcional. Si no viene, se toma el precio de venta de la lista de precios. */
    private Double precioUnitario;
    /** Opcional (0 a 100). Si no viene, se asume 0. */
    private Double porcentajeBonificacion;
}
