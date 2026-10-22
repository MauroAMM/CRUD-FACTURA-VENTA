import { enviar } from './httpCliente.js';
import { ClienteDTO } from './models/cliente.dto.js';

/** Búsqueda de clientes para el explorador (lupa). */
export class ClienteService {

    private readonly baseUrl: string;

    constructor(baseUrl: string = 'http://localhost:8080/api/clientes') {
        this.baseUrl = baseUrl;
    }

    /** GET /api/clientes?texto=... -> por nombre o CUIT/CUIL. Sin texto devuelve todos. */
    async buscarClientes(texto: string = ''): Promise<ClienteDTO[]> {
        const params = new URLSearchParams();
        if (texto.trim() !== '') {
            params.append('texto', texto.trim());
        }
        const query = params.toString();
        return enviar<ClienteDTO[]>('GET', this.baseUrl + (query ? '?' + query : ''));
    }
}
