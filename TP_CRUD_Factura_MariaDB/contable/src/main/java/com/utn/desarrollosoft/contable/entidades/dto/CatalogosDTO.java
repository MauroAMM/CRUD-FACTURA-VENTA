package com.utn.desarrollosoft.contable.entidades.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Datos maestros que necesita el formulario para cargar sus combos.
 * Los clientes se buscan con GET /api/clientes y los artículos con GET /api/listas-precio/{id}/articulos.
 */
@Data
public class CatalogosDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<ItemCatalogoDTO> condicionesIva = new ArrayList<>();
    private List<ItemCatalogoDTO> puntosVenta = new ArrayList<>();
    private List<ItemCatalogoDTO> tiposMoneda = new ArrayList<>();
    private List<ItemCatalogoDTO> listasPrecio = new ArrayList<>();
}
