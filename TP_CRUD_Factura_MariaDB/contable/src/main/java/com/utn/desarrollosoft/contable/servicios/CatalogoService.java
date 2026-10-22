package com.utn.desarrollosoft.contable.servicios;

import com.utn.desarrollosoft.contable.entidades.ListaPrecio;
import com.utn.desarrollosoft.contable.entidades.CondicionIva;
import com.utn.desarrollosoft.contable.entidades.ListaPrecioArticulo;
import com.utn.desarrollosoft.contable.entidades.PuntoVenta;
import com.utn.desarrollosoft.contable.entidades.TipoMoneda;
import com.utn.desarrollosoft.contable.entidades.dto.CatalogosDTO;
import com.utn.desarrollosoft.contable.entidades.dto.ItemCatalogoDTO;
import com.utn.desarrollosoft.contable.repositorios.CondicionIvaRepositorio;
import com.utn.desarrollosoft.contable.repositorios.ListaPrecioArticuloRepositorio;
import com.utn.desarrollosoft.contable.repositorios.ListaPrecioRepositorio;
import com.utn.desarrollosoft.contable.repositorios.PuntoVentaRepositorio;
import com.utn.desarrollosoft.contable.repositorios.TipoMonedaRepositorio;
import com.utn.desarrollosoft.contable.excepciones.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Arma los datos maestros (leídos de la base) que necesita el frontend para llenar sus combos. */
@Service
public class CatalogoService {

    private final CondicionIvaRepositorio condicionIvaRepositorio;
    private final PuntoVentaRepositorio puntoVentaRepositorio;
    private final TipoMonedaRepositorio tipoMonedaRepositorio;
    private final ListaPrecioRepositorio listaPrecioRepositorio;
    private final ListaPrecioArticuloRepositorio listaPrecioArticuloRepositorio;

    public CatalogoService(CondicionIvaRepositorio condicionIvaRepositorio,
                           PuntoVentaRepositorio puntoVentaRepositorio,
                           TipoMonedaRepositorio tipoMonedaRepositorio,
                           ListaPrecioRepositorio listaPrecioRepositorio,
                           ListaPrecioArticuloRepositorio listaPrecioArticuloRepositorio) {
        this.condicionIvaRepositorio = condicionIvaRepositorio;
        this.puntoVentaRepositorio = puntoVentaRepositorio;
        this.tipoMonedaRepositorio = tipoMonedaRepositorio;
        this.listaPrecioRepositorio = listaPrecioRepositorio;
        this.listaPrecioArticuloRepositorio = listaPrecioArticuloRepositorio;
    }

    @Transactional(readOnly = true)
    public CatalogosDTO obtenerCatalogos() {
        CatalogosDTO catalogos = new CatalogosDTO();

        List<CondicionIva> listaCondiciones = new ArrayList<>(condicionIvaRepositorio.findAll());
        listaCondiciones.sort(Comparator.comparing(CondicionIva::getId));
        List<ItemCatalogoDTO> condiciones = new ArrayList<>();
        for (CondicionIva c : listaCondiciones) {
            condiciones.add(new ItemCatalogoDTO(c.getId(), c.getDenominacion(), null));
        }
        catalogos.setCondicionesIva(condiciones);

        List<PuntoVenta> listaPuntos = new ArrayList<>(puntoVentaRepositorio.findAll());
        listaPuntos.sort(Comparator.comparing(PuntoVenta::getId));
        List<ItemCatalogoDTO> puntos = new ArrayList<>();
        for (PuntoVenta p : listaPuntos) {
            puntos.add(new ItemCatalogoDTO(p.getId(), p.getDescripcion(), null));
        }
        catalogos.setPuntosVenta(puntos);

        List<TipoMoneda> listaMonedas = new ArrayList<>(tipoMonedaRepositorio.findAll());
        listaMonedas.sort(Comparator.comparing(TipoMoneda::getId));
        List<ItemCatalogoDTO> monedas = new ArrayList<>();
        for (TipoMoneda m : listaMonedas) {
            monedas.add(new ItemCatalogoDTO(m.getId(), m.getDenominacion(), null));
        }
        catalogos.setTiposMoneda(monedas);

        List<ListaPrecio> listas = new ArrayList<>(listaPrecioRepositorio.findAll());
        listas.sort(Comparator.comparing(ListaPrecio::getId));
        List<ItemCatalogoDTO> listasPrecio = new ArrayList<>();
        for (ListaPrecio l : listas) {
            listasPrecio.add(new ItemCatalogoDTO(l.getId(), l.getDenominacion(), null));
        }
        catalogos.setListasPrecio(listasPrecio);

        return catalogos;
    }

    /** Artículos de una lista de precios, con el precio que tienen EN esa lista. */
    @Transactional(readOnly = true)
    public List<ItemCatalogoDTO> obtenerArticulosDeLista(Long listaPrecioId) {
        if (!listaPrecioRepositorio.existsById(listaPrecioId)) {
            throw new RecursoNoEncontradoException("Lista de precios", listaPrecioId);
        }
        List<ItemCatalogoDTO> articulos = new ArrayList<>();
        for (ListaPrecioArticulo p : listaPrecioArticuloRepositorio.listarPorLista(listaPrecioId)) {
            articulos.add(new ItemCatalogoDTO(p.getArticulo().getId(),
                    p.getArticulo().getCodigo() + " - " + p.getArticulo().getDenominacion(),
                    p.getPrecioVenta()));
        }
        return articulos;
    }
}
