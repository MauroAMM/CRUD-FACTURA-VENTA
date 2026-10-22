/** Espejo de ItemCatalogoDTO.java y CatalogosDTO.java (datos para los combos del formulario). */

export interface ItemCatalogoDTO {
    id: number;
    descripcion: string;
    /** Solo viene en los artículos: el precio que tienen en la lista de precios consultada. */
    precioVenta: number | null;
}

/** Los clientes se buscan aparte (explorador); los artículos dependen de la lista de precios elegida. */
export interface CatalogosDTO {
    condicionesIva: ItemCatalogoDTO[];
    puntosVenta: ItemCatalogoDTO[];
    tiposMoneda: ItemCatalogoDTO[];
    listasPrecio: ItemCatalogoDTO[];
}
