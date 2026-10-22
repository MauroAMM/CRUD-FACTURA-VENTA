import { enviar } from './httpCliente.js';
import { CatalogosDTO, ItemCatalogoDTO } from './models/catalogo.dto.js';

/** Obtiene los datos maestros (condiciones de IVA, listas de precios, etc.) para los combos del formulario. */
export class CatalogoService {

    private readonly baseUrl: string;

    constructor(baseUrl: string = 'http://localhost:8080/api') {
        this.baseUrl = baseUrl;
    }

    /** GET /api/catalogos */
    async obtenerCatalogos(): Promise<CatalogosDTO> {
        return enviar<CatalogosDTO>('GET', this.baseUrl + '/catalogos');
    }

    /** GET /api/listas-precio/{id}/articulos -> artículos con el precio que tienen EN esa lista. */
    async obtenerArticulosDeLista(listaPrecioId: number): Promise<ItemCatalogoDTO[]> {
        return enviar<ItemCatalogoDTO[]>('GET', this.baseUrl + '/listas-precio/' + listaPrecioId + '/articulos');
    }
}
