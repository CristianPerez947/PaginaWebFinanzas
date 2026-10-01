# Cuaderno de Fiado Digital - frontend (Angular) servido con nginx.
# Variables de entorno en Render:
#   API_URL       URL pública del backend, por ejemplo https://bodega-api.onrender.com/api/v1
#   MOSTRAR_DEMO  true/false: muestra los usuarios de demostración en la pantalla de ingreso (por defecto true)
#   PORT          puerto (Render lo define; por defecto 80)
FROM node:24-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npx ng build --configuration production

FROM nginx:1.27-alpine AS runtime
ENV PORT=80 API_URL=http://localhost:8080/api/v1 MOSTRAR_DEMO=true
COPY docker/nginx.conf.template /etc/nginx/templates/default.conf.template
COPY docker/40-config.sh /docker-entrypoint.d/40-config.sh
RUN chmod +x /docker-entrypoint.d/40-config.sh
COPY --from=build /app/dist/bodega-frontend/browser /usr/share/nginx/html
EXPOSE 80
