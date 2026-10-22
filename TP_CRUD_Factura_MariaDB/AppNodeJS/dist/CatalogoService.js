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
/** Obtiene los datos maestros (condiciones de IVA, listas de precios, etc.) para los combos del formulario. */
export class CatalogoService {
    constructor(baseUrl = 'http://localhost:8080/api') {
        this.baseUrl = baseUrl;
    }
    /** GET /api/catalogos */
    obtenerCatalogos() {
        return __awaiter(this, void 0, void 0, function* () {
            return enviar('GET', this.baseUrl + '/catalogos');
        });
    }
    /** GET /api/listas-precio/{id}/articulos -> artículos con el precio que tienen EN esa lista. */
    obtenerArticulosDeLista(listaPrecioId) {
        return __awaiter(this, void 0, void 0, function* () {
            return enviar('GET', this.baseUrl + '/listas-precio/' + listaPrecioId + '/articulos');
        });
    }
}
