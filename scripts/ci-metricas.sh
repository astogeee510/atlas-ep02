#!/usr/bin/env bash
# Publica como anotacion las metricas de calidad del backend (para el informe).
# Uso: bash scripts/ci-metricas.sh pruebas|cobertura|mutacion   (desde backend/)
case "$1" in
  pruebas)
    python3 - <<'PY'
import glob, xml.etree.ElementTree as ET
t=f=e=s=0; clases=[]
for x in glob.glob('target/surefire-reports/TEST-*.xml'):
    r=ET.parse(x).getroot()
    t+=int(r.get('tests',0)); f+=int(r.get('failures',0)); e+=int(r.get('errors',0)); s+=int(r.get('skipped',0))
    clases.append(f"{r.get('name').split('.')[-1]}={r.get('tests')}")
print(f"::notice title=JUnit + Cucumber: {t} pruebas · {f} fallas · {e} errores · {s} omitidas::{', '.join(sorted(clases))}")
PY
    ;;
  cobertura)
    python3 - <<'PY'
import csv
L=Lc=B=Bc=0; paquetes={}
for r in csv.DictReader(open('target/site/jacoco/jacoco.csv')):
    lm,lc=int(r['LINE_MISSED']),int(r['LINE_COVERED']); bm,bc=int(r['BRANCH_MISSED']),int(r['BRANCH_COVERED'])
    L+=lm+lc; Lc+=lc; B+=bm+bc; Bc+=bc
    p=r['PACKAGE'].replace('cl.crealab.atlas.','')
    a=paquetes.setdefault(p,[0,0]); a[0]+=lc; a[1]+=lm+lc
det='%0A'.join(f"{p}: {c}/{t} ({100*c/t:.1f} %)" for p,(c,t) in sorted(paquetes.items()) if t)
print(f"::notice title=JaCoCo: lineas {100*Lc/L:.1f} % ({Lc}/{L}) · ramas {100*Bc/B:.1f} % ({Bc}/{B})::{det}")
PY
    ;;
  mutacion)
    python3 - <<'PY'
import xml.etree.ElementTree as ET, collections
r=ET.parse('target/pit-reports/mutations.xml').getroot()
c=collections.Counter(m.get('status') for m in r.findall('mutation'))
total=sum(c.values()); muertos=c.get('KILLED',0)+c.get('TIMED_OUT',0)+c.get('MEMORY_ERROR',0)
cub=total-c.get('NO_COVERAGE',0)
print(f"::notice title=PIT: mutation score {100*muertos/total:.1f} % ({muertos}/{total}) · test strength {100*muertos/max(cub,1):.1f} %::{dict(c)}")
PY
    ;;
esac
