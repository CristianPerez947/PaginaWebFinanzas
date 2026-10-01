# Frontend — Cuaderno de Fiado Digital

Angular 21 + Angular Material. Requisitos: Node 20+ y npm.

## Levantar

```bash
npm install
npm start          # http://localhost:4200 (el backend debe estar en http://localhost:8080)
```

La URL del API se configura en `public/config.json` (`apiUrl`), sin recompilar.

## Otros comandos

```bash
npx ng test --watch=false   # pruebas unitarias
npx ng build                # compilación de producción en dist/
```

Imágenes de productos: `public/assets/productos/`; logos: `public/assets/logos/` (ver los `LEEME.txt`).
Usuarios de demostración y despliegue: ver el [README principal](../README.md).
