#!/usr/bin/env bash
# Resume un reporte JSON de Trivy como anotacion y opcionalmente falla si hay hallazgos.
# Uso: bash scripts/ci-trivy-resumen.sh reporte.json "nombre-imagen" [bloquear=si|no]
ARCHIVO=$1
NOMBRE=$2
BLOQUEAR=${3:-si}
N=$(jq '[.Results[]?.Vulnerabilities[]?] | length' "$ARCHIVO")
DETALLE=$(jq -r '[.Results[]?.Vulnerabilities[]? | "\(.Severity) \(.VulnerabilityID) \(.PkgName) \(.InstalledVersion) -> \(.FixedVersion // "sin version corregida")"] | unique | join("%0A")' "$ARCHIVO")
NIVEL=notice
if [ "$N" -gt 0 ] && [ "$BLOQUEAR" = "si" ]; then NIVEL=error; fi
echo "::$NIVEL title=Trivy $NOMBRE: $N vulnerabilidades CRITICAL/HIGH corregibles::${DETALLE:-Sin hallazgos}"
if [ "$N" -gt 0 ] && [ "$BLOQUEAR" = "si" ]; then exit 1; fi
exit 0
