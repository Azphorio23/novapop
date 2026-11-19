// "Depurador" que acompaña a cada prueba de Playwright.
// Escucha la página mientras se prueba y guarda en el informe, por cada prueba:
//   - errores de JavaScript (excepciones no capturadas),
//   - mensajes de consola de tipo error o advertencia,
//   - peticiones fallidas y respuestas HTTP 4xx/5xx.
// Al terminar, la prueba FALLA si hubo errores de JavaScript o respuestas 5xx de NovaPop.
import { test as base, expect } from '@playwright/test';
import { mkdirSync, writeFileSync } from 'node:fs';

export const test = base.extend({
  depuracion: [async ({ page, baseURL }, use, testInfo) => {
    const eventos = [];
    const origen = new URL(baseURL).origin;
    const propio = url => url.startsWith(origen);
    const anotar = (tipo, texto, url = page.url()) =>
      eventos.push({ momento: new Date().toISOString().slice(11, 23), tipo, texto, pagina: url.replace(origen, '') });

    page.on('pageerror', e => anotar('error-js', e.message));
    page.on('console', m => {
      if (m.type() === 'error' || m.type() === 'warning') anotar('consola-' + m.type(), m.text());
    });
    page.on('requestfailed', r => {
      const motivo = r.failure()?.errorText || '';
      // Los videos se piden por partes y el navegador corta las que ya no necesita: no es un error
      if (!/ERR_ABORTED/.test(motivo)) anotar('peticion-fallida', `${r.method()} ${r.url()} → ${motivo}`);
    });
    page.on('response', r => {
      if (r.status() >= 400) anotar('http-' + r.status(), `${r.request().method()} ${r.url().replace(origen, '')}`);
    });

    // Las pruebas pueden declarar respuestas que esperan a propósito (p. ej. 401 de un login incorrecto)
    const esperados = [];
    await use({ eventos, esperar: patron => esperados.push(patron) });

    const resumen = eventos.length
      ? eventos.map(e => `[${e.momento}] ${e.tipo.padEnd(18)} ${e.pagina}\n    ${e.texto}`).join('\n')
      : 'Sin errores, advertencias ni peticiones fallidas.';
    await testInfo.attach('depuracion.txt', { body: resumen, contentType: 'text/plain' });
    await testInfo.attach('depuracion.json', { body: JSON.stringify(eventos, null, 2), contentType: 'application/json' });
    // Copia legible fuera del informe HTML: reportes/depuracion/<proyecto> - <prueba>.txt
    const nombre = `${testInfo.project.name} - ${testInfo.titlePath.slice(1).join(' › ')}`.replace(/[\\/:*?"<>|]/g, '_').slice(0, 150);
    mkdirSync('reportes/depuracion', { recursive: true });
    writeFileSync(`reportes/depuracion/${nombre}.txt`, `${testInfo.title}\n${'='.repeat(testInfo.title.length)}\n\n${resumen}\n`);

    const graves = eventos.filter(e =>
      (e.tipo === 'error-js' || /^http-5/.test(e.tipo) || (/^http-4/.test(e.tipo) && e.texto.includes(' /api/')))
      && !esperados.some(p => p.test(`${e.tipo} ${e.texto}`)));
    expect(graves, 'Errores de NovaPop durante la prueba:\n' + graves.map(e => `${e.tipo} ${e.texto}`).join('\n')).toEqual([]);
  }, { auto: true }],
});

export { expect };

// ------------------------------------------------------------------ sesiones (por API, sin escribir en el formulario)

/** Inicia sesión como cliente y elige un perfil sin PIN, dejándolo guardado como lo hace la app. */
export async function entrarComoCliente(page, { infantil = false } = {}) {
  const correo = process.env.NOVAPOP_CORREO || 'demo@novapop.pe';
  const clave = process.env.NOVAPOP_CLAVE || 'NovaPop2026!';
  const r = await page.request.post('/api/auth/login', { data: { correo, clave } });
  expect(r.ok(), 'login del cliente').toBeTruthy();
  const { token } = await r.json();
  const perfiles = await (await page.request.get('/api/perfiles', { headers: { Authorization: 'Bearer ' + token } })).json();
  const perfil = perfiles.find(p => !p.tienePin && p.esInfantil === infantil);
  expect(perfil, 'un perfil sin PIN' + (infantil ? ' infantil' : '')).toBeTruthy();
  await page.addInitScript(([t, c, p]) => {
    localStorage.setItem('np_token', t);
    localStorage.setItem('np_correo', c);
    localStorage.setItem('np_perfil', JSON.stringify({ id: p.id, nombre: p.nombre, avatar: p.avatar, esInfantil: p.esInfantil }));
  }, [token, correo, perfil]);
  return { token, perfil };
}

/** Credenciales del panel: solo por variables de entorno, para no escribirlas en el repositorio. */
export const credencialesAdmin = () =>
  process.env.NOVAPOP_ADMIN_USUARIO && process.env.NOVAPOP_ADMIN_CLAVE
    ? { usuario: process.env.NOVAPOP_ADMIN_USUARIO, clave: process.env.NOVAPOP_ADMIN_CLAVE }
    : null;
