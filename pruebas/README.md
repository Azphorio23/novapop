# Pruebas de NovaPop: Playwright y Lighthouse

Esta carpeta prueba la aplicación **desde afuera**, como lo haría un usuario (caja negra), y mide la calidad de cada
módulo con Lighthouse. Las pruebas de **caja blanca** (JUnit, conociendo el código) están en `backend/src/test`.

## Requisitos

- Node.js 18 o superior y Google Chrome instalado (Playwright usa ese Chrome; no descarga otro navegador).
- NovaPop encendido: en local (`http://localhost:8080`) o en Render.

```bash
cd pruebas
npm install
```

## Playwright: pruebas de punta a punta con registro de depuración

```bash
npm test                      # escritorio y celular
npm run test:escritorio       # solo escritorio
npm run reporte               # abre el informe HTML
```

Qué prueba (`e2e/`):

| Archivo | Tipo | Qué revisa |
|---|---|---|
| `publico.spec.js` | Caja negra | Landing, login, registro, privacidad, página sin conexión, 404, login con contraseña incorrecta, instalación de la PWA |
| `cliente.spec.js` | Caja gris (usuario estándar) | Perfiles, inicio y carrusel, buscador, filtro por género, ficha y filmografía de un actor, reproductor, mi cuenta, mi lista, perfil infantil, redirección sin sesión |
| `admin.spec.js` | Caja gris (personal) | Login del panel por el formulario, cada módulo (tabla) del panel y que un cliente reciba 403 en la API del panel |

**Depuración de cada página** (`e2e/depuracion.js`): durante cada prueba se registran los errores de JavaScript, los
mensajes de consola (error y advertencia), las peticiones fallidas y las respuestas HTTP 4xx/5xx. Se guardan:

- dentro del informe HTML (`reportes/playwright/index.html`), junto a la captura de pantalla de cada prueba;
- como texto en `reportes/depuracion/<proyecto> - <prueba>.txt`.

Una prueba **falla** si la página tuvo un error de JavaScript, una respuesta 5xx o un error 4xx de la API que no se
esperaba.

## Lighthouse: rendimiento por módulo

```bash
npm run lighthouse
```

Mide 12 módulos (landing, login, registro, perfiles, inicio, películas, ficha, reproductor, mi cuenta y tres del panel)
en **celular** (red y CPU limitadas, como PageSpeed Insights) y en **escritorio**: rendimiento, accesibilidad,
buenas prácticas y SEO, con sus métricas (FCP, LCP, TBT, CLS) y la lista de qué mejorar.

Resultado: `reportes/lighthouse/index.html` (resumen) y un informe completo por módulo y dispositivo.

Las páginas con sesión se miden iniciando sesión antes en el mismo navegador (Playwright + Lighthouse), algo que
[PageSpeed Insights](https://pagespeed.web.dev/) no puede hacer: PageSpeed solo mide páginas públicas, como el landing.

## Variables de entorno

| Variable | Para qué | Por defecto |
|---|---|---|
| `NOVAPOP_URL` | Dónde está la app | `http://localhost:8080` |
| `NOVAPOP_CORREO`, `NOVAPOP_CLAVE` | Cuenta de cliente para las pruebas | la cuenta demo |
| `NOVAPOP_ADMIN_USUARIO`, `NOVAPOP_ADMIN_CLAVE` | Usuario del panel (sin ellas, se omiten las pruebas del panel) | — |

Ejemplo contra la versión publicada (PowerShell):

```powershell
$env:NOVAPOP_URL = "https://novapop.onrender.com"
$env:NOVAPOP_ADMIN_USUARIO = "admin"; $env:NOVAPOP_ADMIN_CLAVE = "..."
npm test; npm run lighthouse
```

El plan gratuito de Render apaga la app sin visitas: la primera carga tarda y baja la nota de rendimiento.
Conviene abrir la página una vez antes de medir. Las pruebas tienen un reintento por ese motivo.
