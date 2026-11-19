// Panel de administración: entra por el formulario (como un empleado) y abre cada módulo (tabla) del panel.
// Las credenciales se pasan por variables de entorno: NOVAPOP_ADMIN_USUARIO y NOVAPOP_ADMIN_CLAVE.
import { test, expect, credencialesAdmin } from './depuracion.js';

const cred = credencialesAdmin();
test.skip(!cred, 'Define NOVAPOP_ADMIN_USUARIO y NOVAPOP_ADMIN_CLAVE para probar el panel');

test('un cliente no puede usar la API del panel (403)', async ({ page }) => {
  const r = await page.request.post('/api/auth/login', { data: { correo: process.env.NOVAPOP_CORREO || 'demo@novapop.pe', clave: process.env.NOVAPOP_CLAVE || 'NovaPop2026!' } });
  const { token } = await r.json();
  const resp = await page.request.get('/api/admin/recursos', { headers: { Authorization: 'Bearer ' + token } });
  expect(resp.status()).toBe(403);
});

test('login del panel y cada uno de sus módulos', async ({ page }) => {
  test.setTimeout(180_000);
  await page.goto('/admin/login.html');
  await page.fill('#usuario', cred.usuario);
  await page.fill('#clave', cred.clave);
  await page.click('#btn');
  await expect(page).toHaveURL(/\/admin\/$/);
  await expect(page.locator('.hero-admin h1')).toContainText(/Buen/);

  const token = await page.evaluate(() => localStorage.getItem('np_admin_token'));
  const recursos = await (await page.request.get('/api/admin/recursos', { headers: { Authorization: 'Bearer ' + token } })).json();
  expect(recursos.length).toBeGreaterThan(10);
  for (const r of recursos) {
    await test.step(`módulo ${r.titulo}`, async () => {
      await page.goto(`/admin/#/${r.clave}`);
      await expect(page.locator('.tabla-admin')).toBeVisible();
      await test.info().attach(`${r.clave}.png`, { body: await page.screenshot(), contentType: 'image/png' });
    });
  }
});
