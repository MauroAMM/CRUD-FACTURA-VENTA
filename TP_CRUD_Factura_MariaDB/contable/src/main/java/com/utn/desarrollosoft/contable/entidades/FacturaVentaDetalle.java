package com.utn.desarrollosoft.contable.entidades;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
public class FacturaVentaDetalle extends EntityId {

    /** Lado "dueño" de la relación: la columna factura_id vive en esta tabla. */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private FacturaVenta factura;

    @ManyToOne
    private ListaPrecioArticulo listaPrecioArticulo;

    private String descripcion;
    private double cantidad;
    private double precioUnitario;
    private double porcentajeBonificacion;
    private double importeNeto;
    private double importeIva;
    private double importeSubtotal;
}
