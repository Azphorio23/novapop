// Configuración de Playwright para NovaPop.
// URL a probar: variable NOVAPOP_URL (por defecto la app local en el puerto 8080).
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  outputDir: './reportes/resultados',
  timeout: 60_000,
  expect: { timeout: 15_000 },
  fullyParallel: false, // comparten la cuenta demo: en orden, para no pisarse
  workers: 1,
  retries: 1,           // el plan gratuito de Render tarda en "despertar": un reintento evita falsos errores
  reporter: [
    ['list'],
    ['html', { outputFolder: './reportes/playwright', open: 'never' }],
  ],
  use: {
    baseURL: process.env.NOVAPOP_URL || 'http://localhost:8080',
    channel: 'chrome',          // usa el Chrome instalado en la PC (no hace falta descargar navegadores)
    locale: 'es-PE',
    screenshot: 'on',
    trace: 'retain-on-failure',
    video: 'off',               // grabar video requiere ffmpeg; las capturas y la traza bastan
  },
  projects: [
    { name: 'escritorio', use: { ...devices['Desktop Chrome'], channel: 'chrome' } },
    { name: 'celular', use: { ...devices['Pixel 7'], channel: 'chrome' } },
  ],
});
