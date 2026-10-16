#!/bin/sh
# Genera un certificado autofirmado para el ambiente QA si no se monto uno real.
# En produccion se debe montar un certificado emitido por una CA (por ejemplo Let's Encrypt).
set -e
CERT=/etc/nginx/certs/atlas.crt
KEY=/etc/nginx/certs/atlas.key
if [ ! -f "$CERT" ] || [ ! -f "$KEY" ]; then
  echo "[atlas] Generando certificado TLS autofirmado para QA"
  openssl req -x509 -nodes -newkey rsa:2048 -days 365 \
    -keyout "$KEY" -out "$CERT" \
    -subj "/C=CL/O=CreaLab SpA/CN=localhost" \
    -addext "subjectAltName=DNS:localhost,IP:127.0.0.1"
fi
