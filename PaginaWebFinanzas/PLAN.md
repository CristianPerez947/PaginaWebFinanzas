# Plan de desarrollo — Cuaderno de Fiado Digital (SI642 Finanzas e Ingeniería Económica 2026-20)

> Documento de referencia para desarrollar la aplicación. Todo lo marcado con **[REVISAR]** es una decisión tomada por la IA que el grupo debe validar.

## 1. Alcance

Aplicación web que reemplaza el cuaderno de fiado de una bodega. Es **multi-tienda** (exigencia del enunciado), pero la demostración usa **dos bodegas**:

- **La Tiendita de Don Pepe** (30 productos, tienda principal de la demo).
- **Bodega Doña Rosa** (12 productos, para demostrar el aislamiento entre tiendas).

Fuera de alcance (según el enunciado): inventario/stock e impuestos.

### Roles

| Rol | Quién es | Qué hace |
|---|---|---|
| `ADMIN_SISTEMA` | Administrador de la plataforma | Alta/baja de tiendas y de sus dueños/vendedores. Controla la fecha simulada (modo demo). |
| `VENDEDOR` | Dueño de una bodega (1 por tienda) | Gestiona productos, clientes, ventas fiadas, pagos, estados de cuenta, reportes y condonaciones de **su** tienda. |
| `CLIENTE` | Vecino con crédito | Solo consulta: deuda, historial, estado de cuenta y catálogo de **su** tienda. |

### Pantallas / rutas del frontend

| Ruta | Público | Descripción |
|---|---|---|
| `/t/:slug` | Sí | Página de la bodega: catálogo con precios y acceso de clientes (DNI + clave). |
| `/t/:slug/mi-cuenta` | Cliente | Deuda actual, cuotas, historial, estado de cuenta en PDF. |
| `/ingresar` | Sí | Login del vendedor y del administrador del sistema. |
| `/admin/tiendas` | Admin | Alta/baja de tiendas y dueños. |
| `/admin/reloj` | Admin | Fecha simulada (solo modo demo). |
| `/vendedor/panel` | Vendedor | Dashboard con gráficos y alertas de mora. |
| `/vendedor/productos` | Vendedor | CRUD de productos (baja lógica). |
| `/vendedor/clientes` | Vendedor | CRUD de clientes con sus condiciones de crédito. |
| `/vendedor/ventas/nueva` | Vendedor | Registro de venta fiada (carrito, modalidad, vista previa del plan). |
| `/vendedor/cobranzas` | Vendedor | Estados de cuenta por corte, botón "Registrar pago", condonar deuda. |
| `/vendedor/reportes` | Vendedor | Deudores, morosos, ventas por producto, intereses ganados, cartera. |
| `/vendedor/auditoria` | Vendedor | Registro de todas las operaciones de su tienda. |

Toda la interfaz está en **español**. Cada campo de formulario tiene un **ícono de ayuda** (tooltip) que explica qué es y qué formato espera (requisito del informe: "ayuda por campo").

## 2. Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 25 (LTS), Spring Boot 4.0.x, Spring Data JPA, Spring Security + JWT (jjwt 0.12.6), Lombok, springdoc-openapi, OpenPDF (PDF) |
| Base de datos | PostgreSQL 18 (local) |
| Frontend | Angular 21 standalone, signals, Angular Material, Chart.js (gráficos) |
| Pruebas | JUnit 5 (dominio financiero y servicios), Vitest (frontend) |
| Despliegue | Docker en Render (manual, lo hace el grupo). Imágenes de productos dentro de `assets/` del frontend. |

**[REVISAR]** Se usa Java 25 LTS en lugar de Java 26 (los proyectos de clase) porque su PC tiene ambos y 25 es LTS.

## 3. Estructura de carpetas

```
PaginaWebFinanzas/
├── PLAN.md
├── bodega-backend/     (Spring Boot, DDD)
└── bodega-frontend/    (Angular, DDD)
```

## 4. Arquitectura DDD (copiada de `learning-center-platform-v2610-master` y `safestep-frontend`)

### 4.1 Backend — paquete raíz `com.bodega.plataforma`

Un paquete por *bounded context*, cada uno con las mismas 4 capas. **Código en español.**

```
<contexto>/
├── dominio/
│   ├── modelo/
│   │   ├── agregados/         (raíces de agregado, sin anotaciones JPA)
│   │   ├── entidades/
│   │   ├── objetosvalor/      (records con validación)
│   │   ├── comandos/          (records)
│   │   ├── consultas/         (records)
│   │   └── eventos/
│   ├── excepciones/
│   ├── servicios/             (servicios de dominio, p. ej. calculadora financiera)
│   └── repositorios/          (interfaces = puertos)
├── aplicacion/
│   ├── comandoservicios/      (interfaces)
│   ├── consultaservicios/     (interfaces)
│   └── interno/
│       ├── comandoservicios/  (implementaciones)
│       ├── consultaservicios/
│       ├── manejadoreseventos/
│       └── serviciossalientes/acl/
├── infraestructura/
│   └── persistencia/jpa/
│       ├── entidades/         (XPersistenciaEntidad)
│       ├── repositorios/      (Spring Data)
│       ├── adaptadores/       (XRepositorioImpl implementa el puerto)
│       └── ensambladores/     (dominio <-> persistencia, estáticos)
└── interfaces/
    ├── rest/                  (controladores)
    │   ├── recursos/          (records)
    │   └── transformacion/    (recurso <-> comando/entidad)
    └── acl/                   (fachada hacia otros contextos)
```

Reglas heredadas de los proyectos de clase:
- Los servicios devuelven `Resultado<T, ErrorAplicacion>` (`noEncontrado`, `conflicto`, `validacion`, `reglaDeNegocio`, `inesperado`). El controlador lo transforma con `RespuestaEnsamblador`.
- Un contexto **nunca** usa el repositorio de otro: usa la **fachada ACL** (`XContextoFachada`) más un servicio saliente.
- Entidades JPA con base auditable (`id`, `creadoEn`, `actualizadoEn`); nombres de tabla en snake_case plural.
- Rutas `/api/v1/...`; Swagger en todos los endpoints.
- `GlobalExceptionHandler`, `application-dev.properties` / `application-prod.properties`.

### 4.2 Bounded contexts

| Contexto | Responsabilidad | Agregados / VO principales |
|---|---|---|
| `compartido` | Kernel compartido: `Resultado`, `ErrorAplicacion`, base auditable, manejo de excepciones, OpenAPI, **`RelojSistema`** (fecha simulada), `Dinero`/`Tasa` utilitarios. | — |
| `identidad` | Usuarios, roles, login JWT, hashing BCrypt, seed de roles y admin. | `Usuario`, `Rol`; VO `NombreUsuario` |
| `tiendas` | Alta/baja de tiendas, logo, slug, tipo de cambio, dueño. | `Tienda`; VO `Slug`, `TiendaId` |
| `catalogo` | Productos por tienda, precios contado y crédito, "permite cuotas", imagen. | `Producto`; VO `PrecioProducto`, `NombreImagen` |
| `clientes` | Clientes de la tienda y sus **condiciones de crédito**. | `Cliente`; VO `Dni`, `Telefono`, `CondicionesCredito`, `Tasa` |
| `credito` | **Núcleo financiero**: ventas fiadas, obligaciones, plan francés, pagos, mora, condonaciones. | `VentaCredito`, `Obligacion`, `PlanCuotas`, `Cuota`, `Pago`; servicio de dominio `CalculadoraFinanciera` |
| `estadocuenta` | Estados de cuenta al corte, PDF, mensajes simulados de WhatsApp. | `EstadoCuenta`, `LineaEstadoCuenta`, `MensajeWhatsApp` |
| `analitica` | Dashboard y reportes (solo consultas). | vistas de lectura |
| `auditoria` | Registro de todas las operaciones (quién, qué, cuándo, antes/después). | `RegistroAuditoria` |

**Comunicación entre contextos**
- `credito` ← `clientes` (condiciones), `catalogo` (precios), `tiendas` (tipo de cambio): por fachada ACL de lectura.
- `identidad` ← `clientes` y `tiendas`: crean usuarios vía `IdentidadContextoFachada`.
- `auditoria` escucha **eventos de dominio** publicados por los demás contextos (`OperacionRealizadaEvento`). No hay dependencia inversa.
- `estadocuenta` escucha `CorteCicloEvento` y `PagoRegistradoEvento` de `credito`.

### 4.3 Frontend — `src/app/<contexto>/`

Copiado de `safestep-frontend`:

```
<contexto>/
├── dominio/modelo/*.entidad.ts
├── infraestructura/
│   ├── *-api-endpoint.ts   (extienden BaseApiEndpoint)
│   ├── *.ensamblador.ts
│   ├── *-respuesta.ts
│   └── <contexto>-api.ts   (fachada, extiende BaseApi)
├── aplicacion/<contexto>-store.ts   (signals + computed)
└── presentacion/
    ├── vistas/<vista>/ (componentes standalone)
    └── <contexto>.rutas.ts (carga perezosa)
```

Contextos del frontend: `identidad`, `tiendas`, `catalogo`, `clientes`, `credito`, `estadocuenta`, `analitica`, `auditoria`, `compartido` (shell con menú, componente `ayuda-campo`, guards, interceptor JWT, formato de moneda).

## 5. Modelo de datos (PostgreSQL)

Tablas (snake_case plural), todas con `id`, `creado_en`, `actualizado_en`:

| Tabla | Campos clave |
|---|---|
| `tiendas` | nombre, slug (único), logo (nombre de archivo), ruc, direccion, telefono, tipo_cambio_usd, activa |
| `usuarios` | nombre_usuario (único), clave_hash, rol_id, tienda_id (nulo para admin), activo |
| `roles` | nombre (`ADMIN_SISTEMA`, `VENDEDOR`, `CLIENTE`) |
| `productos` | tienda_id, nombre, marca, proveedor, descripcion, unidad_medida, imagen, precio_contado, precio_credito, permite_cuotas, activo |
| `clientes` | tienda_id, usuario_id, dni, nombres, apellidos, telefono, direccion, moneda, limite_credito, max_meses, dia_corte, hora_corte, dia_pago, tipo_tasa (`EFECTIVA`/`NOMINAL`), tasa_compensatoria, capitalizacion (`DIARIA`/`MENSUAL`), tipo_tasa_mora, tasa_mora, capitalizacion_mora, activo. Único (`tienda_id`, `dni`) |
| `ventas_credito` | tienda_id, cliente_id, fecha_hora, moneda, total, tipo_cambio_aplicado |
| `lineas_venta` | venta_id, producto_id, nombre_producto, cantidad, precio_unitario, subtotal |
| `obligaciones` | venta_id, cliente_id, modalidad (`FIN_DE_MES`/`CUOTAS`), monto_capital, fecha_compra, fecha_pago_ciclo, num_cuotas, dias_gracia, capital_capitalizado, estado |
| `cuotas` | obligacion_id, numero, fecha_vencimiento, cuota, interes, amortizacion, saldo, estado, fecha_pago_real, interes_mora, total_pagado |
| `pagos` | obligacion_id, cuota_id (nulo), fecha_pago, monto_mora, monto_interes, monto_capital, total |
| `condonaciones` | obligacion_id, motivo, fecha, monto_condonado |
| `estados_cuenta` | cliente_id, ciclo (fecha de corte), fecha_corte, fecha_pago, total_capital, total_interes, total_mora, total_a_pagar, estado |
| `lineas_estado_cuenta` | estado_cuenta_id, tipo (`COMPRA`/`INTERES_COMPENSATORIO`/`MORA`/`CUOTA`), fecha, descripcion, monto |
| `mensajes_whatsapp` | cliente_id, telefono, contenido, tipo, fecha_envio, estado (simulado) |
| `auditoria` | tienda_id, usuario, rol, accion, entidad, entidad_id, detalle_antes, detalle_despues (JSON), fecha_hora |
| `configuracion_sistema` | reloj_simulado_activo, fecha_simulada |

## 6. Reglas financieras

> Todas las de esta sección son **[REVISAR]**: son las decisiones de la IA para las preguntas 21 a 32 y para las ambigüedades del enunciado. El grupo puede cambiarlas.

### 6.1 Convenciones (Anexo A del enunciado)
- Base 360 días; mes comercial de 30 días para el método francés.
- **Dinero:** 2 decimales, redondeo `HALF_UP`. **Tasas:** se guardan con al menos 7 decimales (internamente 9).
- Zona horaria: `America/Lima`.
- Días transcurridos entre dos fechas: días calendario reales (compra → pago, pago → cobro). **[REVISAR]**

### 6.2 Conversión de tasas
- Efectiva anual (TEA) a tasa por `d` días: `i(d) = (1 + TEA)^(d/360) − 1`
- Nominal anual (TNA) con capitalización cada `k` días (diaria: k=1; mensual: k=30): `TEA = (1 + TNA · k/360)^(360/k) − 1`
- Tasa efectiva mensual (30 días): `i_m = (1 + TEA)^(30/360) − 1`
- Se aplica la misma conversión a la tasa moratoria.

### 6.3 Ciclo de crédito (corte y pago)
- Cada cliente tiene `dia_corte` (1–28), `hora_corte` (por defecto 23:59) y `dia_pago` (1–28).
- Una compra hecha **hasta** el corte del mes M (fecha y hora) pertenece al ciclo de M. Una compra **posterior** al corte pertenece al ciclo siguiente.
- `fecha_pago_ciclo`: si `dia_pago > dia_corte`, es el día de pago del mismo mes del corte; si no, del mes siguiente. Si cae en feriado o domingo, **no se traslada** (simplificación). **[REVISAR]**
- Ejemplo del enunciado: corte día 20, pago día 26. Compra del 15/09 → ciclo de septiembre, `fecha_pago_ciclo` = 26/09 (11 días).

### 6.4 Compra a fin de mes (`FIN_DE_MES`)
- `dias = fecha_pago_ciclo − fecha_compra`
- Interés compensatorio = `capital · ((1 + TEA)^(dias/360) − 1)`
- Al llegar la fecha de pago, la obligación de cada compra del ciclo vence ese día. El cliente paga **el total del ciclo** en una sola operación.
- **Mora:** si no hay pago en `fecha_pago_ciclo`, corren días de mora hasta el pago real.
  `mora = (capital + interés compensatorio) · ((1 + TEA_mora)^(dias_mora/360) − 1)`
  Mientras hay mora **no se siguen acumulando** intereses compensatorios; solo se acumula la mora, que se muestra como línea separada "Intereses por mora". **[REVISAR]**
- Orden de imputación (aunque el pago sea total, se registra el desglose): **1) mora, 2) interés compensatorio, 3) capital**.

### 6.5 Compra en cuotas (`CUOTAS`)
- Solo si el producto lo permite y `num_cuotas ≤ max_meses` del cliente.
- Método francés vencido, ordinario, meses de 30 días.
- **Periodo de gracia total:** desde `fecha_compra` hasta `fecha_pago_ciclo` (la 1.ª cuota se paga **el mes siguiente** a esa fecha).
  `capital_capitalizado = capital · (1 + TEA)^(dias_gracia/360)`
- Cuota: `C = P' · i_m · (1+i_m)^n / ((1+i_m)^n − 1)`, con `P' = capital_capitalizado`, `n` = número de cuotas.
- Cada cuota: `interés_k = saldo_{k−1} · i_m`; `amortización_k = C − interés_k`; `saldo_k = saldo_{k−1} − amortización_k`.
  La última cuota absorbe el residuo del redondeo.
- Ejemplo del enunciado: compra el 15/09, pago día 26, 3 cuotas → 11 días de gracia; cuotas el 26/10, 26/11 y 26/12.
- Cada cuota se paga **completa**, una por una (sin pagos parciales). Si vence sin pago: mora sobre la cuota, `cuota · ((1 + TEA_mora)^(dias_mora/360) − 1)`.

### 6.6 Límite de crédito
- Se compara contra la **deuda pendiente**: capital de todas las obligaciones no pagadas ni condonadas (incluye capital de cuotas futuras) + monto de la venta nueva. **[REVISAR]**
- Si excede el límite, la venta se rechaza (`reglaDeNegocio`).
- Un cliente **con mora** no puede recibir crédito nuevo hasta pagar. **[REVISAR]**

### 6.7 Moneda
- El producto se vende en soles (PEN). Si el cliente tiene crédito en USD, se convierte con el `tipo_cambio_usd` de la tienda, que se congela en la venta. **[REVISAR]**

### 6.8 Pagos, condonaciones y anulaciones
- No hay pagos parciales ni anulación de ventas.
- El vendedor puede **condonar** una obligación (o todas las deudas de un cliente) con motivo obligatorio. Queda en el historial con estado `CONDONADA` y en la auditoría. No se borra nada físicamente.
- Baja de cliente/producto/tienda: **lógica**.

### 6.9 Corte y estado de cuenta
- Un proceso programado (`@Scheduled`, cada hora) detecta cortes vencidos según el reloj del sistema y genera el `EstadoCuenta` de cada cliente con compras en el ciclo. El vendedor también puede **regenerarlo** manualmente.
- El estado de cuenta lista las compras **ordenadas por fecha**, el interés compensatorio de cada compra y, si hay mora, una línea aparte "Intereses por mora". Se descarga como **PDF**.
- Se genera un mensaje de **WhatsApp simulado** (guardado en BD y visible en la pantalla del vendedor) al generar el estado de cuenta y en el día de vencimiento.

### 6.10 Reloj simulado
- `RelojSistema` devuelve la hora real o la simulada según `configuracion_sistema`. Todo el dominio usa `RelojSistema`, nunca `LocalDate.now()` directo. El administrador del sistema cambia la fecha desde `/admin/reloj` para mostrar cortes, mora y cuotas en la exposición.

## 7. Seguridad

- **Login staff:** `POST /api/v1/autenticacion/iniciar-sesion` con `usuario` y `clave` (admin y vendedor).
- **Login cliente:** `POST /api/v1/autenticacion/cliente` con `slugTienda`, `dni` y `clave`. Internamente el usuario es `<tiendaId>:<dni>`, así el mismo DNI puede ser cliente de dos tiendas con claves distintas.
- JWT con `rol` y `tiendaId`. Toda consulta del vendedor/cliente se filtra por el `tiendaId` **del token**, nunca por un parámetro del cliente.
- La clave del cliente la genera el sistema al crearlo (o al regenerarla), se muestra **una sola vez** al vendedor y se guarda cifrada (BCrypt). El vendedor se la envía por teléfono.
- CORS restringido al frontend.

## 8. Datos de demostración (seed)

- 1 administrador del sistema.
- 2 tiendas con su vendedor; catálogos de 30 y 12 productos (imágenes: los nombres de archivo se listarán para que ustedes las descarguen a `assets/productos/`).
- 8–10 clientes en la tienda principal, con casos para demostrar:
  1. Compra a fin de mes **antes** del corte (interés por días hasta el pago).
  2. Compra a fin de mes **después** del corte (pasa al mes siguiente).
  3. Compra en 3 cuotas antes del corte con **periodo de gracia** (caso del enunciado: 15/09, corte 20, pago 26).
  4. Compra en cuotas después del corte.
  5. Cliente con **mora** en fin de mes.
  6. Cliente con **cuota vencida** en mora.
  7. Cliente **al límite de crédito** (rechazo de venta nueva).
  8. Cliente en **USD**.
  9. Deuda **condonada** y deuda **pagada**.
  10. Tasa **nominal** vs **efectiva**.
- Estos casos coinciden con los **2 juegos de datos de prueba** del informe (Hito 1) y con las pruebas unitarias.

## 9. Pruebas

- **Unitarias (dominio):** `CalculadoraFinanciera` (conversión TNA/TEA, interés por días, capitalización de la gracia, cuota francesa, cronograma, mora, redondeo), reglas del ciclo (antes/después del corte), límite de crédito, prelación de pagos.
- **Integración:** servicios de aplicación con PostgreSQL (Testcontainers o perfil de prueba), seguridad por rol y por tienda.
- **Frontend:** stores y ensambladores con Vitest; recorrido manual de los flujos principales.
- Los cálculos de referencia se validarán a mano (hoja de cálculo) para cada juego de datos.

## 10. Orden de construcción

| Fase | Contenido |
|---|---|
| 0 | Crear `bodega-backend` y `bodega-frontend`, kernel `compartido`, configuración, Dockerfile. |
| 1 | `identidad` + `tiendas` (login, JWT, alta de tiendas) y su frontend (login, shell). |
| 2 | `catalogo` (CRUD, seed de 30 productos) y su frontend (vendedor y vista pública). |
| 3 | `clientes` (condiciones de crédito, clave) y su frontend. |
| 4 | `credito`: `CalculadoraFinanciera` con pruebas, ventas, obligaciones, planes, pagos, mora, condonación. Frontend de venta y cobranzas. |
| 5 | `estadocuenta`: corte, PDF, WhatsApp simulado; portal del cliente. |
| 6 | `auditoria` y reloj simulado. |
| 7 | `analitica`: dashboard y reportes. |
| 8 | Pulido: ayuda por campo en todos los formularios, responsive, seed final, pruebas de extremo a extremo, guía de despliegue en Render. |

## 11. Pendiente del grupo

- Descargar las imágenes de los productos (lista de nombres de archivo en la fase 2).
- Revisar todas las marcas **[REVISAR]**.
- Completar el registro de uso de IA (`REGISTRO_USO_IA.md`).

## 12. Cambios respecto al plan original (registrados durante la construcción)

- **Roles:** cada usuario tiene un único rol (columna `rol` en `usuarios`); no existe la tabla `roles`.
- **Login del cliente:** el frontend obtiene primero la tienda por su identificador de URL y envía `tiendaId + DNI + clave`.
- **Modalidad CONTADO:** las líneas al contado se registran en la venta pero no generan deuda.
- **Estados de cuenta:** un estado de cuenta incluye las compras a fin de mes que vencen en la fecha de pago, las cuotas que vencen esa fecha, las deudas vencidas de ciclos anteriores y la mora a la fecha de generación; las compras en cuotas del ciclo se listan como informativas. Se regeneran cada hora (mora al día) y al registrar un pago.
- **Registro de pagos:** el vendedor paga el ciclo (todas las compras a fin de mes con la misma fecha de pago) o una cuota (en orden) desde la ficha del cliente.
- **Frontend:** las plantillas y estilos de los componentes son inline (un archivo `.ts` por componente) y las fachadas de API son servicios por contexto en lugar de una clase por endpoint.
- **Java:** se compila con `release 25`; el proyecto también ejecuta con JDK 26.
