// Genera las capturas de la interfaz para los anexos del informe (seccion 8 del plan).
// Uso (con Atlas levantado en https://localhost):
//   cd scripts/capturas && npm run instalar
//   EJECUTOR="Nombre Apellido" npm run capturar
// Cada imagen lleva impresa la fecha, la hora, la URL y el ejecutor (regla de evidencia de la guia).
import { chromium } from 'playwright';
import { mkdirSync } from 'node:fs';
import { join, resolve } from 'node:path';

const BASE = process.env.ATLAS_URL || 'https://localhost';
const EJECUTOR = process.env.EJECUTOR || 'Ejecutor sin nombre';
const CLAVE = process.env.ATLAS_CLAVE || 'Atlas#Demo2026';
const SALIDA = resolve(process.env.SALIDA || '../../evidencias');
mkdirSync(SALIDA, { recursive: true });

async function marcar(pagina) {
  const ahora = new Date().toLocaleString('es-CL', { timeZone: 'America/Santiago' });
  await pagina.evaluate(
    ({ texto }) => {
      document.getElementById('marca-evidencia')?.remove();
      const marca = document.createElement('div');
      marca.id = 'marca-evidencia';
      marca.textContent = texto;
      marca.style.cssText =
        'position:fixed;right:8px;top:8px;z-index:99999;background:#111;color:#fff;' +
        'font:14px monospace;padding:6px 10px;border-radius:6px;opacity:.9';
      document.body.appendChild(marca);
    },
    { texto: `${ahora} · ${pagina.url()} · Ejecutor: ${EJECUTOR}` },
  );
}

async function capturar(pagina, archivo) {
  await pagina.waitForLoadState('networkidle');
  await marcar(pagina);
  await pagina.screenshot({ path: join(SALIDA, archivo), fullPage: true });
  console.log('  ✔', archivo);
}

async function ingresar(pagina, usuario) {
  await pagina.goto(`${BASE}/login`);
  await pagina.getByLabel('Nombre de usuario').fill(usuario);
  await pagina.getByLabel('Contrasena').fill(CLAVE);
  await pagina.getByRole('button', { name: 'Ingresar' }).click();
  await pagina.getByRole('heading', { name: 'Resumen de la empresa' }).waitFor();
}

const navegador = await chromium.launch();
try {
  console.log(`Capturando ${BASE} -> ${SALIDA}`);
  const contexto = await navegador.newContext({ ignoreHTTPSErrors: true, viewport: { width: 1366, height: 900 } });
  const pagina = await contexto.newPage();

  await pagina.goto(`${BASE}/login`);
  await capturar(pagina, 'Anexo_0.1b_ambiente_login-https.png');

  await ingresar(pagina, 'admin.demo');
  await capturar(pagina, 'Anexo_1.1_CP-FUN-01_dashboard-admin.png');

  await pagina.goto(`${BASE}/clientes`);
  await pagina.getByLabel('Buscar por nombre').fill('a');
  await pagina.getByRole('button', { name: 'Buscar' }).click();
  await capturar(pagina, 'Anexo_1.3_CP-FUN-03_clientes-filtro.png');

  await pagina.getByRole('button', { name: /^Eliminar / }).first().click();
  await pagina.getByRole('dialog').waitFor();
  await capturar(pagina, 'Anexo_1.4_CP-FUN-04_confirmacion-eliminacion.png');
  await pagina.getByRole('button', { name: 'Cancelar' }).click();

  await pagina.goto(`${BASE}/clientes/1/contratos`);
  await pagina.getByText(/Estado:/).first().waitFor();
  await capturar(pagina, 'Anexo_1.5_CP-FUN-05_estados-contrato.png');

  await pagina.goto(`${BASE}/usuarios`);
  await pagina.getByRole('heading', { name: 'Usuarios de la empresa' }).waitFor();
  await capturar(pagina, 'Anexo_1.6_CP-FUN-06_gestion-usuarios.png');

  await pagina.goto(`${BASE}/auditoria`);
  await pagina.getByRole('table').waitFor();
  await capturar(pagina, 'Anexo_2.3_CP-INT-03_auditoria.png');

  await pagina.goto(`${BASE}/swagger-ui.html`);
  await pagina.waitForTimeout(2500);
  await capturar(pagina, 'Anexo_7.6_swagger-endpoints.png');

  const editor = await navegador.newContext({ ignoreHTTPSErrors: true, viewport: { width: 1366, height: 900 } });
  const paginaEditor = await editor.newPage();
  await ingresar(paginaEditor, 'editor.demo');
  await paginaEditor.goto(`${BASE}/clientes`);
  await paginaEditor.getByRole('heading', { name: 'Clientes' }).waitFor();
  await capturar(paginaEditor, 'Anexo_3.1b_CP-SEG-01_editor-sin-acciones.png');

  const movil = await navegador.newContext({
    ignoreHTTPSErrors: true,
    viewport: { width: 375, height: 667 },
    deviceScaleFactor: 2,
    isMobile: true,
    hasTouch: true,
  });
  const paginaMovil = await movil.newPage();
  await ingresar(paginaMovil, 'admin.demo');
  await paginaMovil.goto(`${BASE}/clientes/1/contratos`);
  await paginaMovil.getByText(/Estado:/).first().waitFor();
  await capturar(paginaMovil, 'Anexo_5.1_CP-USA-01_movil-375x667.png');
  const anchoDocumento = await paginaMovil.evaluate(() => document.documentElement.scrollWidth);
  console.log(`  Ancho del documento en movil: ${anchoDocumento}px (sin scroll horizontal si es <= 375)`);
} finally {
  await navegador.close();
}
