var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
import { enviar } from './httpCliente.js';
import { ESTADO_ANULADA } from './models/factura.dto.js';
/** Centraliza las llamadas HTTP a la API REST de facturas del backend Spring Boot. */
export class FacturaService {
    constructor(baseUrl = 'http://localhost:8080/api/facturas') {
        this.baseUrl = baseUrl;
    }
    /** GET /api/facturas?filtros... -> 200 OK */
    obtenerFacturas() {
        return __awaiter(this, arguments, void 0, function* (filtros = {}) {
            return enviar('GET', this.baseUrl + this.construirQuery(filtros));
        });
    }
    /** POST /api/facturas -> 201 Created + la factura generada */
    crearFactura(factura) {
        return __awaiter(this, void 0, void 0, function* () {
            return enviar('POST', this.baseUrl, factura);
        });
    }
    /**
     * PUT /api/facturas/{id} -> 200 OK.
     * Edición completa: cabecera (cliente, condición de IVA, punto de venta, moneda, lista de precios,
     * fecha, observaciones) e ítems. Los ítems enviados reemplazan a los actuales.
     */
    actualizarFactura(id, factura) {
        return __awaiter(this, void 0, void 0, function* () {
            return enviar('PUT', this.baseUrl + '/' + id, factura);
        });
    }
    /**
     * PUT /api/facturas/{id} -> 200 OK.
     * Envía solo estado (y observaciones si se indican): el backend deja sin cambios
     * la cabecera y los ítems. Con estado "ANULADA" registra la anulación.
     */
    actualizarEstado(id, estado, observaciones) {
        return __awaiter(this, void 0, void 0, function* () {
            const cuerpo = { estado };
            if (observaciones !== undefined) {
                cuerpo.observaciones = observaciones;
            }
            return enviar('PUT', this.baseUrl + '/' + id, cuerpo);
        });
    }
    /** Atajo para anular una factura. */
    anularFactura(id, observaciones) {
        return __awaiter(this, void 0, void 0, function* () {
            return this.actualizarEstado(id, ESTADO_ANULADA, observaciones);
        });
    }
    /** DELETE /api/facturas/{id} -> 204 No Content */
    eliminarFactura(id) {
        return __awaiter(this, void 0, void 0, function* () {
            return enviar('DELETE', this.baseUrl + '/' + id);
        });
    }
    /** Arma "?clave=valor&..." ignorando los filtros vacíos. */
    construirQuery(filtros) {
        const params = new URLSearchParams();
        const clave = Object.keys(filtros);
        for (const nombre of clave) {
            const valor = filtros[nombre];
            if (valor !== undefined && valor !== null && valor.trim() !== '') {
                params.append(nombre, valor.trim());
            }
        }
        const query = params.toString();
        return query ? '?' + query : '';
    }
}
