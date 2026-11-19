// Componentes de interfaz compartidos por todas las páginas de NovaPop.
import { api, sesion } from './api.js';

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
  imagen: '<circle cx="12" cy="9" r="3.5" fill="none" stroke="currentColor" stroke-width="2"/><path d="M5.5 19.5c1.3-3 3.7-4.5 6.5-4.5s5.2 1.5 6.5 4.5" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><circle cx="12" cy="12" r="10" fill="none" stroke="currentColor" stroke-width="2"/>',
  instalar: '<path d="M12 4v11M7 10.5l5 5 5-5M5 20h14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>',
  abajo: '<path d="M6 9l6 6 6-6" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>',
  atras: '<path d="M19 12H5M11 6l-6 6 6 6" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>',
  reloj: '<circle cx="12" cy="13" r="8" fill="none" stroke="currentColor" stroke-width="2"/><path d="M12 9v4l2.5 2.5M9.5 2.5h5" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>',
  celular: '<rect x="7" y="2.5" width="10" height="19" rx="2.5" fill="none" stroke="currentColor" stroke-width="2"/><path d="M11 18.5h2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>',
  laptop: '<rect x="4.5" y="5" width="15" height="10" rx="1.5" fill="none" stroke="currentColor" stroke-width="2"/><path d="M2.5 19h19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>',
  consola: '<path d="M7 7h10a5 5 0 0 1 4.8 6.4l-1 3.4a2.4 2.4 0 0 1-4.1.9L15 16H9l-1.7 1.7a2.4 2.4 0 0 1-4.1-.9l-1-3.4A5 5 0 0 1 7 7z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/><path d="M8 10.5v3M6.5 12h3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><circle cx="15.5" cy="11" r="1.1" fill="currentColor"/><circle cx="17.5" cy="13" r="1.1" fill="currentColor"/>',
};

// Botón de tema: un sol de rayos redondeados y una luna creciente con una estrella (sin emojis)
export const SVG_SOL = '<svg class="ico-tema sol" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="4.3" fill="currentColor"/>' +
  '<path d="M12 2.6v2.3M12 19.1v2.3M2.6 12h2.3M19.1 12h2.3M5.4 5.4l1.6 1.6M17 17l1.6 1.6M5.4 18.6 7 17M17 7l1.6-1.6" stroke="currentColor" stroke-width="2.1" stroke-linecap="round"/></svg>';
export const SVG_LUNA = '<svg class="ico-tema luna" viewBox="0 0 24 24" aria-hidden="true"><path d="M20.2 14.6A8.4 8.4 0 0 1 9.4 3.8a8.4 8.4 0 1 0 10.8 10.8z" fill="currentColor"/>' +
  '<path d="M16.6 3.2l.55 1.45 1.45.55-1.45.55-.55 1.45-.55-1.45-1.45-.55 1.45-.55z" fill="currentColor"/></svg>';
export const icono = nombre => `<svg viewBox="0 0 24 24" aria-hidden="true">${ICONOS[nombre] || ''}</svg>`;

// ---------------------------------------------------------------- marca
export const marca = (clase = '') =>
  `<a href="/inicio.html" class="marca ${clase}" aria-label="NovaPop, inicio"><img src="/img/logo.webp" alt=""><span>Nova<span class="pop">Pop</span></span></a>`;

// ---------------------------------------------------------------- avatares
// Personajes para los avatares de perfil, agrupados como en las plataformas de streaming.
// Marvel, DC, Disney, Del Toro y series: recortes de los pósters de TMDB (los personajes pertenecen a sus dueños).
// Cine abierto: películas de Blender Foundation (CC BY) y de dominio público.
// Las imágenes están en /img/avatares/<clave>.webp; la clave es lo que se guarda en perfil.avatar_url.
export const PERSONAJES = [
  { pelicula: 'Marvel', personajes: [['mcu-ironman', 'Iron Man'], ['mcu-capitan', 'Capitán América'], ['mcu-thor', 'Thor'], ['mcu-hulk', 'Hulk'],
    ['mcu-spiderman', 'Spider-Man'], ['mcu-panther', 'Pantera Negra'], ['mcu-marvel', 'Capitana Marvel'], ['mcu-antman', 'Ant-Man'],
    ['mcu-starlord', 'Star-Lord'], ['mcu-gamora', 'Gamora'], ['mcu-wolverine', 'Wolverine'], ['mcu-deadpool', 'Deadpool']] },
  { pelicula: 'DC', personajes: [['dc-batman', 'Batman'], ['dc-superman', 'Superman'], ['dc-wonder', 'Mujer Maravilla'], ['dc-flash', 'Flash'],
    ['dc-joker', 'Joker'], ['dc-capucha', 'Capucha Roja']] },
  { pelicula: 'Disney', personajes: [['dis-simba', 'Simba'], ['dis-genio', 'Genio'], ['dis-bella', 'Bella'], ['dis-bestia', 'Bestia'],
    ['dis-mulan', 'Mulán'], ['dis-cenicienta', 'Cenicienta'], ['dis-alicia', 'Alicia'], ['dis-peterpan', 'Peter Pan'], ['dis-pinocho', 'Pinocho'],
    ['dis-dumbo', 'Dumbo'], ['dis-bambi', 'Bambi'], ['dis-hercules', 'Hércules'], ['dis-tarzan', 'Tarzán']] },
  { pelicula: 'Guillermo del Toro', personajes: [['gdt-hellboy', 'Hellboy']] },
  { pelicula: 'Series', personajes: [['tv-walter', 'Walter White'], ['tv-malcolm', 'Malcolm']] },
  { pelicula: 'Cine abierto', personajes: [['bbb-bunny', 'Bunny'], ['bbb-frank', 'Frank'], ['bbb-rinky', 'Rinky'], ['sintel', 'Sintel'],
    ['sintel-guerrera', 'Sintel guerrera'], ['sintel-scales', 'Scales'], ['ed-proog', 'Proog'], ['ed-emo', 'Emo'], ['cam-koro', 'Koro'],
    ['sprite-ellie', 'Exploradora'], ['charge-granjero', 'El granjero'], ['wing-gato', 'El gato piloto'], ['luna', 'La Luna']] },
];
const NOMBRE_PERSONAJE = Object.fromEntries(PERSONAJES.flatMap(g => g.personajes));
export const AVATARES = Object.keys(NOMBRE_PERSONAJE);
// Perfiles creados antes de los personajes: se muestran con uno equivalente
const ANTIGUOS = {
  'avatar-1': 'sintel', 'avatar-2': 'ed-proog', 'avatar-3': 'cam-koro', 'avatar-4': 'bbb-frank',
  'avatar-5': 'bbb-bunny', 'avatar-6': 'sprite-ellie', 'avatar-7': 'wing-gato', 'avatar-8': 'charge-granjero',
};
export const claveAvatar = clave => NOMBRE_PERSONAJE[clave] ? clave : (ANTIGUOS[clave] || 'bbb-bunny');
export const nombrePersonaje = clave => NOMBRE_PERSONAJE[claveAvatar(clave)];

/** Galería de personajes agrupada; el elegido se marca con la clase "activo". */
export function galeriaPersonajes(actual) {
  const k = actual ? claveAvatar(actual) : null;
  return PERSONAJES.map(g => `
    <section class="galeria-grupo">
      <h3>${esc(g.pelicula)}</h3>
      <div class="galeria-fila">${g.personajes.map(([clave, nombre]) => `
        <button type="button" class="opcion-avatar${clave === k ? ' activo' : ''}" data-avatar="${clave}" title="${esc(nombre)}" aria-label="${esc(nombre)}">${avatar(clave)}<span>${esc(nombre)}</span></button>`).join('')}</div>
    </section>`).join('');
}

/** Abre la galería en un modal. Resuelve la clave elegida o null si se cierra. */
export function elegirPersonaje(actual) {
  return new Promise(resolve => {
    let elegido = null;
    const { el, cerrar } = modal(`
      <div class="panel-galeria">
        <div class="galeria-cabecera">
          <div><h2>Elige tu personaje</h2><small class="muted">Personajes de las películas y series de NovaPop</small></div>
          <button type="button" class="btn btn-sutil btn-icono" data-cerrar aria-label="Cerrar" style="margin-left:auto">${icono('cerrar')}</button>
        </div>
        ${galeriaPersonajes(actual)}
      </div>`, { clase: 'modal-caja modal-perfil', alCerrar: () => resolve(elegido) });
    el.querySelectorAll('.opcion-avatar').forEach(b => b.onclick = () => { elegido = b.dataset.avatar; cerrar(); });
  });
}

export function avatar(clave) {
  const k = claveAvatar(clave);
  return `<div class="avatar"><img src="/img/avatares/${k}.webp" alt="" loading="lazy" draggable="false"></div>`;
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

/** vertical = usa el póster 2:3 si existe (si no, recorta la imagen horizontal). */
export function imagen(t, vertical = false) {
  const url = (vertical && t.posterVerticalUrl) || t.posterUrl;
  return url
    ? `<img src="${esc(url)}" alt="" loading="lazy" data-titulo="${esc(t.titulo)}">`
    : `<div class="sin-imagen">${esc(t.titulo)}</div>`;
}

export function tarjeta(t, { porcentaje = null, etiqueta = null, href = null, vertical = false } = {}) {
  const destino = href || `/titulo.html?id=${t.idContenido}`;
  const info = [t.anio, t.clasificacion, t.tipo === 'SERIE' ? `${t.totalEpisodios} episodios` : duracion(t.duracionMinutos)]
    .filter(Boolean).map(esc).join(' · ');
  const clase = vertical ? `tarjeta vertical${t.posterVerticalUrl ? '' : ' sin-vertical'}` : 'tarjeta';
  return `<a class="${clase}" href="${destino}" title="${esc(t.titulo)}">
      ${imagen(t, vertical)}
      ${t.tipo === 'SERIE' ? '<span class="chip-serie">SERIE</span>' : ''}
      <div class="capa">
        <span class="titulo">${esc(t.titulo)}</span>
        <span class="info">${etiqueta ? esc(etiqueta) : info}</span>
      </div>
      ${porcentaje !== null ? `<div class="progreso"><span style="width:${Math.max(3, porcentaje)}%"></span></div>` : ''}
    </a>`;
}

/** Fila horizontal con flechas de desplazamiento. vertical = tarjetas tipo póster. */
export function fila(titulo, contenidoHtml, { top10 = false, vertical = false } = {}) {
  return `<section class="fila">
      <h2>${titulo}</h2>
      <div class="carrusel">
        <button class="flecha izq" aria-label="Anterior">${icono('izq')}</button>
        <div class="pista${top10 ? ' top10' : ''}${vertical ? ' vertical' : ''}">${contenidoHtml}</div>
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
/** Botón sol/luna (requiere /js/tema.js en el <head>). */
export const botonTema = (clase = '') =>
  `<button class="btn-tema ${clase}" onclick="novapopTema.alternar()" aria-label="Cambiar tema claro u oscuro" title="Cambiar tema">` +
  `<span class="solo-oscuro">${SVG_SOL}</span><span class="solo-claro">${SVG_LUNA}</span></button>`;

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
          <button class="disparador" aria-label="Menú de perfil" aria-haspopup="true" aria-expanded="false">${avatar(p.avatar)}<span class="flechita">${icono('abajo')}</span></button>
          <div class="menu-desplegable" role="menu">
            <div class="menu-cabecera">
              ${avatar(p.avatar)}
              <div class="menu-quien">
                <b>${esc(p.nombre)}${p.esInfantil ? ' <span class="badge badge-pop">Infantil</span>' : ''}</b>
                <small>${esc(sesion.correo || '')}</small>
              </div>
            </div>
            <button id="cambiarPersonaje" role="menuitem">${icono('imagen')}<span>Cambiar personaje</span></button>
            <a href="/perfiles.html" role="menuitem">${icono('usuario')}<span>Cambiar de perfil</span></a>
            <a href="/cuenta.html" role="menuitem">${icono('lapiz')}<span>Mi cuenta</span></a>
            <button id="instalarApp" class="oculto" role="menuitem">${icono('instalar')}<span>Instalar app</span></button>
            <hr>
            <button id="salir" class="salir" role="menuitem">${icono('salir')}<span>Cerrar sesión</span></button>
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
  const disparador = menu.querySelector('.disparador');
  const cerrarMenu = () => { menu.classList.remove('abierto'); disparador.setAttribute('aria-expanded', 'false'); };
  disparador.onclick = e => {
    e.stopPropagation();
    disparador.setAttribute('aria-expanded', String(menu.classList.toggle('abierto')));
  };
  menu.querySelector('.menu-desplegable').addEventListener('click', e => e.stopPropagation());
  document.addEventListener('click', cerrarMenu);
  document.addEventListener('keydown', e => { if (e.key === 'Escape') cerrarMenu(); });
  $('#salir').onclick = () => { sesion.cerrar(); location.href = '/'; };
  // PWA: "Instalar app" aparece solo si el navegador lo permite (ver /js/tema.js)
  const instalar = $('#instalarApp');
  const mostrarInstalar = () => instalar.classList.toggle('oculto', !window.novapopApp?.instalable());
  document.addEventListener('instalable', mostrarInstalar);
  document.addEventListener('instalada', mostrarInstalar);
  mostrarInstalar();
  instalar.onclick = async () => {
    cerrarMenu();
    if (await window.novapopApp.instalar()) toast('NovaPop quedó instalada');
    mostrarInstalar();
  };

  $('#cambiarPersonaje').onclick = async () => {
    cerrarMenu();
    const actual = sesion.perfil || {};
    const nuevo = await elegirPersonaje(actual.avatar);
    if (!nuevo || nuevo === claveAvatar(actual.avatar)) return;
    try {
      // El PUT necesita el perfil completo: se reenvían sus datos actuales y pin = null (no lo cambia)
      const yo = (await api('/api/perfiles')).find(x => x.id === actual.id);
      await api(`/api/perfiles/${actual.id}`, { method: 'PUT', body: {
        nombre: yo.nombre, avatar: nuevo, esInfantil: yo.esInfantil, idIdiomaPreferido: yo.idIdiomaPreferido, pin: null } });
      sesion.elegirPerfil({ ...actual, avatar: nuevo });
      document.querySelectorAll('.menu-perfil .avatar').forEach(a => a.outerHTML = avatar(nuevo));
      toast(`Ahora eres ${nombrePersonaje(nuevo)}`);
    } catch (e) {
      toast(e.message, 'error');
    }
  };
}

export function pieDePagina() {
  document.body.insertAdjacentHTML('beforeend', `
    <footer class="pie-sitio">
      <p><b>NovaPop</b> · Proyecto académico sin fines de lucro. · <a href="/admin/" style="color:var(--muted);text-decoration:underline">Administración</a></p>
      <p>Películas y series abiertas de Blender Foundation / Blender Studio bajo licencia Creative Commons,
      servidas desde Wikimedia Commons. Los avatares de perfil usan imágenes de esas películas (CC BY), de obras de dominio público y de pósters de TMDB;
      los personajes pertenecen a sus dueños.
      Los demás títulos pertenecen a sus estudios: NovaPop solo muestra su ficha
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

/** Iniciales para cuando una persona no tiene foto. */
export const iniciales = nombre => String(nombre || '').split(' ').filter(Boolean).map(x => x[0]).slice(0, 2).join('');

function edad(iso) {
  const n = new Date(iso + 'T12:00:00'), hoy = new Date();
  return hoy.getFullYear() - n.getFullYear() - (hoy < new Date(hoy.getFullYear(), n.getMonth(), n.getDate()) ? 1 : 0);
}

/** Ficha de un actor o director: sus títulos en NovaPop y el resto de su filmografía (TMDB). */
export async function verPersona(idPersona) {
  const { el, cerrar } = modal('<div class="cargando-modal"><div class="cargando"></div></div>', { clase: 'modal-caja modal-persona' });
  const caja = el.querySelector('.modal-persona');
  let p;
  try {
    p = await api(`/api/catalogo/personas/${idPersona}`, { perfil: true });
  } catch (e) {
    caja.innerHTML = `<p class="error-form">${esc(e.message)}</p><div class="modal-acciones"><button class="btn btn-borde" data-cerrar>Cerrar</button></div>`;
    caja.querySelector('[data-cerrar]').onclick = cerrar;
    return;
  }
  const datos = [
    p.fechaNacimiento ? `Nació el ${fecha(p.fechaNacimiento)} (${edad(p.fechaNacimiento)} años)` : '',
    p.pais ? esc(p.pais) : '',
  ].filter(Boolean).join(' · ');
  const roles = [...new Set(p.enNovaPop.map(t => t.rol).filter(Boolean))].join(', ');
  caja.innerHTML = `
    <button class="btn btn-sutil btn-icono cerrar-persona" data-cerrar aria-label="Cerrar">${icono('cerrar')}</button>
    <header class="persona-cabecera">
      ${p.fotoUrl ? `<img class="persona-foto" src="${esc(p.fotoUrl)}" alt="">` : `<span class="persona-foto">${esc(iniciales(p.nombre))}</span>`}
      <div>
        ${roles ? `<span class="kicker">${esc(roles)}</span>` : ''}
        <h2>${esc(p.nombre)}</h2>
        ${datos ? `<p class="muted">${datos}</p>` : ''}
      </div>
    </header>
    <section class="persona-seccion">
      <h3>En NovaPop <span class="muted">${p.enNovaPop.length}</span></h3>
      ${p.enNovaPop.length ? `<div class="persona-grilla">${p.enNovaPop.map(t => `
        <div class="persona-trabajo">${tarjeta(t.contenido, { vertical: true })}
          <span>${esc(t.personaje || t.rol)}</span></div>`).join('')}</div>`
        : '<p class="muted">Sus títulos no están disponibles en tu país o perfil.</p>'}
    </section>
    ${p.filmografiaDisponible ? `
    <section class="persona-seccion">
      <h3>También participó en</h3>
      ${p.otrosTrabajos.length ? `<div class="persona-fila">${p.otrosTrabajos.map(t => `
        <div class="persona-externo" title="${esc(t.titulo)}">
          <img src="${esc(t.posterUrl)}" alt="" loading="lazy">
          <b>${esc(t.titulo)}</b>
          <small>${[t.anio, t.tipo === 'SERIE' ? 'Serie' : 'Película'].filter(Boolean).join(' · ')}</small>
          ${t.rol ? `<small class="rol">${esc(t.rol)}</small>` : ''}
        </div>`).join('')}</div>
      <p class="fuente">Filmografía: TMDB. Estos títulos no forman parte del catálogo de NovaPop.</p>`
        : '<p class="muted">No encontramos otros trabajos.</p>'}
    </section>` : ''}`;
  caja.querySelector('[data-cerrar]').onclick = cerrar;
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
