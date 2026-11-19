// Service worker de NovaPop (PWA).
// - Páginas, CSS y JS: primero la red (así cada despliegue se ve al instante); si no hay conexión, la copia guardada.
// - Imágenes y fuentes: primero la copia guardada (no cambian) y, si no está, la red.
// - La API (/api/...) y los videos nunca se guardan: siempre van a la red.
// - Sin conexión y sin copia de la página: se muestra /offline.html.
const VERSION = 'novapop-v1';
const BASICOS = [
  '/offline.html', '/css/styles.css', '/js/tema.js', '/js/api.js', '/js/ui.js',
  '/img/logo.webp', '/img/logo-icono.png', '/img/icono-192.png', '/manifest.json',
];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(VERSION).then(c => c.addAll(BASICOS)).then(() => self.skipWaiting()));
});

// Al activar una versión nueva se borran las copias de versiones anteriores
self.addEventListener('activate', e => {
  e.waitUntil(caches.keys()
    .then(claves => Promise.all(claves.filter(k => k !== VERSION).map(k => caches.delete(k))))
    .then(() => self.clients.claim()));
});

self.addEventListener('fetch', e => {
  const req = e.request;
  const url = new URL(req.url);
  if (req.method !== 'GET' || req.headers.has('range')) return;            // formularios, videos por partes
  const mismaWeb = url.origin === location.origin;
  const fuente = /fonts\.(googleapis|gstatic)\.com$/.test(url.hostname);
  if ((mismaWeb && url.pathname.startsWith('/api/')) || (!mismaWeb && !fuente)) return;

  if (req.mode === 'navigate' || /\.(css|js|json)$/.test(url.pathname)) {
    e.respondWith(redPrimero(req));
  } else if (fuente || /\.(webp|png|jpe?g|svg|gif|ico|woff2?)$/.test(url.pathname)) {
    e.respondWith(copiaPrimero(req));
  }
});

async function redPrimero(req) {
  const cache = await caches.open(VERSION);
  try {
    const resp = await fetch(req);
    if (resp.ok) cache.put(req, resp.clone());
    return resp;
  } catch {
    // ignoreSearch: /titulo.html?id=5 sirve la copia de /titulo.html si es la única que hay
    const copia = await cache.match(req) || await cache.match(req, { ignoreSearch: true });
    if (copia) return copia;
    if (req.mode === 'navigate') return cache.match('/offline.html');
    return Response.error();
  }
}

async function copiaPrimero(req) {
  const cache = await caches.open(VERSION);
  const copia = await cache.match(req);
  if (copia) return copia;
  try {
    const resp = await fetch(req);
    if (resp.ok || resp.type === 'opaque') cache.put(req, resp.clone());
    return resp;
  } catch {
    return Response.error();
  }
}
