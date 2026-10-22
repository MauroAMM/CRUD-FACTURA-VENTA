import { enviar } from './httpCliente.js';
import {
    FacturaFiltrosDTO,
    FacturaReporteDTO,
    FacturaRequestDTO,
    ESTADO_ANULADA
} from './models/factura.dto.js';

/** Centraliza las llamadas HTTP a la API REST de facturas del backend Spring Boot. */
export class FacturaService {

    private readonly baseUrl: string;

    constructor(baseUrl: string = 'http://localhost:8080/api/facturas') {
        this.baseUrl = baseUrl;
    }

    /** GET /api/facturas?filtros... -> 200 OK */
    async obtenerFacturas(filtros: FacturaFiltrosDTO = {}): Promise<FacturaReporteDTO[]> {
        return enviar<FacturaReporteDTO[]>('GET', this.baseUrl + this.construirQuery(filtros));
    }

    /** POST /api/facturas -> 201 Created + la factura generada */
    async crearFactura(factura: FacturaRequestDTO): Promise<FacturaReporteDTO> {
        return enviar<FacturaReporteDTO>('POST', this.baseUrl, factura);
    }

    /**
     * PUT /api/facturas/{id} -> 200 OK.
     * Edición completa: cabecera (cliente, condición de IVA, punto de venta, moneda, lista de precios,
     * fecha, observaciones) e ítems. Los ítems enviados reemplazan a los actuales.
     */
    async actualizarFactura(id: number, factura: FacturaRequestDTO): Promise<FacturaReporteDTO> {
        return enviar<FacturaReporteDTO>('PUT', this.baseUrl + '/' + id, factura);
    }

    /**
     * PUT /api/facturas/{id} -> 200 OK.
     * Envía solo estado (y observaciones si se indican): el backend deja sin cambios
     * la cabecera y los ítems. Con estado "ANULADA" registra la anulación.
     */
    async actualizarEstado(id: number, estado: string, observaciones?: string): Promise<FacturaReporteDTO> {
        const cuerpo: FacturaRequestDTO = { estado };
        if (observaciones !== undefined) {
            cuerpo.observaciones = observaciones;
        }
        return enviar<FacturaReporteDTO>('PUT', this.baseUrl + '/' + id, cuerpo);
    }

    /** Atajo para anular una factura. */
    async anularFactura(id: number, observaciones?: string): Promise<FacturaReporteDTO> {
        return this.actualizarEstado(id, ESTADO_ANULADA, observaciones);
    }

    /** DELETE /api/facturas/{id} -> 204 No Content */
    async eliminarFactura(id: number): Promise<void> {
        return enviar<void>('DELETE', this.baseUrl + '/' + id);
    }

    /** Arma "?clave=valor&..." ignorando los filtros vacíos. */
    private construirQuery(filtros: FacturaFiltrosDTO): string {
        const params = new URLSearchParams();
        const clave = Object.keys(filtros) as Array<keyof FacturaFiltrosDTO>;
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
