var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
import { CatalogoService } from './CatalogoService.js';
import { ClienteService } from './ClienteService.js';
import { FacturaService } from './FacturaService.js';
import { ESTADO_ANULADA } from './models/factura.dto.js';
const facturaService = new FacturaService();
const catalogoService = new CatalogoService();
const clienteService = new ClienteService();
let itemsEnMemoria = [];
let catalogos = null;
/** Artículos de la lista de precios elegida (con el precio que tienen en esa lista). */
let articulosDeLista = [];
let numeroDeCargaArticulos = 0;
/** Cliente elegido en el explorador para la factura que se está cargando / editando. */
let clienteDeFactura = null;
/** Cliente elegido en el explorador para filtrar el listado. */
let clienteDeFiltro = null;
/** Si hay una factura en edición; null cuando el formulario está en modo "nueva factura". */
let facturaEnEdicion = null;
// Explorador de clientes
let alSeleccionarCliente = null;
let temporizadorBusqueda;
let numeroDeBusqueda = 0;
// ---------------------------------------------------------------------------
// Acceso al DOM
// ---------------------------------------------------------------------------
function porId(id) {
    const elemento = document.getElementById(id);
    if (!elemento) {
        throw new Error('No se encontró el elemento #' + id + ' en el HTML');
    }
    return elemento;
}
const contenedorMensajes = porId('mensajes');
const btnNuevaFactura = porId('btn-nueva-factura');
// Sección 1: filtros
const filtrosForm = porId('filtros-form');
const filtroNumero = porId('filtro-numero');
const filtroCliente = porId('filtro-cliente');
const btnFiltroBuscarCliente = porId('btn-filtro-buscar-cliente');
const btnFiltroQuitarCliente = porId('btn-filtro-quitar-cliente');
const filtroCondicion = porId('filtro-condicion');
const filtroEstado = porId('filtro-estado');
const filtroFechaDesde = porId('filtro-fecha-desde');
const filtroFechaHasta = porId('filtro-fecha-hasta');
const btnLimpiarFiltros = porId('btn-limpiar');
// Sección 2: alta / edición
const cardFactura = porId('card-factura');
const tituloFactura = porId('titulo-factura');
const altaForm = porId('alta-form');
const altaFecha = porId('alta-fecha');
const altaCliente = porId('alta-cliente');
const btnAltaBuscarCliente = porId('btn-alta-buscar-cliente');
const altaCondicion = porId('alta-condicion');
const altaPuntoVenta = porId('alta-punto-venta');
const altaMoneda = porId('alta-moneda');
const altaListaPrecio = porId('alta-lista-precio');
const listaBloqueada = porId('lista-bloqueada');
const altaObservaciones = porId('alta-observaciones');
const itemArticulo = porId('item-articulo');
const itemCantidad = porId('item-cantidad');
const itemPrecio = porId('item-precio');
const itemBonificacion = porId('item-bonificacion');
const btnAgregarItem = porId('btn-agregar-item');
const itemsTbody = porId('items-tbody');
const itemsTotal = porId('items-total');
const btnGuardarFactura = porId('btn-guardar-factura');
const btnCancelarFactura = porId('btn-cancelar-factura');
// Sección 3: listado
const facturasTbody = porId('facturas-tbody');
const COLUMNAS_LISTADO = 11;
// Explorador de clientes (ventana emergente)
const exploradorModal = porId('explorador-clientes');
const exploradorTexto = porId('explorador-texto');
const exploradorBuscar = porId('explorador-buscar');
const exploradorEstado = porId('explorador-estado');
const exploradorTbody = porId('explorador-tbody');
const exploradorCerrar = porId('explorador-cerrar');
const exploradorCancelar = porId('explorador-cancelar');
// ---------------------------------------------------------------------------
// Utilidades de formato y de DOM
// ---------------------------------------------------------------------------
function formatearMoneda(valor) {
    const numero = valor === null || valor === undefined ? 0 : valor;
    return numero.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
/** "2026-03-15" -> "15/03/2026" (sin pasar por Date, para evitar corrimientos de zona horaria). */
function formatearFecha(fecha) {
    if (!fecha) {
        return '-';
    }
    const partes = fecha.substring(0, 10).split('-');
    return partes.length === 3 ? partes[2] + '/' + partes[1] + '/' + partes[0] : fecha;
}
function fechaDeHoy() {
    const hoy = new Date();
    const mes = ('0' + (hoy.getMonth() + 1)).slice(-2);
    const dia = ('0' + hoy.getDate()).slice(-2);
    return hoy.getFullYear() + '-' + mes + '-' + dia;
}
function mensajeDeError(error) {
    return error instanceof Error ? error.message : String(error);
}
function mostrar(elemento) {
    elemento.classList.remove('d-none');
}
function ocultar(elemento) {
    elemento.classList.add('d-none');
}
/** Texto que se muestra en los campos de cliente: "Juan Pérez (20-12345678-9)". */
function textoDeCliente(cliente) {
    if (!cliente) {
        return '';
    }
    return cliente.cuitCuil ? cliente.denominacion + ' (' + cliente.cuitCuil + ')' : cliente.denominacion;
}
function mostrarMensaje(tipo, texto) {
    const alerta = document.createElement('div');
    alerta.className = 'alert alert-' + tipo + ' alert-dismissible fade show';
    alerta.setAttribute('role', 'alert');
    alerta.textContent = texto;
    const cerrar = document.createElement('button');
    cerrar.type = 'button';
    cerrar.className = 'btn-close';
    cerrar.setAttribute('data-bs-dismiss', 'alert');
    cerrar.setAttribute('aria-label', 'Cerrar');
    alerta.appendChild(cerrar);
    contenedorMensajes.textContent = '';
    contenedorMensajes.appendChild(alerta);
    window.scrollTo({ top: 0, behavior: 'smooth' });
}
// Se crean los nodos con textContent (no innerHTML) para que los datos nunca se interpreten como HTML.
function crearCelda(texto, clase) {
    const celda = document.createElement('td');
    celda.textContent = texto;
    if (clase) {
        celda.className = clase;
    }
    return celda;
}
function crearBoton(texto, clases, onClick) {
    const boton = document.createElement('button');
    boton.type = 'button';
    boton.textContent = texto;
    boton.className = 'btn btn-sm ' + clases;
    boton.addEventListener('click', onClick);
    return boton;
}
function crearFilaMensaje(texto, columnas, clase) {
    const fila = document.createElement('tr');
    const celda = crearCelda(texto, clase);
    celda.colSpan = columnas;
    fila.appendChild(celda);
    return fila;
}
// ---------------------------------------------------------------------------
// Combos (cargados desde GET /api/catalogos)
// ---------------------------------------------------------------------------
function llenarSelect(select, items, textoVacio, usarDescripcionComoValor = false) {
    select.textContent = '';
    if (textoVacio !== null) {
        const vacia = document.createElement('option');
        vacia.value = '';
        vacia.textContent = textoVacio;
        select.appendChild(vacia);
    }
    for (const item of items) {
        const opcion = document.createElement('option');
        opcion.value = usarDescripcionComoValor ? item.descripcion : String(item.id);
        opcion.textContent = item.descripcion;
        select.appendChild(opcion);
    }
}
function poblarCombos(datos) {
    llenarSelect(altaCondicion, datos.condicionesIva, 'Seleccionar...');
    llenarSelect(altaPuntoVenta, datos.puntosVenta, 'Seleccionar...');
    llenarSelect(altaMoneda, datos.tiposMoneda, 'Seleccionar...');
    llenarSelect(altaListaPrecio, datos.listasPrecio, null); // sin opción vacía: siempre hay una lista elegida
    // El filtro por condición de IVA se hace por nombre, no por ID
    llenarSelect(filtroCondicion, datos.condicionesIva, 'Todas', true);
}
/** Moneda y lista de precios por defecto (la primera de cada una). */
function aplicarValoresPorDefecto() {
    if (catalogos && catalogos.tiposMoneda.length > 0) {
        altaMoneda.value = String(catalogos.tiposMoneda[0].id);
    }
    if (catalogos && catalogos.listasPrecio.length > 0) {
        altaListaPrecio.value = String(catalogos.listasPrecio[0].id);
    }
}
function listaSeleccionadaId() {
    return parseInt(altaListaPrecio.value, 10);
}
/** Carga los artículos de la lista de precios elegida (cada lista puede tener precios distintos). */
function cargarArticulosDeLista(listaId) {
    return __awaiter(this, void 0, void 0, function* () {
        numeroDeCargaArticulos++;
        const miCarga = numeroDeCargaArticulos;
        // Mientras llegan los artículos de la lista nueva no se muestran los de la anterior (tienen otros precios)
        articulosDeLista = [];
        llenarSelect(itemArticulo, articulosDeLista, 'Seleccionar artículo...');
        itemPrecio.value = '';
        if (Number.isNaN(listaId)) {
            return;
        }
        try {
            const articulos = yield catalogoService.obtenerArticulosDeLista(listaId);
            if (miCarga !== numeroDeCargaArticulos) {
                return; // llegó tarde: mientras tanto se eligió otra lista
            }
            articulosDeLista = articulos;
            llenarSelect(itemArticulo, articulosDeLista, 'Seleccionar artículo...');
        }
        catch (error) {
            if (miCarga === numeroDeCargaArticulos) {
                mostrarMensaje('danger', 'No se pudieron cargar los artículos de la lista de precios: ' + mensajeDeError(error));
            }
        }
    });
}
function buscarArticulo(id) {
    return articulosDeLista.find(a => a.id === id);
}
// ---------------------------------------------------------------------------
// Explorador de clientes (ventana con búsqueda; se abre con la lupa)
// ---------------------------------------------------------------------------
function exploradorAbierto() {
    return exploradorModal.classList.contains('show');
}
function abrirExplorador(alElegir) {
    alSeleccionarCliente = alElegir;
    exploradorTexto.value = '';
    exploradorTbody.textContent = '';
    exploradorEstado.textContent = '';
    exploradorModal.style.display = 'block';
    exploradorModal.style.backgroundColor = 'rgba(0, 0, 0, 0.5)';
    exploradorModal.classList.add('show');
    exploradorModal.removeAttribute('aria-hidden');
    document.body.classList.add('modal-open');
    exploradorTexto.focus();
    void buscarClientes();
}
function cerrarExplorador() {
    window.clearTimeout(temporizadorBusqueda);
    exploradorModal.classList.remove('show');
    exploradorModal.style.display = 'none';
    exploradorModal.setAttribute('aria-hidden', 'true');
    document.body.classList.remove('modal-open');
    alSeleccionarCliente = null;
}
function elegirCliente(cliente) {
    const accion = alSeleccionarCliente;
    cerrarExplorador();
    if (accion) {
        accion(cliente);
    }
}
function renderizarClientes(clientes) {
    exploradorTbody.textContent = '';
    if (clientes.length === 0) {
        exploradorTbody.appendChild(crearFilaMensaje('No se encontraron clientes.', 3, 'text-center text-muted'));
        exploradorEstado.textContent = '';
        return;
    }
    exploradorEstado.textContent = clientes.length === 1 ? '1 cliente' : clientes.length + ' clientes';
    clientes.forEach(cliente => {
        const fila = document.createElement('tr');
        fila.appendChild(crearCelda(cliente.cuitCuil ? cliente.cuitCuil : '-'));
        fila.appendChild(crearCelda(cliente.denominacion));
        const celdaAccion = document.createElement('td');
        celdaAccion.className = 'text-end';
        celdaAccion.appendChild(crearBoton('Seleccionar', 'btn-outline-primary', () => elegirCliente(cliente)));
        fila.appendChild(celdaAccion);
        exploradorTbody.appendChild(fila);
    });
}
function buscarClientes() {
    return __awaiter(this, void 0, void 0, function* () {
        // Si el usuario sigue escribiendo, solo vale la respuesta de la última búsqueda
        numeroDeBusqueda++;
        const miBusqueda = numeroDeBusqueda;
        try {
            const clientes = yield clienteService.buscarClientes(exploradorTexto.value);
            if (miBusqueda === numeroDeBusqueda) {
                renderizarClientes(clientes);
            }
        }
        catch (error) {
            if (miBusqueda === numeroDeBusqueda) {
                exploradorTbody.textContent = '';
                exploradorTbody.appendChild(crearFilaMensaje('Error al buscar clientes: ' + mensajeDeError(error), 3, 'text-center text-danger'));
            }
        }
    });
}
// ---------------------------------------------------------------------------
// Sección 2: formulario de factura (alta y edición) e ítems en memoria
// ---------------------------------------------------------------------------
function netoEstimado(item) {
    return item.cantidad * item.precioUnitario * (1 - item.porcentajeBonificacion / 100);
}
function renderizarItems() {
    itemsTbody.textContent = '';
    // La lista de precios se bloquea mientras haya ítems: sus precios salieron de esa lista
    const hayItems = itemsEnMemoria.length > 0;
    altaListaPrecio.disabled = hayItems;
    if (hayItems) {
        mostrar(listaBloqueada);
    }
    else {
        ocultar(listaBloqueada);
    }
    if (!hayItems) {
        itemsTbody.appendChild(crearFilaMensaje('Todavía no se agregaron ítems.', 6, 'text-center text-muted'));
        itemsTotal.textContent = formatearMoneda(0);
        return;
    }
    let total = 0;
    itemsEnMemoria.forEach((item, indice) => {
        const neto = netoEstimado(item);
        total += neto;
        const fila = document.createElement('tr');
        fila.appendChild(crearCelda(item.descripcion));
        fila.appendChild(crearCelda(String(item.cantidad), 'text-end'));
        fila.appendChild(crearCelda(formatearMoneda(item.precioUnitario), 'text-end'));
        fila.appendChild(crearCelda(item.porcentajeBonificacion + ' %', 'text-end'));
        fila.appendChild(crearCelda(formatearMoneda(neto), 'text-end'));
        const celdaAccion = document.createElement('td');
        celdaAccion.className = 'text-center';
        celdaAccion.appendChild(crearBoton('Quitar', 'btn-outline-danger', () => {
            itemsEnMemoria.splice(indice, 1);
            renderizarItems();
        }));
        fila.appendChild(celdaAccion);
        itemsTbody.appendChild(fila);
    });
    itemsTotal.textContent = formatearMoneda(total);
}
function limpiarCamposDeItem() {
    itemArticulo.value = '';
    itemCantidad.value = '1';
    itemPrecio.value = '';
    itemBonificacion.value = '0';
}
function agregarItem() {
    var _a;
    const articuloId = parseInt(itemArticulo.value, 10);
    const articulo = Number.isNaN(articuloId) ? undefined : buscarArticulo(articuloId);
    if (!articulo) {
        mostrarMensaje('warning', 'Seleccioná un artículo para agregar el ítem.');
        return;
    }
    const cantidad = parseFloat(itemCantidad.value);
    if (Number.isNaN(cantidad) || cantidad <= 0) {
        mostrarMensaje('warning', 'La cantidad debe ser mayor a 0.');
        return;
    }
    const precio = itemPrecio.value.trim() === '' ? ((_a = articulo.precioVenta) !== null && _a !== void 0 ? _a : 0) : parseFloat(itemPrecio.value);
    if (Number.isNaN(precio) || precio < 0) {
        mostrarMensaje('warning', 'El precio unitario no es válido.');
        return;
    }
    const bonificacion = itemBonificacion.value.trim() === '' ? 0 : parseFloat(itemBonificacion.value);
    if (Number.isNaN(bonificacion) || bonificacion < 0 || bonificacion > 100) {
        mostrarMensaje('warning', 'La bonificación debe estar entre 0 y 100.');
        return;
    }
    itemsEnMemoria.push({
        articuloId: articulo.id,
        descripcion: articulo.descripcion,
        cantidad,
        precioUnitario: precio,
        porcentajeBonificacion: bonificacion
    });
    contenedorMensajes.textContent = '';
    limpiarCamposDeItem();
    renderizarItems();
}
function limpiarFormulario() {
    altaForm.reset();
    altaFecha.value = fechaDeHoy();
    clienteDeFactura = null;
    altaCliente.value = '';
    aplicarValoresPorDefecto();
    itemsEnMemoria = [];
    limpiarCamposDeItem();
    renderizarItems();
}
function mostrarFormulario(titulo, textoBoton) {
    tituloFactura.textContent = titulo;
    btnGuardarFactura.textContent = textoBoton;
    mostrar(cardFactura);
    if (typeof cardFactura.scrollIntoView === 'function') {
        cardFactura.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
}
function cerrarFormulario() {
    ocultar(cardFactura);
    facturaEnEdicion = null;
}
/** Botón "Nueva Factura": muestra el formulario vacío. */
function abrirFormularioAlta() {
    return __awaiter(this, void 0, void 0, function* () {
        facturaEnEdicion = null;
        contenedorMensajes.textContent = '';
        limpiarFormulario();
        mostrarFormulario('2. Nueva factura', 'Emitir factura');
        yield cargarArticulosDeLista(listaSeleccionadaId());
    });
}
/** Botón "Editar" de una fila: muestra el mismo formulario con los datos de la factura. */
function abrirFormularioEdicion(factura) {
    return __awaiter(this, void 0, void 0, function* () {
        facturaEnEdicion = factura;
        contenedorMensajes.textContent = '';
        limpiarFormulario();
        altaFecha.value = factura.fechaEmision.substring(0, 10);
        clienteDeFactura = { id: factura.clienteId, cuitCuil: null, denominacion: factura.clienteDenominacion };
        altaCliente.value = textoDeCliente(clienteDeFactura);
        altaCondicion.value = String(factura.condicionIvaId);
        altaPuntoVenta.value = String(factura.puntoVentaId);
        altaMoneda.value = String(factura.tipoMonedaId);
        if (factura.listaPrecioId !== null) {
            altaListaPrecio.value = String(factura.listaPrecioId);
        }
        altaObservaciones.value = factura.observaciones ? factura.observaciones : '';
        itemsEnMemoria = factura.detalles.map(detalle => ({
            articuloId: detalle.articuloId,
            descripcion: detalle.descripcion,
            descripcionGuardada: detalle.descripcion,
            cantidad: detalle.cantidad,
            precioUnitario: detalle.precioUnitario,
            porcentajeBonificacion: detalle.porcentajeBonificacion
        }));
        renderizarItems();
        mostrarFormulario('Editar factura Nº ' + factura.numeroFactura, 'Guardar cambios');
        yield cargarArticulosDeLista(listaSeleccionadaId());
    });
}
function leerFacturaDesdeFormulario() {
    const observaciones = altaObservaciones.value.trim();
    return {
        fechaEmision: altaFecha.value || undefined,
        clienteId: clienteDeFactura ? clienteDeFactura.id : undefined,
        condicionIvaId: parseInt(altaCondicion.value, 10),
        puntoVentaId: parseInt(altaPuntoVenta.value, 10),
        tipoMonedaId: parseInt(altaMoneda.value, 10),
        listaPrecioId: parseInt(altaListaPrecio.value, 10),
        // Al editar se envía siempre (aunque quede vacía) para poder borrarlas; en el alta solo si hay texto
        observaciones: facturaEnEdicion ? observaciones : (observaciones || undefined),
        detalles: itemsEnMemoria.map(item => {
            const detalle = {
                articuloId: item.articuloId,
                cantidad: item.cantidad,
                precioUnitario: item.precioUnitario,
                porcentajeBonificacion: item.porcentajeBonificacion
            };
            if (item.descripcionGuardada) {
                detalle.descripcion = item.descripcionGuardada;
            }
            return detalle;
        })
    };
}
function guardarFactura() {
    return __awaiter(this, void 0, void 0, function* () {
        const factura = leerFacturaDesdeFormulario();
        if (!clienteDeFactura) {
            mostrarMensaje('warning', 'Seleccioná un cliente con la lupa.');
            return;
        }
        if ([factura.condicionIvaId, factura.puntoVentaId, factura.tipoMonedaId, factura.listaPrecioId]
            .some(valor => valor === undefined || Number.isNaN(valor))) {
            mostrarMensaje('warning', 'Completá condición de IVA, punto de venta, moneda y lista de precios.');
            return;
        }
        if (itemsEnMemoria.length === 0) {
            mostrarMensaje('warning', 'Agregá al menos un ítem a la factura.');
            return;
        }
        const editando = facturaEnEdicion;
        try {
            if (editando) {
                const actualizada = yield facturaService.actualizarFactura(editando.id, factura);
                mostrarMensaje('success', 'Factura Nº ' + actualizada.numeroFactura + ' actualizada. Neto '
                    + formatearMoneda(actualizada.importeNeto) + ' + IVA ' + formatearMoneda(actualizada.importeIva)
                    + ' = Total ' + formatearMoneda(actualizada.importeTotal) + '.');
            }
            else {
                const creada = yield facturaService.crearFactura(factura);
                mostrarMensaje('success', 'Factura Nº ' + creada.numeroFactura + ' emitida. Neto '
                    + formatearMoneda(creada.importeNeto) + ' + IVA ' + formatearMoneda(creada.importeIva)
                    + ' = Total ' + formatearMoneda(creada.importeTotal) + '.');
            }
            cerrarFormulario();
            yield cargarFacturas();
        }
        catch (error) {
            mostrarMensaje('danger', 'No se pudo ' + (editando ? 'guardar los cambios' : 'crear la factura')
                + ': ' + mensajeDeError(error));
        }
    });
}
// ---------------------------------------------------------------------------
// Sección 3: listado y acciones por fila
// ---------------------------------------------------------------------------
function crearFilaDetalle(factura) {
    const fila = document.createElement('tr');
    fila.className = 'fila-detalle';
    const celda = document.createElement('td');
    celda.colSpan = COLUMNAS_LISTADO;
    const tabla = document.createElement('table');
    tabla.className = 'table table-sm table-bordered mb-0 bg-white';
    const cabecera = document.createElement('thead');
    const filaCabecera = document.createElement('tr');
    ['Descripción', 'Cantidad', 'Precio unit.', 'Bonif. %', 'Neto', 'IVA', 'Subtotal'].forEach(titulo => {
        const th = document.createElement('th');
        th.textContent = titulo;
        filaCabecera.appendChild(th);
    });
    cabecera.appendChild(filaCabecera);
    tabla.appendChild(cabecera);
    const cuerpo = document.createElement('tbody');
    factura.detalles.forEach(detalle => {
        const filaItem = document.createElement('tr');
        filaItem.appendChild(crearCelda(detalle.descripcion));
        filaItem.appendChild(crearCelda(String(detalle.cantidad), 'text-end'));
        filaItem.appendChild(crearCelda(formatearMoneda(detalle.precioUnitario), 'text-end'));
        filaItem.appendChild(crearCelda(detalle.porcentajeBonificacion + ' %', 'text-end'));
        filaItem.appendChild(crearCelda(formatearMoneda(detalle.importeNeto), 'text-end'));
        filaItem.appendChild(crearCelda(formatearMoneda(detalle.importeIva), 'text-end'));
        filaItem.appendChild(crearCelda(formatearMoneda(detalle.importeSubtotal), 'text-end'));
        cuerpo.appendChild(filaItem);
    });
    tabla.appendChild(cuerpo);
    celda.appendChild(tabla);
    if (factura.observaciones) {
        const observaciones = document.createElement('div');
        observaciones.className = 'small text-muted mt-2';
        observaciones.textContent = 'Observaciones: ' + factura.observaciones;
        celda.appendChild(observaciones);
    }
    fila.appendChild(celda);
    return fila;
}
function anularFactura(factura) {
    return __awaiter(this, void 0, void 0, function* () {
        const motivo = window.prompt('Anular la factura Nº ' + factura.numeroFactura
            + '.\nMotivo (opcional). Cancelar para no anular:');
        if (motivo === null) {
            return;
        }
        // Se conservan las observaciones previas y se agrega el motivo
        const partes = [];
        if (factura.observaciones) {
            partes.push(factura.observaciones);
        }
        if (motivo.trim() !== '') {
            partes.push('Anulación: ' + motivo.trim());
        }
        const observaciones = partes.length > 0 ? partes.join(' | ') : undefined;
        try {
            yield facturaService.actualizarEstado(factura.id, ESTADO_ANULADA, observaciones);
            mostrarMensaje('success', 'Factura Nº ' + factura.numeroFactura + ' anulada.');
            yield cargarFacturas();
        }
        catch (error) {
            mostrarMensaje('danger', 'No se pudo anular la factura: ' + mensajeDeError(error));
        }
    });
}
function eliminarFactura(factura) {
    return __awaiter(this, void 0, void 0, function* () {
        if (!window.confirm('¿Eliminar definitivamente la factura Nº ' + factura.numeroFactura + '?')) {
            return;
        }
        try {
            yield facturaService.eliminarFactura(factura.id);
            mostrarMensaje('success', 'Factura Nº ' + factura.numeroFactura + ' eliminada.');
            if (facturaEnEdicion && facturaEnEdicion.id === factura.id) {
                cerrarFormulario(); // la factura que se estaba editando ya no existe
            }
            yield cargarFacturas();
        }
        catch (error) {
            mostrarMensaje('danger', 'No se pudo eliminar la factura: ' + mensajeDeError(error));
        }
    });
}
function renderizarFacturas(facturas) {
    facturasTbody.textContent = '';
    if (facturas.length === 0) {
        facturasTbody.appendChild(crearFilaMensaje('No se encontraron facturas.', COLUMNAS_LISTADO, 'text-center'));
        return;
    }
    facturas.forEach(factura => {
        const anulada = factura.estado === ESTADO_ANULADA;
        const fila = document.createElement('tr');
        if (anulada) {
            fila.className = 'text-muted';
        }
        fila.appendChild(crearCelda(String(factura.numeroFactura)));
        fila.appendChild(crearCelda(formatearFecha(factura.fechaEmision)));
        fila.appendChild(crearCelda(factura.clienteDenominacion));
        fila.appendChild(crearCelda(factura.condicionIva));
        fila.appendChild(crearCelda(factura.puntoVentaDescripcion));
        fila.appendChild(crearCelda(formatearMoneda(factura.importeNeto), 'text-end'));
        fila.appendChild(crearCelda(formatearMoneda(factura.importeIva), 'text-end'));
        fila.appendChild(crearCelda(formatearMoneda(factura.importeTotal), 'text-end fw-semibold'));
        const celdaEstado = document.createElement('td');
        const insignia = document.createElement('span');
        insignia.className = 'badge ' + (anulada ? 'bg-danger' : 'bg-success');
        insignia.textContent = factura.estado;
        if (anulada && factura.fechaAnulacion) {
            insignia.title = 'Anulada el ' + new Date(factura.fechaAnulacion).toLocaleString('es-AR');
        }
        celdaEstado.appendChild(insignia);
        fila.appendChild(celdaEstado);
        fila.appendChild(crearCelda(String(factura.detalles.length), 'text-center'));
        const celdaAcciones = document.createElement('td');
        celdaAcciones.className = 'text-nowrap';
        const grupo = document.createElement('div');
        grupo.className = 'btn-group';
        grupo.appendChild(crearBoton('Ver ítems', 'btn-outline-secondary', () => {
            const siguiente = fila.nextElementSibling;
            if (siguiente && siguiente.classList.contains('fila-detalle')) {
                siguiente.remove();
            }
            else {
                fila.after(crearFilaDetalle(factura));
            }
        }));
        const btnEditar = crearBoton('Editar', 'btn-outline-primary', () => { void abrirFormularioEdicion(factura); });
        btnEditar.disabled = anulada; // una factura anulada no puede modificarse
        grupo.appendChild(btnEditar);
        const btnAnular = crearBoton('Anular', 'btn-outline-warning', () => { void anularFactura(factura); });
        btnAnular.disabled = anulada;
        grupo.appendChild(btnAnular);
        grupo.appendChild(crearBoton('Eliminar', 'btn-outline-danger', () => { void eliminarFactura(factura); }));
        celdaAcciones.appendChild(grupo);
        fila.appendChild(celdaAcciones);
        facturasTbody.appendChild(fila);
    });
}
function leerFiltros() {
    return {
        numeroFactura: filtroNumero.value,
        clienteId: clienteDeFiltro ? String(clienteDeFiltro.id) : '',
        condicionIva: filtroCondicion.value,
        estado: filtroEstado.value,
        fechaDesde: filtroFechaDesde.value,
        fechaHasta: filtroFechaHasta.value
    };
}
function cargarFacturas() {
    return __awaiter(this, void 0, void 0, function* () {
        try {
            const facturas = yield facturaService.obtenerFacturas(leerFiltros());
            renderizarFacturas(facturas);
        }
        catch (error) {
            facturasTbody.textContent = '';
            facturasTbody.appendChild(crearFilaMensaje('Error al cargar facturas: ' + mensajeDeError(error), COLUMNAS_LISTADO, 'text-center text-danger'));
        }
    });
}
// ---------------------------------------------------------------------------
// Eventos
// ---------------------------------------------------------------------------
btnNuevaFactura.addEventListener('click', () => { void abrirFormularioAlta(); });
btnCancelarFactura.addEventListener('click', cerrarFormulario);
filtrosForm.addEventListener('submit', (evento) => {
    evento.preventDefault();
    void cargarFacturas();
});
btnLimpiarFiltros.addEventListener('click', () => {
    filtrosForm.reset();
    clienteDeFiltro = null;
    filtroCliente.value = '';
    void cargarFacturas();
});
// Explorador de clientes: en el filtro de búsqueda
function elegirClienteParaFiltro() {
    abrirExplorador(cliente => {
        clienteDeFiltro = cliente;
        filtroCliente.value = textoDeCliente(cliente);
    });
}
btnFiltroBuscarCliente.addEventListener('click', elegirClienteParaFiltro);
filtroCliente.addEventListener('click', elegirClienteParaFiltro);
btnFiltroQuitarCliente.addEventListener('click', () => {
    clienteDeFiltro = null;
    filtroCliente.value = '';
});
// Explorador de clientes: en el formulario de factura
function elegirClienteParaFactura() {
    abrirExplorador(cliente => {
        clienteDeFactura = cliente;
        altaCliente.value = textoDeCliente(cliente);
    });
}
btnAltaBuscarCliente.addEventListener('click', elegirClienteParaFactura);
altaCliente.addEventListener('click', elegirClienteParaFactura);
// Explorador de clientes: ventana
exploradorBuscar.addEventListener('click', () => { void buscarClientes(); });
exploradorTexto.addEventListener('input', () => {
    window.clearTimeout(temporizadorBusqueda);
    temporizadorBusqueda = window.setTimeout(() => { void buscarClientes(); }, 300);
});
exploradorTexto.addEventListener('keydown', (evento) => {
    if (evento.key === 'Enter') {
        evento.preventDefault();
        window.clearTimeout(temporizadorBusqueda);
        void buscarClientes();
    }
});
exploradorCerrar.addEventListener('click', cerrarExplorador);
exploradorCancelar.addEventListener('click', cerrarExplorador);
exploradorModal.addEventListener('click', (evento) => {
    if (evento.target === exploradorModal) { // clic en el fondo oscuro
        cerrarExplorador();
    }
});
document.addEventListener('keydown', (evento) => {
    if (evento.key === 'Escape' && exploradorAbierto()) {
        cerrarExplorador();
    }
});
// Lista de precios y artículos
altaListaPrecio.addEventListener('change', () => { void cargarArticulosDeLista(listaSeleccionadaId()); });
itemArticulo.addEventListener('change', () => {
    const articulo = buscarArticulo(parseInt(itemArticulo.value, 10));
    itemPrecio.value = articulo && articulo.precioVenta !== null ? String(articulo.precioVenta) : '';
});
btnAgregarItem.addEventListener('click', agregarItem);
// Enter dentro de los campos del ítem agrega el ítem (no envía la factura)
[itemCantidad, itemPrecio, itemBonificacion].forEach(campo => {
    campo.addEventListener('keydown', (evento) => {
        if (evento.key === 'Enter') {
            evento.preventDefault();
            agregarItem();
        }
    });
});
altaForm.addEventListener('submit', (evento) => {
    evento.preventDefault();
    void guardarFactura();
});
// ---------------------------------------------------------------------------
// Arranque
// ---------------------------------------------------------------------------
function iniciar() {
    return __awaiter(this, void 0, void 0, function* () {
        altaFecha.value = fechaDeHoy();
        renderizarItems();
        filtroEstado.value = '';
        try {
            catalogos = yield catalogoService.obtenerCatalogos();
            poblarCombos(catalogos);
        }
        catch (error) {
            mostrarMensaje('danger', 'No se pudieron cargar los datos del formulario: ' + mensajeDeError(error));
        }
        yield cargarFacturas();
    });
}
void iniciar();
