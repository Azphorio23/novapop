// Caja negra: páginas públicas, sin sesión. Solo se mira lo que ve cualquier visitante.
import { test, expect } from './depuracion.js';

const PAGINAS = [
  ['/', /NovaPop/, 'Películas y series'],
  ['/login.html', /Iniciar sesión/, 'Iniciar sesión'],
  ['/registro.html', /NovaPop/, 'Crea tu cuenta'],
  ['/privacidad.html', /Política de privacidad/, 'Política de privacidad'],
  ['/offline.html', /Sin conexión/, 'Estás sin conexión'],
];

for (const [ruta, titulo, texto] of PAGINAS) {
  test(`la página ${ruta} carga sin errores`, async ({ page }) => {
    const respuesta = await page.goto(ruta);
    expect(respuesta.status()).toBe(200);
    await expect(page).toHaveTitle(titulo);
    await expect(page.getByText(texto, { exact: false }).first()).toBeVisible();
    await expect(page.locator('.marca img').first()).toBeVisible();
  });
}

test('el landing muestra los planes desde la API', async ({ page }) => {
  await page.goto('/');
  await expect(page.locator('#listaPlanes .plan')).toHaveCount(3);
  await expect(page.locator('#listaPlanes')).toContainText('S/');
});

test('una dirección que no existe muestra la página 404 propia', async ({ page }) => {
  const respuesta = await page.goto('/esta-pagina-no-existe');
  expect(respuesta.status()).toBe(404);
  await expect(page).toHaveTitle(/Página no encontrada/);
  await expect(page.locator('#ruta')).toHaveText('/esta-pagina-no-existe');
});

test('login con contraseña incorrecta muestra un error y no entra', async ({ page, depuracion }) => {
  depuracion.esperar(/http-401 POST \/api\/auth\/login/); // el 401 es justo lo que se prueba
  await page.goto('/login.html');
  await page.fill('#correo', 'demo@novapop.pe');
  await page.fill('#clave', 'contraseña-equivocada');
  await page.click('#btn');
  await expect(page.locator('#error')).toBeVisible();
  await expect(page).toHaveURL(/login\.html/);
});

test('la app se puede instalar (manifest y service worker)', async ({ page }) => {
  await page.goto('/');
  const manifest = await (await page.request.get('/manifest.json')).json();
  expect(manifest.short_name).toBe('NovaPop');
  expect(manifest.icons.some(i => i.sizes === '512x512')).toBeTruthy();
  const estado = await page.evaluate(() => navigator.serviceWorker.ready.then(r => r.active.state));
  expect(estado).toBe('activated');
});
