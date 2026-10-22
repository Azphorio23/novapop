// Componentes de interfaz compartidos por todas las páginas de NovaPop.
import { sesion } from './api.js';

/** Escapa texto antes de insertarlo con innerHTML. */
export function esc(valor) {
  return String(valor ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

export const $ = (selector, raiz = document) => raiz.querySelector(selector);

// ---------------------------------------------------------------- íconos
const ICONOS = {
  play: '<path d="M8 5.14v13.72a1 1 0 0 0 1.5.86l11-6.86a1 1 0 0 0 0-1.72l-11-6.86A1 1 0 0 0 8 5.14z" fill="currentColor"/>',
  info: '<circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" stroke-width="2"/><path d="M12 11v6M12 7.5v.5" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>',
  mas: '<path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"/>',
  check: '<path d="M5 12.5l4.5 4.5L19 7.5" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>',
  buscar: '<circle cx="11" cy="11" r="7" fill="none" stroke="currentColor" stroke-width="2"/><path d="M20 20l-3.5-3.5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>',
  izq: '<path d="M15 5l-7 7 7 7" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>',
  der: '<path d="M9 5l7 7-7 7" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>',
  cerrar: '<path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"/>',
  estrella: '<path d="M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.9l-5.2 2.7 1-5.8-4.3-4.1 5.9-.9z" fill="currentColor"/>',
  candado: '<rect x="5" y="11" width="14" height="10" rx="2" fill="currentColor"/><path d="M8 11V8a4 4 0 0 1 8 0v3" fill="none" stroke="currentColor" stroke-width="2"/>',
  lapiz: '<path d="M4 20h4L19 9l-4-4L4 16v4z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>',
  casa: '<path d="M4 11l8-7 8 7v9a1 1 0 0 1-1 1h-5v-6h-4v6H5a1 1 0 0 1-1-1z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>',
  peli: '<rect x="3" y="5" width="18" height="14" rx="2" fill="none" stroke="currentColor" stroke-width="2"/><path d="M7 5v14M17 5v14M3 9h4M3 15h4M17 9h4M17 15h4" stroke="currentColor" stroke-width="2"/>',
  tv: '<rect x="3" y="6" width="18" height="12" rx="2" fill="none" stroke="currentColor" stroke-width="2"/><path d="M8 21h8M9 2l3 4 3-4" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>',
  lista: '<path d="M5 3h14v18l-7-4-7 4z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>',
  usuario: '<circle cx="12" cy="8" r="4" fill="none" stroke="currentColor" stroke-width="2"/><path d="M4 21c1.5-4 4.5-6 8-6s6.5 2 8 6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>',
  salir: '<path d="M15 4h4v16h-4M10 8l-4 4 4 4M6 12h10" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>',
  atras: '<path d="M19 12H5M11 6l-6 6 6 6" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>',
};
export const icono = nombre => `<svg viewBox="0 0 24 24" aria-hidden="true">${ICONOS[nombre] || ''}</svg>`;

// ---------------------------------------------------------------- marca
export const marca = (clase = '') =>
  `<a href="/inicio.html" class="marca ${clase}" aria-label="NovaPop, inicio"><img src="/img/logo.svg" alt=""><span>Nova<span class="pop">Pop</span></span></a>`;

// ---------------------------------------------------------------- avatares
const COLORES_AVATAR = {
  'avatar-1': ['#e63946', '#ff8a5b'], 'avatar-2': ['#3a86ff', '#7cc6fe'],
  'avatar-3': ['#2a9d8f', '#8ce99a'], 'avatar-4': ['#7b2cbf', '#e0aaff'],
  'avatar-5': ['#ffb703', '#ffe066'], 'avatar-6': ['#f72585', '#ffafcc'],
  'avatar-7': ['#023e8a', '#48cae4'], 'avatar-8': ['#3d405b', '#f2cc8f'],
};
export const AVATARES = Object.keys(COLORES_AVATAR);

export function avatar(clave = 'avatar-1') {
  const [a, b] = COLORES_AVATAR[clave] || COLORES_AVATAR['avatar-1'];
  const id = 'g' + Math.random().toString(36).slice(2, 8);
  const n = Number(String(clave).split('-')[1]) || 1;
  const boca = n % 2 ? '<path d="M38 64 Q50 76 62 64" fill="none" stroke="#1b1300" stroke-width="5" stroke-linecap="round"/>'
    : '<ellipse cx="50" cy="66" rx="9" ry="7" fill="#1b1300"/>';
  return `<div class="avatar"><svg viewBox="0 0 100 100" aria-hidden="true">
    <defs><linearGradient id="${id}" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="${a}"/><stop offset="1" stop-color="${b}"/></linearGradient></defs>
    <rect width="100" height="100" fill="url(#${id})"/>
    <circle cx="36" cy="44" r="6" fill="#1b1300"/><circle cx="64" cy="44" r="6" fill="#1b1300"/>
    <circle cx="38" cy="42" r="2" fill="#fff"/><circle cx="66" cy="42" r="2" fill="#fff"/>
    ${boca}
    ${n === 5 ? '<path d="M78 10 L81 19 L90 22 L81 25 L78 34 L75 25 L66 22 L75 19Z" fill="#fff" opacity=".9"/>' : ''}
  </svg></div>`;
}

// ---------------------------------------------------------------- formato
export function duracion(minutos) {
  if (!minutos) return '';
  const h = Math.floor(minutos / 60);
  const m = minutos % 60;
  return h ? (m ? `${h} h ${m} min` : `${h} h`) : `${m} min`;
}
export const soles = n => 'S/ ' + Number(n).toFixed(2);
export const fecha = iso => iso ? new Date(iso.length === 10 ? iso + 'T12:00:00' : iso)
  .toLocaleDateString('es-PE', { day: 'numeric', month: 'short', year: 'numeric' }) : '';

export function idYoutube(url) {
  const m = String(url || '').match(/(?:v=|youtu\.be\/|embed\/)([\w-]{11})/);
  return m ? m[1] : null;
}

// ---------------------------------------------------------------- tarjetas
// Si una imagen no carga, se reemplaza por un bloque de color con el título
document.addEventListener('error', e => {
  const img = e.target;
  if (img.tagName === 'IMG' && img.dataset.titulo !== undefined) {
    const div = document.createElement('div');
    div.className = 'sin-imagen';
    div.textContent = img.dataset.titulo;
    img.replaceWith(div);
  }
}, true);

export function imagen(t) {
  return t.posterUrl
    ? `<img src="${esc(t.posterUrl)}" alt="" loading="lazy" data-titulo="${esc(t.titulo)}">`
    : `<div class="sin-imagen">${esc(t.titulo)}</div>`;
}

export function tarjeta(t, { porcentaje = null, etiqueta = null, href = null } = {}) {
  const destino = href || `/titulo.html?id=${t.idContenido}`;
  const info = [t.anio, t.clasificacion, t.tipo === 'SERIE' ? `${t.totalEpisodios} episodios` : duracion(t.duracionMinutos)]
    .filter(Boolean).map(esc).join(' · ');
  return `<a class="tarjeta" href="${destino}" title="${esc(t.titulo)}">
      ${imagen(t)}
      ${t.tipo === 'SERIE' ? '<span class="chip-serie">SERIE</span>' : ''}
      <div class="capa">
        <span class="titulo">${esc(t.titulo)}</span>
        <span class="info">${etiqueta ? esc(etiqueta) : info}</span>
      </div>
      ${porcentaje !== null ? `<div class="progreso"><span style="width:${Math.max(3, porcentaje)}%"></span></div>` : ''}
    </a>`;
}

/** Fila horizontal con flechas de desplazamiento. */
export function fila(titulo, contenidoHtml, { top10 = false } = {}) {
  return `<section class="fila">
      <h2>${titulo}</h2>
      <div class="carrusel">
        <button class="flecha izq" aria-label="Anterior">${icono('izq')}</button>
        <div class="pista ${top10 ? 'top10' : ''}">${contenidoHtml}</div>
        <button class="flecha der" aria-label="Siguiente">${icono('der')}</button>
      </div>
    </section>`;
}

export function activarCarruseles(raiz = document) {
  raiz.querySelectorAll('.carrusel').forEach(c => {
    const pista = c.querySelector('.pista');
    c.querySelector('.izq').onclick = () => pista.scrollBy({ left: -pista.clientWidth * 0.85, behavior: 'smooth' });
    c.querySelector('.der').onclick = () => pista.scrollBy({ left: pista.clientWidth * 0.85, behavior: 'smooth' });
  });
}

// ---------------------------------------------------------------- navegación
/** Botón ☀️/🌙 (requiere /js/tema.js en el <head>). */
export const botonTema = (clase = '') =>
  `<button class="btn-tema ${clase}" onclick="novapopTema.alternar()" aria-label="Cambiar tema claro u oscuro" title="Cambiar tema">` +
  '<span class="solo-oscuro">☀️</span><span class="solo-claro">🌙</span></button>';

export function navbar(activo = '') {
  const p = sesion.perfil || {};
  const enlaces = [
    ['inicio', '/inicio.html', 'Inicio', 'casa'],
    ['peliculas', '/inicio.html?tipo=PELICULA', 'Películas', 'peli'],
    ['series', '/inicio.html?tipo=SERIE', 'Series', 'tv'],
    ['lista', '/inicio.html?lista=1', 'Mi lista', 'lista'],
  ];
  const html = `
    <header class="nav" id="nav">
      ${marca()}
      <nav class="nav-links">${enlaces.map(([k, h, t]) => `<a href="${h}" class="${k === activo ? 'activo' : ''}">${t}</a>`).join('')}</nav>
      <div class="nav-derecha">
        <form class="buscador" action="/inicio.html" role="search">
          ${icono('buscar')}<input name="q" type="search" placeholder="Buscar títulos" aria-label="Buscar" value="${esc(new URLSearchParams(location.search).get('q') || '')}">
        </form>
        ${botonTema()}
        <div class="menu-perfil" id="menuPerfil">
          <button aria-label="Menú de perfil" aria-haspopup="true">${avatar(p.avatar)}</button>
          <div class="menu-desplegable">
            <div style="padding:8px 12px;font-weight:700">${esc(p.nombre)}${p.esInfantil ? ' <span class="badge badge-pop">Infantil</span>' : ''}</div>
            <hr>
            <a href="/perfiles.html">${icono('usuario')} Cambiar de perfil</a>
            <a href="/cuenta.html">${icono('lapiz')} Mi cuenta</a>
            <hr>
            <button id="salir">${icono('salir')} Cerrar sesión</button>
          </div>
        </div>
      </div>
    </header>
    <nav class="nav-movil">${enlaces.map(([k, h, t, i]) => `<a href="${h}" class="${k === activo ? 'activo' : ''}">${icono(i)}${t}</a>`).join('')}</nav>`;
  document.body.insertAdjacentHTML('afterbegin', html);

  const nav = $('#nav');
  const actualizar = () => nav.classList.toggle('solida', scrollY > 30);
  addEventListener('scroll', actualizar, { passive: true });
  actualizar();

  const menu = $('#menuPerfil');
  menu.querySelector('button').onclick = e => { e.stopPropagation(); menu.classList.toggle('abierto'); };
  document.addEventListener('click', () => menu.classList.remove('abierto'));
  $('#salir').onclick = () => { sesion.cerrar(); location.href = '/'; };
}

export function pieDePagina() {
  document.body.insertAdjacentHTML('beforeend', `
    <footer class="pie-sitio">
      <p><b>NovaPop</b> · Proyecto académico sin fines de lucro. · <a href="/admin/" style="color:var(--muted);text-decoration:underline">Administración</a></p>
      <p>Películas y series abiertas de Blender Foundation / Blender Studio bajo licencia Creative Commons,
      servidas desde Wikimedia Commons. Los demás títulos pertenecen a sus estudios: NovaPop solo muestra su ficha
      y su tráiler oficial de YouTube. Las marcas pertenecen a sus dueños.</p>
      <p>Datos e imágenes de catálogo: <a href="https://www.themoviedb.org" target="_blank" rel="noopener" style="color:var(--pop)">TMDB</a>.
      This product uses the TMDB API but is not endorsed or certified by TMDB.</p>
    </footer>`);
}

// ---------------------------------------------------------------- avisos y modales
export function toast(mensaje, tipo = 'ok') {
  let cont = $('.toasts');
  if (!cont) {
    cont = document.createElement('div');
    cont.className = 'toasts';
    cont.setAttribute('role', 'status');
    document.body.append(cont);
  }
  const t = document.createElement('div');
  t.className = 'toast ' + (tipo === 'error' ? 'error' : '');
  t.textContent = mensaje;
  cont.append(t);
  setTimeout(() => t.remove(), 4200);
}

/** Abre un modal con el HTML dado. Devuelve { el, cerrar }. */
export function modal(html, { clase = 'modal-caja', alCerrar } = {}) {
  const fondo = document.createElement('div');
  fondo.className = 'modal';
  fondo.innerHTML = `<div class="${clase}" role="dialog" aria-modal="true">${html}</div>`;
  const cerrar = () => {
    fondo.remove();
    removeEventListener('keydown', esc_);
    alCerrar?.();
  };
  const esc_ = e => { if (e.key === 'Escape') cerrar(); };
  fondo.addEventListener('click', e => { if (e.target === fondo) cerrar(); });
  addEventListener('keydown', esc_);
  document.body.append(fondo);
  fondo.querySelectorAll('[data-cerrar]').forEach(b => b.onclick = cerrar);
  return { el: fondo, cerrar };
}

export function verTrailer(url) {
  const id = idYoutube(url);
  if (!id) return;
  modal(`<button class="btn btn-sutil btn-icono cerrar" data-cerrar aria-label="Cerrar">${icono('cerrar')}</button>
         <iframe src="https://www.youtube-nocookie.com/embed/${id}?autoplay=1&rel=0" title="Tráiler"
           allow="autoplay; encrypted-media; picture-in-picture; fullscreen" allowfullscreen></iframe>`,
  { clase: 'modal-video' });
}

/**
 * Pide el PIN de 4 dígitos y lo valida con `verificar(pin)` (una promesa que falla si es incorrecto).
 * Resuelve true cuando el PIN es correcto y false si el usuario cancela.
 */
export function pedirPin(nombrePerfil, verificar) {
  return new Promise(resolve => {
    let correcto = false;
    const { el, cerrar } = modal(`
      <h2>PIN de ${esc(nombrePerfil)}</h2>
      <p class="muted" style="margin-top:-8px">Este perfil está protegido con control parental.</p>
      <div class="pin">${'<input inputmode="numeric" maxlength="1" aria-label="Dígito del PIN">'.repeat(4)}</div>
      <p class="error-form oculto" id="pinError"></p>
      <div class="modal-acciones"><button class="btn btn-borde" data-cerrar>Cancelar</button></div>`,
    { alCerrar: () => resolve(correcto) });
    const cajas = [...el.querySelectorAll('.pin input')];
    const error = el.querySelector('#pinError');
    cajas[0].focus();
    cajas.forEach((caja, i) => {
      caja.addEventListener('input', async () => {
        caja.value = caja.value.replace(/\D/g, '');
        if (caja.value && i < 3) cajas[i + 1].focus();
        const pin = cajas.map(c => c.value).join('');
        if (pin.length < 4) return;
        try {
          await verificar(pin);
          correcto = true;
          cerrar();
        } catch (e) {
          error.textContent = e.message;
          error.classList.remove('oculto');
          cajas.forEach(c => c.value = '');
          cajas[0].focus();
        }
      });
      caja.addEventListener('keydown', e => { if (e.key === 'Backspace' && !caja.value && i > 0) cajas[i - 1].focus(); });
    });
  });
}

export function cargando(contenedor) {
  contenedor.innerHTML = '<div style="display:grid;place-items:center;padding:120px 0"><div class="cargando"></div></div>';
}
