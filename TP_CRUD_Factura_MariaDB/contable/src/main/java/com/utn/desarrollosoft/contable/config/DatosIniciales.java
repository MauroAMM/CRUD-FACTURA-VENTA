package com.utn.desarrollosoft.contable.config;

import com.utn.desarrollosoft.contable.entidades.Articulo;
import com.utn.desarrollosoft.contable.entidades.Cliente;
import com.utn.desarrollosoft.contable.entidades.CondicionIva;
import com.utn.desarrollosoft.contable.entidades.ListaPrecio;
import com.utn.desarrollosoft.contable.entidades.ListaPrecioArticulo;
import com.utn.desarrollosoft.contable.entidades.PuntoVenta;
import com.utn.desarrollosoft.contable.entidades.TipoMoneda;
import com.utn.desarrollosoft.contable.entidades.dto.FacturaDetalleRequestDTO;
import com.utn.desarrollosoft.contable.entidades.dto.FacturaRequestDTO;
import com.utn.desarrollosoft.contable.repositorios.ArticuloRepositorio;
import com.utn.desarrollosoft.contable.repositorios.ClienteRepositorio;
import com.utn.desarrollosoft.contable.repositorios.CondicionIvaRepositorio;
import com.utn.desarrollosoft.contable.repositorios.ListaPrecioArticuloRepositorio;
import com.utn.desarrollosoft.contable.repositorios.ListaPrecioRepositorio;
import com.utn.desarrollosoft.contable.repositorios.PuntoVentaRepositorio;
import com.utn.desarrollosoft.contable.repositorios.TipoMonedaRepositorio;
import com.utn.desarrollosoft.contable.servicios.FacturaService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Carga datos de ejemplo la PRIMERA vez que se arranca contra una base vacía
 * (clientes, condiciones de IVA, puntos de venta, monedas, artículos y 3 facturas).
 * Si la tabla de clientes ya tiene datos, no los toca: no pisa lo que haya en la base.
 * Además, siempre se asegura de que exista la "Lista mayorista" (para bases creadas antes de que
 * existiera esa lista).
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final ClienteRepositorio clienteRepositorio;
    private final CondicionIvaRepositorio condicionIvaRepositorio;
    private final PuntoVentaRepositorio puntoVentaRepositorio;
    private final TipoMonedaRepositorio tipoMonedaRepositorio;
    private final ArticuloRepositorio articuloRepositorio;
    private final ListaPrecioRepositorio listaPrecioRepositorio;
    private final ListaPrecioArticuloRepositorio listaPrecioArticuloRepositorio;
    private final FacturaService facturaService;

    public DatosIniciales(ClienteRepositorio clienteRepositorio,
                          CondicionIvaRepositorio condicionIvaRepositorio,
                          PuntoVentaRepositorio puntoVentaRepositorio,
                          TipoMonedaRepositorio tipoMonedaRepositorio,
                          ArticuloRepositorio articuloRepositorio,
                          ListaPrecioRepositorio listaPrecioRepositorio,
                          ListaPrecioArticuloRepositorio listaPrecioArticuloRepositorio,
                          FacturaService facturaService) {
        this.clienteRepositorio = clienteRepositorio;
        this.condicionIvaRepositorio = condicionIvaRepositorio;
        this.puntoVentaRepositorio = puntoVentaRepositorio;
        this.tipoMonedaRepositorio = tipoMonedaRepositorio;
        this.articuloRepositorio = articuloRepositorio;
        this.listaPrecioRepositorio = listaPrecioRepositorio;
        this.listaPrecioArticuloRepositorio = listaPrecioArticuloRepositorio;
        this.facturaService = facturaService;
    }

    @Override
    public void run(String... args) {
        if (clienteRepositorio.count() == 0) {
            cargarDatosBase();
        }
        asegurarListaMayorista();
    }

    private void cargarDatosBase() {
        Cliente consumidorFinal = cliente("20-00000000-1", "Consumidor Final");
        Cliente juan = cliente("20-12345678-9", "Juan Pérez");
        Cliente maria = cliente("27-23456789-4", "María González");

        CondicionIva responsableInscripto = condicionIva(1, "IVA Responsable Inscripto");
        CondicionIva monotributo = condicionIva(6, "Monotributo");
        condicionIva(5, "Consumidor Final");
        condicionIva(4, "IVA Sujeto Exento");

        PuntoVenta central = puntoVenta(1, "Sucursal Central");
        PuntoVenta norte = puntoVenta(2, "Sucursal Norte");

        TipoMoneda pesos = tipoMoneda("PES", "Peso Argentino", "$");
        tipoMoneda("DOL", "Dólar Estadounidense", "US$");

        ListaPrecio listaGeneral = new ListaPrecio();
        listaGeneral.setCodigo("LP01");
        listaGeneral.setDenominacion("Lista general");
        listaGeneral = listaPrecioRepositorio.save(listaGeneral);

        Articulo a = articulo(listaGeneral, "A001", "Producto A", 500.0);
        Articulo b = articulo(listaGeneral, "B002", "Producto B", 500.0);
        Articulo c = articulo(listaGeneral, "C003", "Producto C", 750.0);
        Articulo d = articulo(listaGeneral, "D004", "Producto D", 1000.5);
        Articulo e = articulo(listaGeneral, "E005", "Producto E", 1000.0);
        Articulo f = articulo(listaGeneral, "F006", "Producto F", 3750.0);

        // Tres facturas de ejemplo (se crean con el servicio: pasan por las mismas reglas que la API)
        facturaService.crear(factura(LocalDate.of(2026, 3, 15), consumidorFinal, responsableInscripto,
                central, pesos, renglon(a, 2), renglon(b, 1)));
        facturaService.crear(factura(LocalDate.of(2026, 3, 16), juan, responsableInscripto,
                central, pesos, renglon(c, 3), renglon(d, 1)));
        facturaService.crear(factura(LocalDate.of(2026, 3, 17), maria, monotributo,
                norte, pesos, renglon(e, 5), renglon(f, 1)));
    }

    /**
     * Crea la "Lista mayorista" (LP02) si no existe: mismos artículos que la lista general
     * pero con 10% de descuento. Sirve para ver que el precio cambia según la lista elegida.
     */
    private void asegurarListaMayorista() {
        if (listaPrecioRepositorio.findByCodigo("LP02").isPresent()) {
            return;
        }
        Optional<ListaPrecio> general = listaPrecioRepositorio.findByCodigo("LP01");
        if (!general.isPresent()) {
            return; // no hay lista base de la cual copiar los artículos
        }
        ListaPrecio mayorista = new ListaPrecio();
        mayorista.setCodigo("LP02");
        mayorista.setDenominacion("Lista mayorista");
        mayorista = listaPrecioRepositorio.save(mayorista);

        for (ListaPrecioArticulo base : listaPrecioArticuloRepositorio.listarPorLista(general.get().getId())) {
            ListaPrecioArticulo precio = new ListaPrecioArticulo();
            precio.setListaPrecio(mayorista);
            precio.setArticulo(base.getArticulo());
            precio.setPrecioVenta(Math.round(base.getPrecioVenta() * 0.90 * 100.0) / 100.0);
            listaPrecioArticuloRepositorio.save(precio);
        }
    }

    // ------------------------------------------------------------------ helpers

    private Cliente cliente(String cuit, String denominacion) {
        Cliente cliente = new Cliente();
        cliente.setCuitCuil(cuit);
        cliente.setDenominacion(denominacion);
        return clienteRepositorio.save(cliente);
    }

    private CondicionIva condicionIva(int codigoAfip, String denominacion) {
        CondicionIva condicion = new CondicionIva();
        condicion.setCodigoAfip(codigoAfip);
        condicion.setDenominacion(denominacion);
        return condicionIvaRepositorio.save(condicion);
    }

    private PuntoVenta puntoVenta(int numero, String descripcion) {
        PuntoVenta puntoVenta = new PuntoVenta();
        puntoVenta.setNumero(numero);
        puntoVenta.setDescripcion(descripcion);
        return puntoVentaRepositorio.save(puntoVenta);
    }

    private TipoMoneda tipoMoneda(String codigoAfip, String denominacion, String simbolo) {
        TipoMoneda moneda = new TipoMoneda();
        moneda.setCodigoAfip(codigoAfip);
        moneda.setDenominacion(denominacion);
        moneda.setSimbolo(simbolo);
        return tipoMonedaRepositorio.save(moneda);
    }

    private Articulo articulo(ListaPrecio lista, String codigo, String denominacion, double precioVenta) {
        Articulo articulo = new Articulo();
        articulo.setCodigo(codigo);
        articulo.setDenominacion(denominacion);
        articulo = articuloRepositorio.save(articulo);

        ListaPrecioArticulo precio = new ListaPrecioArticulo();
        precio.setListaPrecio(lista);
        precio.setArticulo(articulo);
        precio.setPrecioVenta(precioVenta);
        listaPrecioArticuloRepositorio.save(precio);
        return articulo;
    }

    private FacturaRequestDTO factura(LocalDate fecha, Cliente cliente, CondicionIva condicion,
                                      PuntoVenta puntoVenta, TipoMoneda moneda,
                                      FacturaDetalleRequestDTO... renglones) {
        FacturaRequestDTO request = new FacturaRequestDTO();
        request.setFechaEmision(fecha);
        request.setClienteId(cliente.getId());
        request.setCondicionIvaId(condicion.getId());
        request.setPuntoVentaId(puntoVenta.getId());
        request.setTipoMonedaId(moneda.getId());
        List<FacturaDetalleRequestDTO> lista = new ArrayList<>();
        for (FacturaDetalleRequestDTO renglon : renglones) {
            lista.add(renglon);
        }
        request.setDetalles(lista);
        return request;
    }

    private FacturaDetalleRequestDTO renglon(Articulo articulo, double cantidad) {
        FacturaDetalleRequestDTO detalle = new FacturaDetalleRequestDTO();
        detalle.setArticuloId(articulo.getId());
        detalle.setCantidad(cantidad);
        return detalle;
    }
}
