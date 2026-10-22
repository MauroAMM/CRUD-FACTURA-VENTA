var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
import { registrarPeticion } from './apiLogger.js';
/**
 * Pequeño helper compartido por los servicios HTTP: arma la petición con fetch,
 * la registra en el panel de trazas y traduce los errores del backend a Error.
 */
export function enviar(metodo, url, body) {
    return __awaiter(this, void 0, void 0, function* () {
        const headers = {};
        const opciones = { method: metodo };
        if (body !== undefined) {
            headers['Content-Type'] = 'application/json';
            opciones.headers = headers;
            opciones.body = JSON.stringify(body);
        }
        registrarPeticion({ metodo, url, headers, body });
        let respuesta;
        try {
            respuesta = yield fetch(url, opciones);
        }
        catch (_a) {
            throw new Error('No se pudo conectar con el backend (' + url + '). ¿Está levantado en el puerto 8080?');
        }
        if (!respuesta.ok) {
            throw new Error(yield extraerMensajeError(respuesta));
        }
        if (respuesta.status === 204) {
            return undefined;
        }
        return respuesta.json();
    });
}
/** El backend responde errores como { status, error, mensaje }. */
function extraerMensajeError(respuesta) {
    return __awaiter(this, void 0, void 0, function* () {
        const texto = yield respuesta.text();
        try {
            const json = JSON.parse(texto);
            if (json && typeof json.mensaje === 'string') {
                return json.mensaje;
            }
        }
        catch (_a) {
            // la respuesta no era JSON: se usa el texto tal cual
        }
        return 'Error ' + respuesta.status + ': ' + (texto || respuesta.statusText);
    });
}
