package com.utn.desarrollosoft.contable.entidades;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class ListaPrecioArticulo extends AuditoriaApp {
    @ManyToOne
    private ListaPrecio listaPrecio;
    private double precioVenta;
    @ManyToOne
    private Articulo articulo;
}
