// Caja negra con una cuenta de cliente (caja gris: usamos un usuario estándar, sin ver el código).
// Recorre los módulos que usa un suscriptor y guarda una captura y el registro de depuración de cada uno.
import { test, expect, entrarComoCliente } from './depuracion.js';

test.describe('cliente con sesión', () => {
  let sesion;
  test.beforeEach(async ({ page }) => { sesion = await entrarComoCliente(page); });

  test('perfiles: "¿Quién está viendo?" muestra los perfiles con su personaje', async ({ page }) => {
    await page.goto('/perfiles.html');
    await expect(page.locator('h1')).toHaveText('¿Quién está viendo?');
    await expect(page.locator('.perfil[data-id]').first()).toBeVisible();
    await expect(page.locator('.perfil[data-id] .avatar img').first()).toHaveAttribute('src', /\/img\/avatares\//);
  });

  test('inicio: carrusel destacado y filas del catálogo', async ({ page }) => {
    await page.goto('/inicio.html');
    await expect(page.locator('.hero-slide.activo h1')).toBeVisible();
    const primero = await page.locator('.hero-slide.activo h1').textContent();
    await page.click('.hero-flecha.der');
    await expect(page.locator('.hero-slide.activo h1')).not.toHaveText(primero);
    expect(await page.locator('.fila').count()).toBeGreaterThan(2);
    await expect(page.locator('.tarjeta.vertical').first()).toBeVisible();
  });

  test('buscador: encuentra "Sintel"', async ({ page }) => {
    await page.goto('/inicio.html?q=sintel');
    await expect(page.locator('.explorar h1')).toContainText('sintel');
    await expect(page.locator('.grilla .tarjeta').first()).toHaveAttribute('title', /Sintel/);
  });

  test('películas: filtro por género', async ({ page }) => {
    await page.goto('/inicio.html?tipo=PELICULA');
    await expect(page.locator('.grilla .tarjeta').first()).toBeVisible();
    const total = await page.locator('.grilla .tarjeta').count();
    await page.locator('.chip', { hasText: 'Animación' }).click();
    await expect(page.locator('.chip.activo')).toHaveText('Animación');
    expect(await page.locator('.grilla .tarjeta').count()).toBeLessThanOrEqual(total);
  });

  test('ficha del título y filmografía de un actor', async ({ page }) => {
    await page.goto('/titulo.html?id=3');
    await expect(page.locator('.detalle-info h1')).toHaveText('Sintel');
    await page.locator('.credito', { hasText: 'Halina Reijn' }).click();
    await expect(page.locator('.modal-persona h2')).toHaveText('Halina Reijn');
    await expect(page.locator('.persona-trabajo').first()).toBeVisible();
  });

  test('reproductor: carga el video de Big Buck Bunny', async ({ page }) => {
    await page.goto('/ver.html?id=1');
    await expect(page.locator('#titulo')).toHaveText('Big Buck Bunny');
    await expect.poll(() => page.locator('#video').evaluate(v => v.currentSrc), { timeout: 20_000 }).toMatch(/^https:\/\//);
    // Cada navegador nuevo se registra como DISPOSITIVO: lo quitamos para no llenar la cuenta demo
    const id = await page.evaluate(() => Object.entries(localStorage).find(([k]) => k.startsWith('np_dispositivo_'))?.[1]);
    if (id) await page.request.delete(`/api/cuenta/dispositivos/${id}`, { headers: { Authorization: 'Bearer ' + sesion.token } });
  });

  test('mi cuenta: plan, pagos y dispositivos', async ({ page }) => {
    await page.goto('/cuenta.html');
    await expect(page.locator('h1')).toHaveText('Mi cuenta');
    for (const seccion of ['Tu plan', 'Métodos de pago', 'Historial de pagos', 'Dispositivos']) {
      await expect(page.getByRole('heading', { name: seccion })).toBeVisible();
    }
  });

  test('mi cuenta: descargar mis datos (copia de seguridad del usuario)', async ({ page }) => {
    await page.goto('/cuenta.html');
    const [descarga] = await Promise.all([page.waitForEvent('download'), page.click('#misDatos')]);
    expect(descarga.suggestedFilename()).toMatch(/^novapop-mis-datos-[\d-]+\.json$/);
    const datos = JSON.parse(await (await import('node:fs/promises')).readFile(await descarga.path(), 'utf8'));
    expect(datos.cuenta.correo).toBeTruthy();
    expect(datos.perfiles.length).toBeGreaterThan(0);
    expect(JSON.stringify(datos)).not.toContain('clave_hash');
  });

  test('mi lista', async ({ page }) => {
    await page.goto('/inicio.html?lista=1');
    await expect(page.locator('.explorar h1')).toHaveText('Mi lista');
  });
});

test('perfil infantil: no ve títulos para mayores de 13', async ({ page }) => {
  await entrarComoCliente(page, { infantil: true });
  await page.goto('/inicio.html?tipo=PELICULA');
  await expect(page.locator('.grilla .tarjeta').first()).toBeVisible();
  const clasificaciones = await page.locator('.grilla .tarjeta .info').allTextContents();
  expect(clasificaciones.some(t => /\+1[368]/.test(t))).toBeFalsy();
});

test('sin sesión, las páginas privadas mandan al login', async ({ page }) => {
  await page.goto('/inicio.html');
  await expect(page).toHaveURL(/login\.html|perfiles\.html/);
});
