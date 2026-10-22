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
/** Búsqueda de clientes para el explorador (lupa). */
export class ClienteService {
    constructor(baseUrl = 'http://localhost:8080/api/clientes') {
        this.baseUrl = baseUrl;
    }
    /** GET /api/clientes?texto=... -> por nombre o CUIT/CUIL. Sin texto devuelve todos. */
    buscarClientes() {
        return __awaiter(this, arguments, void 0, function* (texto = '') {
            const params = new URLSearchParams();
            if (texto.trim() !== '') {
                params.append('texto', texto.trim());
            }
            const query = params.toString();
            return enviar('GET', this.baseUrl + (query ? '?' + query : ''));
        });
    }
}
