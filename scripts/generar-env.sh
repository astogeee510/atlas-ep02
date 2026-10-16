#!/usr/bin/env bash
# Crea infra/.env con secretos aleatorios para el ambiente QA (no se versiona).
set -euo pipefail
DESTINO="$(dirname "$0")/../infra/.env"
if [ -f "$DESTINO" ]; then
  echo "Ya existe $DESTINO (no se sobrescribe)."
  exit 0
fi
cat > "$DESTINO" <<VARS
ATLAS_DB_USER=atlas
ATLAS_DB_PASSWORD=$(openssl rand -hex 16)
ATLAS_JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')
ATLAS_CRYPTO_KEY=$(openssl rand -base64 32 | tr -d '\n')
VARS
chmod 600 "$DESTINO"
echo "Creado $DESTINO"
