package com.utn.desarrollosoft.contable.entidades;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class FacturaVenta extends AuditoriaApp {

    /** Número de comprobante. Único: la base impide duplicados. */
    @Column(unique = true)
    private Long numero;

    @Temporal(TemporalType.DATE)
    private Date fechaEmision;

    @ManyToOne
    private PuntoVenta puntoVenta;
    @ManyToOne
    private Cliente cliente;
    @ManyToOne
    private CondicionIva condicionIva;
    @ManyToOne
    private TipoMoneda tipoMoneda;
    /** Lista de precios con la que se armó la factura (puede ser null en facturas anteriores a esta columna). */
    @ManyToOne
    private ListaPrecio listaPrecio;

    private double importeCobrado;
    private double importeSaldo;
    private double importeTotal;
    private String cae;

    @Temporal(TemporalType.DATE)
    private Date caeFechaVencimiento;

    private String resultadoAfip;
    private String motivoRechazo;

    @Column(length = 20)
    private String estado;

    private Date fechaAnulacion;

    @Column(length = 500)
    private String observaciones;

    /**
     * Los detalles se guardan, modifican y borran junto con la factura (cascada).
     * Importante: esta lista NO debe reemplazarse por otra (setDetalles) al actualizar;
     * hay que usar clear() + add() para que orphanRemoval borre los renglones viejos.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FacturaVentaDetalle> detalles = new ArrayList<>();
}
