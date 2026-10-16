#!/usr/bin/env bash
# NFR-SEG-7: respaldo de la base de datos de Atlas (CP-SEG-04).
# Uso: bash scripts/backup.sh   ->  backups/atlas_AAAAMMDD_HHMMSS.sql.gz
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p backups
ARCHIVO="backups/atlas_$(date +%Y%m%d_%H%M%S).sql.gz"
docker compose -f infra/docker-compose.yml exec -T db \
  sh -c 'pg_dump -U "$POSTGRES_USER" -d atlas --no-owner' | gzip > "$ARCHIVO"
echo "Respaldo creado: $ARCHIVO ($(du -h "$ARCHIVO" | cut -f1))"
CLIENTES=$(docker compose -f infra/docker-compose.yml exec -T db \
  sh -c 'psql -U "$POSTGRES_USER" -d atlas -At -c "SELECT COUNT(*) FROM cliente"')
echo "Clientes en la base al momento del respaldo: $CLIENTES"
