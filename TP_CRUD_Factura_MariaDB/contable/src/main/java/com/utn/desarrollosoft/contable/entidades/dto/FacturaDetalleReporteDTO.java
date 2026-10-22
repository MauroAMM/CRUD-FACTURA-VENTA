package com.utn.desarrollosoft.contable.entidades.dto;

import lombok.Data;

import java.io.Serializable;

/** Renglón de factura tal como se devuelve al cliente (ya con importes calculados). */
@Data
public class FacturaDetalleReporteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long articuloId;
    private String descripcion;
    private Double cantidad;
    private Double precioUnitario;
    private Double porcentajeBonificacion;
    private Double importeNeto;
    private Double importeIva;
    private Double importeSubtotal;
}
