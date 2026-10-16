#!/usr/bin/env bash
# NFR-DIS-1: mide la disponibilidad consultando /actuator/health cada N segundos (CP-DIS-01).
# Uso: bash scripts/uptime.sh [duracion_minutos=60] [intervalo_segundos=10] [url]
set -uo pipefail
DURACION=${1:-60}
INTERVALO=${2:-10}
URL=${3:-https://localhost/actuator/health}
FIN=$(( $(date +%s) + DURACION * 60 ))
OK=0; TOTAL=0
echo "Midiendo $URL durante $DURACION min (cada $INTERVALO s). Inicio: $(date '+%d-%m-%Y %H:%M:%S')"
while [ "$(date +%s)" -lt "$FIN" ]; do
  TOTAL=$((TOTAL + 1))
  if curl -ks --max-time 5 "$URL" | grep -q '"UP"'; then OK=$((OK + 1)); else echo "  $(date '+%H:%M:%S') CAIDA"; fi
  sleep "$INTERVALO"
done
PORCENTAJE=$(awk "BEGIN { printf \"%.2f\", ($OK / $TOTAL) * 100 }")
echo "Fin: $(date '+%d-%m-%Y %H:%M:%S') | chequeos=$TOTAL ok=$OK | disponibilidad=$PORCENTAJE % (objetivo 99,5 %)"
