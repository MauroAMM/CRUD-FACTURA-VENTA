export interface DetallePeticion {
    metodo: string;
    url: string;
    headers?: Record<string, string>;
    body?: unknown;
}

function formatearJson(valor: unknown): string {
    try {
        return JSON.stringify(valor, null, 2);
    } catch {
        return String(valor);
    }
}

function obtenerPanel(): HTMLElement {
    const panelExistente = document.getElementById('api-log-panel');
    if (panelExistente) {
        return panelExistente;
    }

    const panel = document.createElement('section');
    panel.id = 'api-log-panel';
    panel.className = 'api-log-panel';
    panel.innerHTML = `
        <div class="api-log-header">
            <h5 class="mb-0">Trazas de comunicación con el backend</h5>
            <button type="button" id="btn-limpiar-api-log" class="btn btn-sm btn-outline-secondary">Limpiar</button>
        </div>
        <div id="api-log-entradas" class="api-log-entradas"></div>
    `;

    document.body.appendChild(panel);

    panel.querySelector('#btn-limpiar-api-log')?.addEventListener('click', () => {
        const entradas = document.getElementById('api-log-entradas');
        if (entradas) {
            entradas.innerHTML = '';
        }
    });

    return panel;
}

function obtenerContenedorEntradas(): HTMLElement {
    const panel = obtenerPanel();
    let entradas = document.getElementById('api-log-entradas');
    if (!entradas) {
        entradas = document.createElement('div');
        entradas.id = 'api-log-entradas';
        entradas.className = 'api-log-entradas';
        panel.appendChild(entradas);
    }
    return entradas;
}

export function registrarPeticion(detalle: DetallePeticion): void {
    const entradas = obtenerContenedorEntradas();

    const entrada = document.createElement('div');
    entrada.className = 'api-log-entrada';

    const timestamp = new Date().toLocaleTimeString('es-AR', { hour12: false });
    const metodoClase = `api-log-metodo api-log-metodo-${detalle.metodo.toLowerCase()}`;

    let bodyHtml = '';
    if (detalle.body !== undefined) {
        bodyHtml = `
            <div class="api-log-seccion">Body:</div>
            <pre class="api-log-body">${formatearJson(detalle.body)}</pre>
        `;
    }

    let headersHtml = '';
    if (detalle.headers && Object.keys(detalle.headers).length > 0) {
        headersHtml = `
            <div class="api-log-seccion">Headers:</div>
            <pre class="api-log-headers">${formatearJson(detalle.headers)}</pre>
        `;
    }

    entrada.innerHTML = `
        <div class="api-log-linea-principal">
            <span class="api-log-timestamp">[${timestamp}]</span>
            <span class="${metodoClase}">${detalle.metodo.toUpperCase()}</span>
            <span class="api-log-url">${detalle.url}</span>
        </div>
        ${headersHtml}
        ${bodyHtml}
    `;

    entradas.insertBefore(entrada, entradas.firstChild);
}
