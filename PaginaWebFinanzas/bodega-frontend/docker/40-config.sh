#!/bin/sh
# Genera config.json con la URL del API al iniciar el contenedor.
cat > /usr/share/nginx/html/config.json <<JSON
{
  "apiUrl": "${API_URL}",
  "mostrarDemo": ${MOSTRAR_DEMO}
}
JSON
