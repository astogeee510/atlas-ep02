#!/usr/bin/env bash
# NFR-SEG-7: restauracion de un respaldo en una base de prueba "atlas_restore" (CP-SEG-04).
# Uso: bash scripts/restore.sh backups/atlas_AAAAMMDD_HHMMSS.sql.gz
set -euo pipefail
cd "$(dirname "$0")/.."
ARCHIVO="${1:?Indique el archivo de respaldo}"
COMPOSE="docker compose -f infra/docker-compose.yml"
$COMPOSE exec -T db sh -c 'dropdb -U "$POSTGRES_USER" --if-exists atlas_restore && createdb -U "$POSTGRES_USER" atlas_restore'
gunzip -c "$ARCHIVO" | $COMPOSE exec -T db sh -c 'psql -q -U "$POSTGRES_USER" -d atlas_restore' > /dev/null
echo "Restauracion completa en atlas_restore. Comparacion de filas (original vs restaurada):"
for TABLA in usuario empresa cliente contrato documento auditoria; do
  ORIG=$($COMPOSE exec -T db sh -c "psql -U \"\$POSTGRES_USER\" -d atlas -At -c 'SELECT COUNT(*) FROM $TABLA'")
  REST=$($COMPOSE exec -T db sh -c "psql -U \"\$POSTGRES_USER\" -d atlas_restore -At -c 'SELECT COUNT(*) FROM $TABLA'")
  echo "  $TABLA: $ORIG vs $REST"
done
