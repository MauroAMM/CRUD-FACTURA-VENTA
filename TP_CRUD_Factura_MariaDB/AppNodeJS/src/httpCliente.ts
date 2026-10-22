import { registrarPeticion } from './apiLogger.js';

/**
 * Pequeño helper compartido por los servicios HTTP: arma la petición con fetch,
 * la registra en el panel de trazas y traduce los errores del backend a Error.
 */
export async function enviar<T>(metodo: string, url: string, body?: unknown): Promise<T> {
    const headers: Record<string, string> = {};
    const opciones: RequestInit = { method: metodo };

    if (body !== undefined) {
        headers['Content-Type'] = 'application/json';
        opciones.headers = headers;
        opciones.body = JSON.stringify(body);
    }

    registrarPeticion({ metodo, url, headers, body });

    let respuesta: Response;
    try {
        respuesta = await fetch(url, opciones);
    } catch {
        throw new Error('No se pudo conectar con el backend (' + url + '). ¿Está levantado en el puerto 8080?');
    }

    if (!respuesta.ok) {
        throw new Error(await extraerMensajeError(respuesta));
    }
    if (respuesta.status === 204) {
        return undefined as T;
    }
    return respuesta.json() as Promise<T>;
}

/** El backend responde errores como { status, error, mensaje }. */
async function extraerMensajeError(respuesta: Response): Promise<string> {
    const texto = await respuesta.text();
    try {
        const json = JSON.parse(texto);
        if (json && typeof json.mensaje === 'string') {
            return json.mensaje;
        }
    } catch {
        // la respuesta no era JSON: se usa el texto tal cual
    }
    return 'Error ' + respuesta.status + ': ' + (texto || respuesta.statusText);
}
