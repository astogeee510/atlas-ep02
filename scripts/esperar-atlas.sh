#!/usr/bin/env bash
# Espera a que Atlas responda "UP" en el endpoint de salud (por defecto via nginx con TLS).
set -uo pipefail
URL=${1:-https://localhost/actuator/health}
for i in $(seq 1 120); do
  if curl -ksf "$URL" | grep -q '"UP"'; then
    echo "Atlas listo en $URL"
    exit 0
  fi
  sleep 2
done
echo "Atlas no respondio a tiempo en $URL" >&2
docker compose -f "$(dirname "$0")/../infra/docker-compose.yml" logs --tail=200 2>/dev/null || true
exit 1
