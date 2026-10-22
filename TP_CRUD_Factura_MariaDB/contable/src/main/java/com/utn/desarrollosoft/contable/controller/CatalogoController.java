package com.utn.desarrollosoft.contable.controller;

import com.utn.desarrollosoft.contable.entidades.dto.CatalogosDTO;
import com.utn.desarrollosoft.contable.entidades.dto.ItemCatalogoDTO;
import com.utn.desarrollosoft.contable.servicios.CatalogoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    /** Condiciones de IVA, puntos de venta, monedas y listas de precios (con su ID). */
    @GetMapping("api/catalogos")
    public ResponseEntity<CatalogosDTO> getCatalogos() {
        return ResponseEntity.ok(catalogoService.obtenerCatalogos());
    }

    /** Artículos de una lista de precios, con el precio que tienen en esa lista. */
    @GetMapping("api/listas-precio/{id}/articulos")
    public ResponseEntity<List<ItemCatalogoDTO>> getArticulosDeLista(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.obtenerArticulosDeLista(id));
    }
}
