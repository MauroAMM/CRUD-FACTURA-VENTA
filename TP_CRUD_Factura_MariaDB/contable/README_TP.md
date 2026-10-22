# TP: Operaciones de escritura en API REST (POST, PUT, DELETE) + base de datos XAMPP

## Puesta en marcha con XAMPP

1. Abrir el **Panel de control de XAMPP** e iniciar **MySQL** (XAMPP usa MariaDB; Apache no hace falta).
2. *(Recomendado)* Crear la base con soporte completo de acentos: abrir `http://localhost/phpmyadmin`,
   pestaña **SQL**, pegar el contenido de `database/crear_base.sql` y ejecutar.
   Si te lo salteás, Spring la crea sola (`createDatabaseIfNotExist=true`).
3. En una terminal dentro de `contable`:
   - Windows: `.\mvnw.cmd clean spring-boot:run`
   - Mac/Linux: `./mvnw clean spring-boot:run`
4. En el primer arranque Hibernate **crea las tablas** y se cargan **datos de ejemplo** (solo si la base está vacía).
   Podés verlas en phpMyAdmin, base `contable`.
5. Levantar el frontend como siempre (`npm start` en `AppNodeJS`); no tiene cambios.

La conexión se configura en `src/main/resources/application.properties`
(usuario `root` sin contraseña, puerto 3306: los valores por defecto de XAMPP).

### Tablas que crea Hibernate
`factura_venta`, `factura_venta_detalle`, `cliente`, `contacto`, `domicilio`, `condicion_iva`, `punto_venta`,
`tipo_moneda`, `articulo`, `rubro`, `marca`, `lista_precio`, `lista_precio_articulo`.

### Problemas frecuentes
| Síntoma | Causa / solución |
|---|---|
| `Communications link failure`, `Connection refused` | MySQL no está iniciado en el panel de XAMPP |
| `Access denied for user 'root'` | Le pusiste contraseña a root: completar `spring.datasource.password` |
| Puerto distinto de 3306 | Cambiar el puerto en `spring.datasource.url` |
| `Ambiguous mapping` al arrancar | Quedaron clases viejas compiladas: borrar la carpeta `target` y usar `clean` |
| Querés volver a los datos de ejemplo | En phpMyAdmin borrar la base `contable` y reiniciar el backend |
| Las tildes se ven mal en phpMyAdmin | Crear la base con `database/crear_base.sql` (utf8mb4) |
| `spring.jpa.show-sql=true` | Muestra en consola el SQL que ejecuta Hibernate |

## Funciones de la pantalla

- **Nueva Factura:** el formulario de alta no se muestra hasta tocar el botón *Nueva Factura*; *Cancelar* lo vuelve a ocultar.
- **Editar:** cada fila tiene un botón *Editar* que abre el mismo formulario con los datos de la factura (cabecera e ítems) y guarda con `PUT /api/facturas/{id}`. Las facturas anuladas no se pueden editar.
- **Explorador de clientes:** el cliente se elige con una lupa que abre una ventana con buscador (por nombre o CUIT/CUIL). Se usa en el formulario de factura y en la búsqueda (filtra por el cliente elegido; la "x" lo quita).
- **Lista de precios:** el formulario tiene un combo de *Lista de precios*; los artículos disponibles y su precio dependen de la lista elegida (hay dos: *Lista general* y *Lista mayorista*, esta con 10% menos). La lista se bloquea mientras haya ítems cargados, porque sus precios salieron de ella. Cada factura guarda con qué lista se armó.

### Si ya tenías datos en la base
No hace falta borrar nada: al arrancar, Hibernate agrega la columna `lista_precio_id` a `factura_venta` y el backend crea la *Lista mayorista* si no existe (clientes y facturas no se tocan). Las facturas anteriores, que no tienen lista guardada, usan la lista de sus ítems.

## Qué contiene

| Capa | Archivo | Descripción |
|---|---|---|
| Entidades | `entidades/*` | Ahora son entidades JPA (`@Entity`, `@ManyToOne`, `@OneToMany`...) |
| DTOs de entrada | `entidades/dto/FacturaRequestDTO`, `FacturaDetalleRequestDTO` | Reciben IDs, sin objetos anidados |
| DTOs de salida | `FacturaReporteDTO`, `FacturaDetalleReporteDTO` | Respuesta de la API |
| Repositorios | `repositorios/*Repositorio` | Interfaces de Spring Data JPA (consultas a la base) |
| Servicio | `servicios/FacturaService` | `crear`, `actualizar`, `eliminar` con `@Transactional` |
| Controlador | `controller/FacturaRestController` | `POST`, `PUT`, `DELETE` |
| Errores | `excepciones/*` | `RecursoNoEncontradoException` -> 404, `ReglaNegocioException` -> 400 |
| Datos de ejemplo | `config/DatosIniciales` | Se ejecuta al arrancar; solo carga si la base está vacía |
| Clientes | `servicios/ClienteService`, `controller/ClienteRestController` | Búsqueda de clientes para el explorador (`GET /api/clientes`) |
| Extra | `controller/CatalogoController` | Datos para los combos y artículos por lista de precios |

## Endpoints

| Método | URL | Respuesta |
|---|---|---|
| GET | `/api/facturas?numeroFactura=&clienteId=&clienteDenominacion=&condicionIva=&estado=&fechaDesde=&fechaHasta=` | 200 + lista |
| GET | `/api/facturas/{id}` | 200 / 404 |
| POST | `/api/facturas` | **201 Created** + `FacturaReporteDTO` |
| PUT | `/api/facturas/{id}` | **200 OK** + `FacturaReporteDTO` / 404 / 400 |
| DELETE | `/api/facturas/{id}` | **204 No Content** / 404 |
| GET | `/api/catalogos` | 200 (condiciones de IVA, puntos de venta, monedas y listas de precios) |
| GET | `/api/clientes?texto=` | 200 + clientes que coinciden por nombre o CUIT/CUIL (explorador de clientes) |
| GET | `/api/listas-precio/{id}/articulos` | 200 + artículos con el precio que tienen en esa lista / 404 |

`{id}` es el ID interno de la factura (campo `id`), no el número de comprobante.

### POST (alta)
```json
{
  "fechaEmision": "2026-10-16",
  "clienteId": 2,
  "condicionIvaId": 1,
  "puntoVentaId": 1,
  "tipoMonedaId": 1,
  "listaPrecioId": 2,
  "observaciones": "Pedido web",
  "detalles": [
    { "articuloId": 3, "cantidad": 2 },
    { "articuloId": 1, "cantidad": 4, "precioUnitario": 500, "porcentajeBonificacion": 10 }
  ]
}
```
`fechaEmision`, `listaPrecioId` (si falta se usa la primera lista, la general), `precioUnitario` (si falta se toma de la lista de precios elegida), `porcentajeBonificacion` y `descripcion` son opcionales.

### PUT (actualización parcial)
Todo campo que no se envía queda sin cambios. Si se envía `detalles`, reemplaza a los ítems actuales.

Edición completa (lo que envía el botón **Editar** de la pantalla): cabecera + ítems. Para cambiar la `listaPrecioId` hay que enviar también los `detalles`, porque sus precios salen de la lista.

Anular (anulación lógica: `estado = "ANULADA"` y `fechaAnulacion = new Date()`):
```json
{ "estado": "ANULADA", "observaciones": "Error de carga" }
```
Una factura anulada no puede volver a modificarse (400).

### DELETE
Eliminación física: borra la factura y, en cascada, sus ítems. Anular (PUT) y eliminar (DELETE) son operaciones distintas.

## Cálculo de importes (lo hace el servidor)

```
neto del ítem = cantidad x precio x (1 - bonificación / 100)
IVA del ítem  = neto x 21%      (0% si la condición es "IVA Sujeto Exento")
subtotal      = neto + IVA
total factura = suma de los subtotales
```

## Errores (cuerpo JSON)
```json
{ "status": 404, "error": "Not Found", "mensaje": "Cliente con id 99 no existe" }
```

## Notas técnicas
- `@Transactional`: si algo falla, se revierte todo. Además el servicio valida todo antes de modificar la entidad.
- Al reemplazar los ítems de una factura se usa `clear()` + `add()` sobre la misma lista (no `setDetalles`),
  porque con `orphanRemoval = true` JPA exige conservar la colección original.
- El número de comprobante es único en la base (`unique`) y se calcula como último + 1.
- Los importes son `double` (como en las entidades originales). En un sistema real conviene `BigDecimal` / `DECIMAL`.
- `@Temporal` figura como "desaconsejado" en JPA 3.2 a favor de `java.time`; sigue funcionando y solo genera un aviso al compilar.
- `ddl-auto=update` es cómodo para desarrollo; en producción las tablas se manejan con scripts de migración.
