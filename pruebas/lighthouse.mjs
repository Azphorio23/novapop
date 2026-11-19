// Lighthouse por módulo: rendimiento, accesibilidad, buenas prácticas y SEO de cada página de NovaPop,
// en celular (con red y CPU limitadas, como PageSpeed) y en escritorio.
//
// Uso:  npm run lighthouse
//       NOVAPOP_URL=https://novapop.onrender.com npm run lighthouse
//       (panel de administración: NOVAPOP_ADMIN_USUARIO y NOVAPOP_ADMIN_CLAVE)
//
// Resultado: reportes/lighthouse/<modulo>-<celular|escritorio>.html y reportes/lighthouse/index.html (resumen).
// Las páginas con sesión se miden iniciando sesión antes en el mismo navegador (Playwright + Lighthouse).
import { chromium } from '@playwright/test';
import lighthouse from 'lighthouse';
import escritorio from 'lighthouse/core/config/desktop-config.js';
import { mkdirSync, writeFileSync, rmSync } from 'node:fs';

const BASE = (process.env.NOVAPOP_URL || 'http://localhost:8080').replace(/\/$/, '');
const CORREO = process.env.NOVAPOP_CORREO || 'demo@novapop.pe';
const CLAVE = process.env.NOVAPOP_CLAVE || 'NovaPop2026!';
const ADMIN = process.env.NOVAPOP_ADMIN_USUARIO && process.env.NOVAPOP_ADMIN_CLAVE
  ? { usuario: process.env.NOVAPOP_ADMIN_USUARIO, clave: process.env.NOVAPOP_ADMIN_CLAVE } : null;
const PUERTO = 9333;
const SALIDA = 'reportes/lighthouse';

// [clave, nombre del módulo, ruta, sesión que necesita]
const MODULOS = [
  ['landing', 'Landing (público)', '/', null],
  ['login', 'Inicio de sesión', '/login.html', null],
  ['registro', 'Registro', '/registro.html', null],
  ['perfiles', '¿Quién está viendo?', '/perfiles.html', 'cliente'],
  ['inicio', 'Inicio / catálogo', '/inicio.html', 'cliente'],
  ['peliculas', 'Películas (grilla)', '/inicio.html?tipo=PELICULA', 'cliente'],
  ['titulo', 'Ficha del título', '/titulo.html?id=3', 'cliente'],
  ['reproductor', 'Reproductor', '/ver.html?id=1', 'cliente'],
  ['cuenta', 'Mi cuenta', '/cuenta.html', 'cliente'],
  ['admin-login', 'Panel: login', '/admin/login.html', null],
  ['admin-tablero', 'Panel: tablero', '/admin/', 'admin'],
  ['admin-contenidos', 'Panel: películas y series', '/admin/#/contenidos', 'admin'],
];
const CATEGORIAS = [['performance', 'Rendimiento'], ['accessibility', 'Accesibilidad'], ['best-practices', 'Buenas prácticas'], ['seo', 'SEO']];

rmSync(SALIDA, { recursive: true, force: true });
mkdirSync(SALIDA, { recursive: true });
rmSync('reportes/.perfil-lighthouse', { recursive: true, force: true });
const navegador = await chromium.launchPersistentContext('reportes/.perfil-lighthouse', {
  channel: 'chrome', headless: true, args: [`--remote-debugging-port=${PUERTO}`],
});
const pagina = navegador.pages()[0] || await navegador.newPage();

// ---------------------------------------------------------------- sesiones
async function post(ruta, datos) {
  const r = await pagina.request.post(BASE + ruta, { data: datos });
  if (!r.ok()) throw new Error(`${ruta} respondió ${r.status()}`);
  return r.json();
}
const cliente = await post('/api/auth/login', { correo: CORREO, clave: CLAVE });
const perfiles = await (await pagina.request.get(BASE + '/api/perfiles', { headers: { Authorization: 'Bearer ' + cliente.token } })).json();
const perfil = perfiles.find(p => !p.tienePin && !p.esInfantil);
const admin = ADMIN ? await post('/api/admin/auth/login', ADMIN) : null;

/** Deja la sesión en localStorage antes de cada medición (perfiles.html, por ejemplo, olvida el perfil). */
async function prepararSesion(tipo) {
  await pagina.goto(BASE + '/offline.html');
  await pagina.evaluate(([t, c, p, a]) => {
    localStorage.clear();
    if (t === 'cliente') {
      localStorage.setItem('np_token', c.token);
      localStorage.setItem('np_correo', c.correo);
      localStorage.setItem('np_perfil', JSON.stringify({ id: p.id, nombre: p.nombre, avatar: p.avatar, esInfantil: p.esInfantil }));
    } else if (t === 'admin') {
      localStorage.setItem('np_admin_token', a.token);
      localStorage.setItem('np_admin_usuario', JSON.stringify({ usuario: a.usuario, nombres: a.nombres, rol: a.rol }));
    }
  }, [tipo, cliente, perfil, admin]);
}

// ---------------------------------------------------------------- mediciones
const resultados = [];
for (const [clave, nombre, ruta, sesion] of MODULOS) {
  if (sesion === 'admin' && !admin) { console.log(`- ${nombre}: omitido (faltan las credenciales del panel)`); continue; }
  for (const [dispositivo, config] of [['celular', undefined], ['escritorio', escritorio]]) {
    await prepararSesion(sesion);
    const r = await lighthouse(BASE + ruta, {
      port: PUERTO, output: 'html', logLevel: 'error', disableStorageReset: true, locale: 'es',
      onlyCategories: CATEGORIAS.map(c => c[0]),
    }, config);
    writeFileSync(`${SALIDA}/${clave}-${dispositivo}.html`, r.report);
    const notas = Object.fromEntries(CATEGORIAS.map(([c]) => [c, Math.round((r.lhr.categories[c]?.score ?? 0) * 100)]));
    const metricas = {
      fcp: r.lhr.audits['first-contentful-paint']?.displayValue,
      lcp: r.lhr.audits['largest-contentful-paint']?.displayValue,
      tbt: r.lhr.audits['total-blocking-time']?.displayValue,
      cls: r.lhr.audits['cumulative-layout-shift']?.displayValue,
    };
    // Qué mejorar: auditorías que no pasaron en cada categoría (sin las informativas)
    const mejorar = [...new Set(CATEGORIAS.flatMap(([c]) => (r.lhr.categories[c]?.auditRefs || [])
      .map(a => r.lhr.audits[a.id])
      .filter(a => a && a.score !== null && a.score < 0.9 && !['informative', 'manual', 'notApplicable'].includes(a.scoreDisplayMode))
      .map(a => a.title)))];
    resultados.push({ clave, nombre, ruta, dispositivo, notas, metricas, mejorar, archivo: `${clave}-${dispositivo}.html` });
    console.log(`- ${nombre} (${dispositivo}): ` + CATEGORIAS.map(([c, n]) => `${n} ${notas[c]}`).join(' · '));
  }
}

// El reproductor registra este navegador como dispositivo de la cuenta demo: se quita al terminar
await prepararSesion('cliente');
const idDispositivo = await pagina.evaluate(() => Object.entries(localStorage).find(([k]) => k.startsWith('np_dispositivo_'))?.[1]);
await navegador.close();
if (idDispositivo) {
  await fetch(`${BASE}/api/cuenta/dispositivos/${idDispositivo}`, { method: 'DELETE', headers: { Authorization: 'Bearer ' + cliente.token } }).catch(() => {});
}

// ---------------------------------------------------------------- resumen
writeFileSync(`${SALIDA}/resumen.json`, JSON.stringify({ base: BASE, fecha: new Date().toISOString(), resultados }, null, 2));
const color = n => n >= 90 ? '#0c7d48' : n >= 50 ? '#b26a00' : '#c1121f';
const celda = n => `<td style="color:${color(n)};font-weight:700">${n}</td>`;
writeFileSync(`${SALIDA}/index.html`, `<!doctype html><html lang="es"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1"><title>Lighthouse · NovaPop</title>
<style>body{font-family:system-ui,sans-serif;margin:32px;color:#1a1622}table{border-collapse:collapse;width:100%}
th,td{padding:8px 10px;border-bottom:1px solid #e5e1d8;text-align:left;font-size:14px}th{background:#f6f3ec}
td small{color:#6b6577}a{color:#7a4f00}</style></head><body>
<h1>Lighthouse por módulo — NovaPop</h1>
<p>${BASE} · ${new Date().toLocaleString('es-PE')} · Notas de 0 a 100 (verde ≥ 90, naranja ≥ 50, rojo &lt; 50).
Celular = red y CPU limitadas, como PageSpeed Insights.</p>
<table><tr><th>Módulo</th><th>Dispositivo</th>${CATEGORIAS.map(c => `<th>${c[1]}</th>`).join('')}<th>FCP</th><th>LCP</th><th>TBT</th><th>CLS</th><th>Informe</th></tr>
${resultados.map(r => `<tr><td>${r.nombre}<br><small>${r.ruta}</small></td><td>${r.dispositivo}</td>${CATEGORIAS.map(([c]) => celda(r.notas[c])).join('')}
<td>${r.metricas.fcp ?? ''}</td><td>${r.metricas.lcp ?? ''}</td><td>${r.metricas.tbt ?? ''}</td><td>${r.metricas.cls ?? ''}</td>
<td><a href="${r.archivo}">ver</a></td></tr>
${r.mejorar.length ? `<tr><td colspan="10" style="padding-top:0"><small>Mejorar: ${r.mejorar.join(' · ')}</small></td></tr>` : ''}`).join('\n')}
</table></body></html>`);
console.log(`\nResumen: ${SALIDA}/index.html`);
