#!/usr/bin/env bash
# Publica las ultimas lineas de un archivo como anotacion de GitHub Actions (visible en la API de la ejecucion).
# Uso: bash scripts/ci-anotar.sh "Titulo" archivo [lineas=60] [nivel=error]
TITULO=${1:-Detalle}
ARCHIVO=${2:-}
LINEAS=${3:-60}
NIVEL=${4:-error}
if [ -z "$ARCHIVO" ] || [ ! -f "$ARCHIVO" ]; then
  echo "::$NIVEL title=$TITULO::(no existe el archivo $ARCHIVO)"
  exit 0
fi
CONTENIDO=$(tail -n "$LINEAS" "$ARCHIVO" | sed 's/\x1b\[[0-9;]*m//g' | sed 's/%/%25/g' | sed ':a;N;$!ba;s/\r//g;s/\n/%0A/g')
echo "::$NIVEL title=$TITULO::$CONTENIDO"
