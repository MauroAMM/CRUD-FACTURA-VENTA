package com.utn.desarrollosoft.contable.entidades.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** Elemento de un combo: id + texto a mostrar (+ precio de venta, solo para artículos). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCatalogoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String descripcion;
    private Double precioVenta;
}
