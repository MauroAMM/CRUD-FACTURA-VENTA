package com.utn.desarrollosoft.contable.servicios;

import com.utn.desarrollosoft.contable.entidades.Cliente;
import com.utn.desarrollosoft.contable.entidades.CondicionIva;
import com.utn.desarrollosoft.contable.entidades.FacturaVenta;
import com.utn.desarrollosoft.contable.entidades.FacturaVentaDetalle;
import com.utn.desarrollosoft.contable.entidades.ListaPrecio;
import com.utn.desarrollosoft.contable.entidades.ListaPrecioArticulo;
import com.utn.desarrollosoft.contable.entidades.PuntoVenta;
import com.utn.desarrollosoft.contable.entidades.TipoMoneda;
import com.utn.desarrollosoft.contable.entidades.dto.FacturaDetalleReporteDTO;
import com.utn.desarrollosoft.contable.entidades.dto.FacturaDetalleRequestDTO;
import com.utn.desarrollosoft.contable.entidades.dto.FacturaReporteDTO;
import com.utn.desarrollosoft.contable.entidades.dto.FacturaRequestDTO;
import com.utn.desarrollosoft.contable.excepciones.RecursoNoEncontradoException;
import com.utn.desarrollosoft.contable.excepciones.ReglaNegocioException;
import com.utn.desarrollosoft.contable.repositorios.ClienteRepositorio;
import com.utn.desarrollosoft.contable.repositorios.CondicionIvaRepositorio;
import com.utn.desarrollosoft.contable.repositorios.FacturaVentaRepositorio;
import com.utn.desarrollosoft.contable.repositorios.ListaPrecioArticuloRepositorio;
import com.utn.desarrollosoft.contable.repositorios.ListaPrecioRepositorio;
import com.utn.desarrollosoft.contable.repositorios.PuntoVentaRepositorio;
import com.utn.desarrollosoft.contable.repositorios.TipoMonedaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class FacturaService {

    public static final String ESTADO_EMITIDA = "EMITIDA";
    public static final String ESTADO_ANULADA = "ANULADA";

    /** Alícuota general de IVA (21%). */
    private static final double ALICUOTA_IVA_GENERAL = 0.21;
    /** Código AFIP de la condición "IVA Sujeto Exento": no se le calcula IVA. */
    private static final int CODIGO_AFIP_EXENTO = 4;

    /** Primer número de comprobante. */
    private static final long NUMERO_INICIAL = 1001L;

    private final FacturaVentaRepositorio facturaRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final CondicionIvaRepositorio condicionIvaRepositorio;
    private final PuntoVentaRepositorio puntoVentaRepositorio;
    private final TipoMonedaRepositorio tipoMonedaRepositorio;
    private final ListaPrecioRepositorio listaPrecioRepositorio;
    private final ListaPrecioArticuloRepositorio listaPrecioArticuloRepositorio;

    public FacturaService(FacturaVentaRepositorio facturaRepositorio,
                          ClienteRepositorio clienteRepositorio,
                          CondicionIvaRepositorio condicionIvaRepositorio,
                          PuntoVentaRepositorio puntoVentaRepositorio,
                          TipoMonedaRepositorio tipoMonedaRepositorio,
                          ListaPrecioRepositorio listaPrecioRepositorio,
                          ListaPrecioArticuloRepositorio listaPrecioArticuloRepositorio) {
        this.facturaRepositorio = facturaRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.condicionIvaRepositorio = condicionIvaRepositorio;
        this.puntoVentaRepositorio = puntoVentaRepositorio;
        this.tipoMonedaRepositorio = tipoMonedaRepositorio;
        this.listaPrecioRepositorio = listaPrecioRepositorio;
        this.listaPrecioArticuloRepositorio = listaPrecioArticuloRepositorio;
    }

    // =====================================================================
    // Consultas
    // =====================================================================

    @Transactional(readOnly = true)
    public List<FacturaReporteDTO> listar(Long numeroFactura, Long clienteId, String clienteDenominacion,
                                          String condicionIva, String estado,
                                          LocalDate fechaDesde, LocalDate fechaHasta) {
        List<FacturaVenta> facturas = facturaRepositorio.buscar(
                numeroFactura,
                clienteId,
                vacioANull(clienteDenominacion),
                vacioANull(condicionIva),
                vacioANull(estado),
                fechaDesde == null ? null : aDate(fechaDesde),
                fechaHasta == null ? null : aDate(fechaHasta));

        List<FacturaReporteDTO> reportes = new ArrayList<>();
        for (FacturaVenta factura : facturas) {
            reportes.add(aReporte(factura));
        }
        return reportes;
    }

    @Transactional(readOnly = true)
    public FacturaReporteDTO obtenerPorId(Long id) {
        return aReporte(buscarFactura(id));
    }

    // =====================================================================
    // Creación
    // =====================================================================

    @Transactional
    public FacturaReporteDTO crear(FacturaRequestDTO request) {
        if (request == null) {
            throw new ReglaNegocioException("El cuerpo de la petición es obligatorio");
        }
        exigir(request.getClienteId(), "clienteId");
        exigir(request.getCondicionIvaId(), "condicionIvaId");
        exigir(request.getPuntoVentaId(), "puntoVentaId");
        exigir(request.getTipoMonedaId(), "tipoMonedaId");
        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new ReglaNegocioException("La factura debe tener al menos un ítem");
        }

        // 1) Buscar las entidades asociadas por ID (404 si alguna no existe)
        PuntoVenta puntoVenta = buscarPuntoVenta(request.getPuntoVentaId());
        CondicionIva condicionIva = buscarCondicionIva(request.getCondicionIvaId());
        TipoMoneda tipoMoneda = buscarTipoMoneda(request.getTipoMonedaId());
        Cliente cliente = buscarCliente(request.getClienteId());
        ListaPrecio listaPrecio = request.getListaPrecioId() != null
                ? buscarListaPrecio(request.getListaPrecioId()) : listaPrecioPorDefecto();
        List<FacturaVentaDetalle> detalles = construirDetalles(request.getDetalles(), listaPrecio);

        // 2) Mapear el DTO a la entidad (recién acá, con todo validado, se consume el número)
        FacturaVenta factura = new FacturaVenta();
        factura.setNumero(siguienteNumero());
        factura.setFechaEmision(aDate(request.getFechaEmision() != null
                ? request.getFechaEmision() : LocalDate.now()));
        factura.setPuntoVenta(puntoVenta);
        factura.setCondicionIva(condicionIva);
        factura.setTipoMoneda(tipoMoneda);
        factura.setCliente(cliente);
        factura.setListaPrecio(listaPrecio);
        factura.setEstado(ESTADO_EMITIDA);
        factura.setObservaciones(limpiarTexto(request.getObservaciones()));
        factura.setFechaAlta(new Date());
        factura.setImporteCobrado(0.0);
        agregarDetalles(factura, detalles);

        // 3) Calcular neto, IVA y total, y guardar (los detalles se persisten en cascada)
        recalcularImportes(factura);
        return aReporte(facturaRepositorio.saveAndFlush(factura));
    }

    // =====================================================================
    // Actualización (parcial) y anulación
    // =====================================================================

    @Transactional
    public FacturaReporteDTO actualizar(Long id, FacturaRequestDTO request) {
        if (request == null) {
            throw new ReglaNegocioException("El cuerpo de la petición es obligatorio");
        }
        FacturaVenta factura = buscarFactura(id);
        if (ESTADO_ANULADA.equals(factura.getEstado())) {
            throw new ReglaNegocioException("La factura " + factura.getNumero()
                    + " está anulada y no puede modificarse");
        }

        // 1) Validar y resolver todo ANTES de modificar la entidad:
        //    si algo falla, la factura queda exactamente como estaba.
        PuntoVenta puntoVenta = request.getPuntoVentaId() == null
                ? null : buscarPuntoVenta(request.getPuntoVentaId());
        CondicionIva condicionIva = request.getCondicionIvaId() == null
                ? null : buscarCondicionIva(request.getCondicionIvaId());
        TipoMoneda tipoMoneda = request.getTipoMonedaId() == null
                ? null : buscarTipoMoneda(request.getTipoMonedaId());
        Cliente cliente = request.getClienteId() == null
                ? null : buscarCliente(request.getClienteId());

        ListaPrecio nuevaLista = request.getListaPrecioId() == null
                ? null : buscarListaPrecio(request.getListaPrecioId());
        ListaPrecio listaActual = listaActual(factura);
        boolean cambiaLista = nuevaLista != null
                && (listaActual == null || !nuevaLista.getId().equals(listaActual.getId()));
        if (cambiaLista && request.getDetalles() == null) {
            throw new ReglaNegocioException(
                    "Para cambiar la lista de precios hay que enviar también los ítems (detalles)");
        }

        List<FacturaVentaDetalle> nuevosDetalles = null;
        ListaPrecio listaParaItems = null;
        if (request.getDetalles() != null) {
            if (request.getDetalles().isEmpty()) {
                throw new ReglaNegocioException("La factura debe tener al menos un ítem");
            }
            listaParaItems = nuevaLista != null ? nuevaLista
                    : (listaActual != null ? listaActual : listaPrecioPorDefecto());
            nuevosDetalles = construirDetalles(request.getDetalles(), listaParaItems);
        }
        String nuevoEstado = normalizarEstado(request.getEstado());

        // 2) Aplicar cambios a la cabecera y a los ítems
        if (puntoVenta != null) {
            factura.setPuntoVenta(puntoVenta);
        }
        if (condicionIva != null) {
            factura.setCondicionIva(condicionIva);
        }
        if (tipoMoneda != null) {
            factura.setTipoMoneda(tipoMoneda);
        }
        if (cliente != null) {
            factura.setCliente(cliente);
        }
        if (request.getFechaEmision() != null) {
            factura.setFechaEmision(aDate(request.getFechaEmision()));
        }
        if (request.getObservaciones() != null) {
            factura.setObservaciones(limpiarTexto(request.getObservaciones()));
        }
        if (nuevosDetalles != null) {
            // clear() + add(): con orphanRemoval, JPA borra los renglones viejos e inserta los nuevos
            factura.getDetalles().clear();
            agregarDetalles(factura, nuevosDetalles);
            factura.setListaPrecio(listaParaItems);
        }
        recalcularImportes(factura);

        // 3) Anulación lógica: cambia el estado y registra la fecha de anulación
        if (ESTADO_ANULADA.equals(nuevoEstado)) {
            factura.setEstado(ESTADO_ANULADA);
            factura.setFechaAnulacion(new Date());
        }
        factura.setFechaModificacion(new Date());

        return aReporte(facturaRepositorio.saveAndFlush(factura));
    }

    // =====================================================================
    // Eliminación física
    // =====================================================================

    @Transactional
    public void eliminar(Long id) {
        if (!facturaRepositorio.existsById(id)) {
            throw new RecursoNoEncontradoException("Factura", id);
        }
        // Los detalles se borran en cascada junto con la cabecera
        facturaRepositorio.deleteById(id);
    }

    // =====================================================================
    // Búsquedas de entidades asociadas (lanzan 404 si no existen)
    // =====================================================================

    private FacturaVenta buscarFactura(Long id) {
        return facturaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura", id));
    }

    private PuntoVenta buscarPuntoVenta(Long id) {
        return puntoVentaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Punto de venta", id));
    }

    private CondicionIva buscarCondicionIva(Long id) {
        return condicionIvaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Condición de IVA", id));
    }

    private TipoMoneda buscarTipoMoneda(Long id) {
        return tipoMonedaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de moneda", id));
    }

    private ListaPrecio buscarListaPrecio(Long id) {
        return listaPrecioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lista de precios", id));
    }

    /** Lista que se usa cuando la petición no indica ninguna: la de menor ID (la general). */
    private ListaPrecio listaPrecioPorDefecto() {
        return listaPrecioRepositorio.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ReglaNegocioException("No hay listas de precios cargadas"));
    }

    /**
     * Lista con la que se armó la factura. Las facturas anteriores a esta columna no la tienen
     * guardada: en ese caso se deduce de los ítems.
     */
    private ListaPrecio listaActual(FacturaVenta factura) {
        if (factura.getListaPrecio() != null) {
            return factura.getListaPrecio();
        }
        for (FacturaVentaDetalle detalle : factura.getDetalles()) {
            if (detalle.getListaPrecioArticulo() != null
                    && detalle.getListaPrecioArticulo().getListaPrecio() != null) {
                return detalle.getListaPrecioArticulo().getListaPrecio();
            }
        }
        return null;
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));
    }

    // =====================================================================
    // Construcción de ítems y cálculo de importes
    // =====================================================================

    /** Valida cada renglón del request y lo convierte en entidad (sin importes todavía). */
    private List<FacturaVentaDetalle> construirDetalles(List<FacturaDetalleRequestDTO> items,
                                                        ListaPrecio listaPrecio) {
        List<FacturaVentaDetalle> detalles = new ArrayList<>();
        int renglon = 1;
        for (FacturaDetalleRequestDTO item : items) {
            String ref = "Ítem " + renglon + ": ";
            if (item == null || item.getArticuloId() == null) {
                throw new ReglaNegocioException(ref + "articuloId es obligatorio");
            }
            final Long articuloId = item.getArticuloId();
            // El precio se toma de la lista elegida: el mismo artículo puede costar distinto en cada lista
            ListaPrecioArticulo precio = listaPrecioArticuloRepositorio
                    .findFirstByListaPrecioIdAndArticuloIdOrderByIdAsc(listaPrecio.getId(), articuloId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("El artículo con id " + articuloId
                            + " no existe o no tiene precio en la lista '" + listaPrecio.getDenominacion() + "'"));

            double cantidad = item.getCantidad() != null ? item.getCantidad() : 0.0;
            if (cantidad <= 0) {
                throw new ReglaNegocioException(ref + "la cantidad debe ser mayor a 0");
            }
            double precioUnitario = item.getPrecioUnitario() != null
                    ? item.getPrecioUnitario() : precio.getPrecioVenta();
            if (precioUnitario < 0) {
                throw new ReglaNegocioException(ref + "el precio unitario no puede ser negativo");
            }
            double bonificacion = item.getPorcentajeBonificacion() != null
                    ? item.getPorcentajeBonificacion() : 0.0;
            if (bonificacion < 0 || bonificacion > 100) {
                throw new ReglaNegocioException(ref + "la bonificación debe estar entre 0 y 100");
            }

            FacturaVentaDetalle detalle = new FacturaVentaDetalle();
            detalle.setListaPrecioArticulo(precio);
            detalle.setDescripcion(esVacio(item.getDescripcion())
                    ? precio.getArticulo().getDenominacion() : item.getDescripcion().trim());
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setPorcentajeBonificacion(bonificacion);
            detalles.add(detalle);
            renglon++;
        }
        return detalles;
    }

    /**
     * Recalcula neto, IVA y subtotal de cada ítem y el total de la cabecera:
     *   neto     = cantidad x precio x (1 - bonificación/100)
     *   IVA      = neto x alícuota (21%, o 0% si la condición es Exento)
     *   subtotal = neto + IVA
     *   total    = suma de los subtotales
     */
    private void recalcularImportes(FacturaVenta factura) {
        double alicuota = alicuotaIva(factura.getCondicionIva());
        double total = 0.0;
        for (FacturaVentaDetalle detalle : factura.getDetalles()) {
            double neto = redondear(detalle.getCantidad() * detalle.getPrecioUnitario()
                    * (1 - detalle.getPorcentajeBonificacion() / 100.0));
            double iva = redondear(neto * alicuota);
            detalle.setImporteNeto(neto);
            detalle.setImporteIva(iva);
            detalle.setImporteSubtotal(redondear(neto + iva));
            total += detalle.getImporteSubtotal();
        }
        factura.setImporteTotal(redondear(total));
        factura.setImporteSaldo(redondear(factura.getImporteTotal() - factura.getImporteCobrado()));
    }

    /** Enlaza cada renglón con su factura (el JPA los guarda en cascada junto con ella). */
    private void agregarDetalles(FacturaVenta factura, List<FacturaVentaDetalle> detalles) {
        for (FacturaVentaDetalle detalle : detalles) {
            detalle.setFactura(factura);
            factura.getDetalles().add(detalle);
        }
    }

    /** Próximo número de comprobante: el último emitido + 1 (o NUMERO_INICIAL si no hay facturas). */
    private Long siguienteNumero() {
        Long ultimo = facturaRepositorio.obtenerUltimoNumero();
        return ultimo == null ? NUMERO_INICIAL : ultimo + 1;
    }

    private double alicuotaIva(CondicionIva condicion) {
        if (condicion != null && condicion.getCodigoAfip() == CODIGO_AFIP_EXENTO) {
            return 0.0;
        }
        return ALICUOTA_IVA_GENERAL;
    }

    // =====================================================================
    // Mapeo entidad -> DTO de salida
    // =====================================================================

    private FacturaReporteDTO aReporte(FacturaVenta factura) {
        FacturaReporteDTO dto = new FacturaReporteDTO();
        dto.setId(factura.getId());
        dto.setNumeroFactura(factura.getNumero());
        dto.setFechaEmision(aLocalDate(factura.getFechaEmision()));
        dto.setClienteId(factura.getCliente().getId());
        dto.setClienteDenominacion(factura.getCliente().getDenominacion());
        dto.setCondicionIvaId(factura.getCondicionIva().getId());
        dto.setCondicionIva(factura.getCondicionIva().getDenominacion());
        dto.setPuntoVentaId(factura.getPuntoVenta().getId());
        dto.setPuntoVentaDescripcion(factura.getPuntoVenta().getDescripcion());
        dto.setTipoMonedaId(factura.getTipoMoneda().getId());
        dto.setTipoMoneda(factura.getTipoMoneda().getDenominacion());
        ListaPrecio lista = listaActual(factura);
        dto.setListaPrecioId(lista == null ? null : lista.getId());
        dto.setEstado(factura.getEstado());
        dto.setFechaAnulacion(factura.getFechaAnulacion());
        dto.setObservaciones(factura.getObservaciones());

        double neto = 0.0;
        double iva = 0.0;
        List<FacturaDetalleReporteDTO> detallesDto = new ArrayList<>();
        for (FacturaVentaDetalle detalle : factura.getDetalles()) {
            FacturaDetalleReporteDTO d = new FacturaDetalleReporteDTO();
            d.setId(detalle.getId());
            d.setArticuloId(detalle.getListaPrecioArticulo().getArticulo().getId());
            d.setDescripcion(detalle.getDescripcion());
            d.setCantidad(detalle.getCantidad());
            d.setPrecioUnitario(detalle.getPrecioUnitario());
            d.setPorcentajeBonificacion(detalle.getPorcentajeBonificacion());
            d.setImporteNeto(detalle.getImporteNeto());
            d.setImporteIva(detalle.getImporteIva());
            d.setImporteSubtotal(detalle.getImporteSubtotal());
            detallesDto.add(d);
            neto += detalle.getImporteNeto();
            iva += detalle.getImporteIva();
        }
        dto.setDetalles(detallesDto);
        dto.setImporteNeto(redondear(neto));
        dto.setImporteIva(redondear(iva));
        dto.setImporteTotal(factura.getImporteTotal());
        return dto;
    }

    // =====================================================================
    // Utilidades
    // =====================================================================

    private void exigir(Long valor, String campo) {
        if (valor == null) {
            throw new ReglaNegocioException("El campo " + campo + " es obligatorio");
        }
    }

    /** Devuelve null si no se informó estado; si se informó, debe ser EMITIDA o ANULADA. */
    private String normalizarEstado(String estado) {
        if (esVacio(estado)) {
            return null;
        }
        String normalizado = estado.trim().toUpperCase();
        if (!ESTADO_EMITIDA.equals(normalizado) && !ESTADO_ANULADA.equals(normalizado)) {
            throw new ReglaNegocioException("Estado inválido: '" + estado
                    + "'. Valores permitidos: " + ESTADO_EMITIDA + ", " + ESTADO_ANULADA);
        }
        return normalizado;
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    private String vacioANull(String texto) {
        return esVacio(texto) ? null : texto.trim();
    }

    private String limpiarTexto(String texto) {
        return esVacio(texto) ? null : texto.trim();
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private Date aDate(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Al leer una columna DATE, la base devuelve un java.sql.Date, que NO admite toInstant()
     * (lanza UnsupportedOperationException). Por eso se copia primero a un java.util.Date común.
     */
    private LocalDate aLocalDate(Date fecha) {
        return new Date(fecha.getTime()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
