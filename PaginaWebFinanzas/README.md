# Cuaderno de Fiado Digital

Aplicación web para el control de créditos (fiado) de bodegas y pequeños comercios. Trabajo final de **SI642 Finanzas e Ingeniería Económica (2026-20)**.

- `bodega-backend/` — API REST en Java 25 + Spring Boot 4 (arquitectura DDD) + PostgreSQL.
- `bodega-frontend/` — Angular 21 + Angular Material (misma organización por contextos).
- `PLAN.md` — plan de desarrollo, reglas financieras y decisiones **[REVISAR]**.

## Inicio rápido (sin instalar PostgreSQL)

Abra **dos terminales**:

```bash
# Terminal 1 - backend (base de datos en memoria, con datos de demostración)
cd bodega-backend
mvn spring-boot:run "-Dspring-boot.run.profiles=h2"
```

```bash
# Terminal 2 - frontend
cd bodega-frontend
npm install
npm start
```

Luego abra <http://localhost:4200>. Requisitos: JDK 25 o superior, Maven 3.9+ y Node 20+.

## Ejecutar en local (con PostgreSQL)

### 1. Base de datos (PostgreSQL)
Cree una base de datos llamada `bodega` y defina las variables de entorno (ver `bodega-backend/.env.example`):

```
DATABASE_URL=localhost   DATABASE_PORT=5432   DATABASE_NAME=bodega
DATABASE_USER=postgres   DATABASE_PASSWORD=<su contraseña>
```

Las tablas se crean solas al iniciar. Para probar **sin PostgreSQL** use la base en memoria: `--spring.profiles.active=h2`.

### 2. Backend
```bash
cd bodega-backend
mvn spring-boot:run            # con PostgreSQL
mvn spring-boot:run "-Dspring-boot.run.profiles=h2"   # sin PostgreSQL (memoria)
```
API en <http://localhost:8080> · documentación Swagger en <http://localhost:8080/swagger-ui.html>.
La primera vez se cargan el administrador y los **datos de demostración** (desactivar con `SEMILLA_DEMO=false`).

### 3. Frontend
```bash
cd bodega-frontend
npm install
npm start                      # http://localhost:4200
```
La URL del API se lee de `public/config.json` (`apiUrl`), sin recompilar.

## Datos de demostración

| Rol | Usuario | Clave |
|---|---|---|
| Administrador del sistema | `admin` | `admin123` |
| Vendedor — La Tiendita de Don Pepe | `pepe` | `pepe123` |
| Vendedor — Bodega Doña Rosa | `rosa` | `rosa123` |
| Clientes (ingresan por la página de su bodega con DNI + clave) | p. ej. DNI `45871236` (María), `40219876` (Juan) | `123456` |

Páginas de cliente: `/t/tiendita-don-pepe` y `/t/bodega-dona-rosa`. Casos incluidos en Don Pepe: compra a fin de mes pagada y pendiente, plan de 3 cuotas con una cuota pagada a tiempo, una con mora pagada y una vencida (caso de periodo de gracia del enunciado), cliente en mora, compra tras el corte, crédito en dólares, deuda condonada, tasa nominal con capitalización diaria/mensual, cliente al límite y cliente dado de baja.

## Imágenes
Coloque las imágenes de los productos en `bodega-frontend/public/assets/productos/` y los logos en `public/assets/logos/`. Los nombres de archivo esperados por los datos de demostración están en los `LEEME.txt` de esas carpetas. Si falta una imagen se muestra un ícono.

## Pruebas
```bash
cd bodega-backend && mvn test           # fórmulas financieras (caso del enunciado, 11 pruebas)
cd bodega-frontend && npx ng test --watch=false
```

## Despliegue en Render (manual)
1. **Base de datos:** cree una base PostgreSQL. (La versión gratuita de Render caduca a los 30 días; valide el plan antes de la sustentación.)
2. **Backend** (Web Service, Docker, carpeta `bodega-backend`): variables `DATABASE_URL` (host), `DATABASE_PORT`, `DATABASE_NAME`, `DATABASE_USER`, `DATABASE_PASSWORD`, `JWT_SECRET` (mínimo 32 caracteres), `CORS_ORIGENES` (URL del frontend), `ADMIN_USUARIO` y `ADMIN_CLAVE` (cámbielas), y `SEMILLA_DEMO=true` solo si quiere los datos de ejemplo.
3. **Frontend** (Web Service, Docker, carpeta `bodega-frontend`): variable `API_URL` = `https://<su-backend>.onrender.com/api/v1` y opcionalmente `MOSTRAR_DEMO=false` para ocultar los usuarios de demostración.

## Arquitectura (DDD)
Un paquete/carpeta por *bounded context*: `identidad`, `tiendas`, `catalogo`, `clientes`, `credito` (núcleo financiero), `estadocuenta`, `analitica`, `auditoria` y `compartido`. En el backend cada contexto tiene las capas `dominio`, `aplicacion`, `infraestructura` e `interfaces` (comandos/consultas, servicios `Resultado<T, Error>`, puertos y adaptadores, ensambladores, fachadas ACL y eventos de dominio). El frontend organiza cada contexto en `dominio/modelo`, `infraestructura` (fachada de API), `aplicacion` (stores con signals) y `presentacion`.

Las fórmulas están en `credito/dominio/servicios/CalculadoraFinanciera.java` (base 360 días, mes de 30 días, dinero con 2 decimales, tasas con 9 decimales internos).
