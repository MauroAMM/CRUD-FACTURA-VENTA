package com.utn.desarrollosoft.contable.entidades.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Factura de venta tal como se devuelve al cliente (GET, y respuesta de POST / PUT). */
@Data
public class FacturaReporteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long numeroFactura;
    /** Se serializa como "yyyy-MM-dd". */
    private LocalDate fechaEmision;
    private Long clienteId;
    private String clienteDenominacion;
    private Long condicionIvaId;
    private String condicionIva;
    private Long puntoVentaId;
    private String puntoVentaDescripcion;
    private Long tipoMonedaId;
    private String tipoMoneda;
    private Long listaPrecioId;
    private Double importeNeto;
    private Double importeIva;
    private Double importeTotal;
    private String estado;
    /** Solo tiene valor si la factura fue anulada. */
    private Date fechaAnulacion;
    private String observaciones;
    private List<FacturaDetalleReporteDTO> detalles = new ArrayList<>();
}
