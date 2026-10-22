// Panel de administración de NovaPop: CRUD de las tablas fuertes generado a partir de /api/admin/recursos
const $ = id => document.getElementById(id);
const esc = v => String(v ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const TAMANO_PAGINA = 15;

const token = localStorage.getItem('np_admin_token');
if (!token) location.replace('/admin/login.html');
const usuario = JSON.parse(localStorage.getItem('np_admin_usuario') || '{}');

let recursos = [];
let resumen = {};
const cacheOpciones = {};
const estado = { recurso: null, pagina: 0, q: '', inactivos: false, editando: null };

// ------------------------------------------------------------------ API
async function api(ruta, { method = 'GET', body } = {}) {
  const r = await fetch('/api/admin' + ruta, {
    method,
    headers: { Authorization: 'Bearer ' + token, ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}) },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  if (r.status === 401) { salir(); throw new Error('Sesión expirada'); }
  if (r.status === 204) return null;
  const texto = await r.text();
  const datos = texto ? JSON.parse(texto) : null;
  if (!r.ok) throw new Error(datos?.mensaje || (r.status === 403 ? 'No tienes permiso para esta acción' : `Error ${r.status}`));
  return datos;
}

function salir() {
  localStorage.removeItem('np_admin_token');
  localStorage.removeItem('np_admin_usuario');
  location.replace('/admin/login.html');
}

function toast(mensaje, tipo = 'success') {
  const el = document.createElement('div');
  el.className = `toast align-items-center text-bg-${tipo} border-0`;
  el.setAttribute('role', 'status');
  el.innerHTML = `<div class="d-flex"><div class="toast-body">${esc(mensaje)}</div>
    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Cerrar"></button></div>`;
  $('toasts').append(el);
  new bootstrap.Toast(el, { delay: 3500 }).show();
  el.addEventListener('hidden.bs.toast', () => el.remove());
}

function confirmar(texto) {
  return new Promise(resolve => {
    const modal = bootstrap.Modal.getOrCreateInstance($('modalConfirmar'));
    $('confirmarTexto').textContent = texto;
    let ok = false;
    $('confirmarSi').onclick = () => { ok = true; modal.hide(); };
    $('modalConfirmar').addEventListener('hidden.bs.modal', () => resolve(ok), { once: true });
    modal.show();
  });
}

// ------------------------------------------------------------------ navegación
function pintarMenu() {
  const grupos = {};
  recursos.forEach(r => (grupos[r.grupo] ||= []).push(r));
  $('navegacion').innerHTML = `
    <nav class="nav flex-column">
      <a class="nav-link ${!estado.recurso ? 'active' : ''}" href="#/"><i class="bi bi-speedometer2"></i> Tablero</a>
      ${Object.entries(grupos).map(([grupo, lista]) => `
        <div class="menu-grupo">${esc(grupo)}</div>
        ${lista.map(r => `<a class="nav-link ${estado.recurso?.clave === r.clave ? 'active' : ''}" href="#/${r.clave}">
            <i class="bi ${r.icono}"></i> ${esc(r.titulo)}
            <span class="badge rounded-pill text-bg-secondary">${resumen[r.clave] ?? ''}</span></a>`).join('')}
      `).join('')}
    </nav>`;
  $('navegacion').querySelectorAll('a').forEach(a => a.addEventListener('click', () =>
    bootstrap.Offcanvas.getInstance($('menu'))?.hide()));
}

function ruta() {
  const clave = location.hash.replace(/^#\/?/, '');
  estado.recurso = recursos.find(r => r.clave === clave) || null;
  estado.pagina = 0;
  estado.q = '';
  estado.inactivos = false;
  pintarMenu();
  estado.recurso ? vistaTabla() : vistaTablero();
}

// ------------------------------------------------------------------ tablero
function vistaTablero() {
  document.title = 'Panel · NovaPop';
  const grupos = {};
  recursos.forEach(r => (grupos[r.grupo] ||= []).push(r));
  const hora = new Date().getHours();
  const saludo = hora < 12 ? 'Buenos días' : hora < 19 ? 'Buenas tardes' : 'Buenas noches';
  $('vista').innerHTML = `
    <section class="hero-admin mb-4">
      <p class="text-uppercase small fw-semibold mb-2 opacity-75">Panel de administración</p>
      <h1 class="display-6 mb-2">${saludo}, ${esc(usuario.nombres || usuario.usuario)}</h1>
      <p class="mb-3 opacity-75" style="max-width: 60ch">Gestiona las ${recursos.length} tablas principales de NovaPop:
        catálogos, contenido, elenco, planes y cuentas. Los cambios quedan registrados en los campos de auditoría.</p>
      <a class="btn btn-warning fw-semibold" href="#/contenidos"><i class="bi bi-film me-1"></i> Gestionar películas y series</a>
    </section>
    ${Object.entries(grupos).map(([grupo, lista]) => `
      <h2 class="h6 text-uppercase text-body-secondary fw-bold mt-4 mb-3">${esc(grupo)}</h2>
      <div class="row g-3">
        ${lista.map(r => `
          <div class="col-12 col-sm-6 col-xl-4">
            <a class="card tarjeta-resumen h-100 text-body" href="#/${r.clave}">
              <div class="card-body d-flex align-items-center gap-3">
                <span class="icono"><i class="bi ${r.icono}"></i></span>
                <div class="flex-grow-1">
                  <div class="fw-semibold">${esc(r.titulo)}</div>
                  <small class="text-body-secondary">${resumen[r.clave] ?? 0} registros activos</small>
                </div>
                <i class="bi bi-chevron-right text-body-secondary"></i>
              </div>
            </a>
          </div>`).join('')}
      </div>`).join('')}`;
}

// ------------------------------------------------------------------ listado
function vistaTabla() {
  const r = estado.recurso;
  document.title = `${r.titulo} · Panel NovaPop`;
  $('vista').innerHTML = `
    <nav aria-label="breadcrumb"><ol class="breadcrumb small mb-2">
      <li class="breadcrumb-item"><a href="#/">Tablero</a></li>
      <li class="breadcrumb-item">${esc(r.grupo)}</li>
      <li class="breadcrumb-item active">${esc(r.titulo)}</li></ol></nav>
    <div class="d-flex flex-wrap align-items-center gap-2 mb-3">
      <h1 class="h3 mb-0 me-auto"><i class="bi ${r.icono} text-warning me-2"></i>${esc(r.titulo)}</h1>
      <button class="btn btn-warning fw-semibold" id="nuevo"><i class="bi bi-plus-lg me-1"></i>Agregar ${esc(r.singular)}</button>
    </div>
    <div class="card">
      <div class="card-body border-bottom d-flex flex-wrap gap-3 align-items-center">
        <div class="input-group" style="max-width: 360px">
          <span class="input-group-text"><i class="bi bi-search"></i></span>
          <input class="form-control" id="buscar" type="search" placeholder="Buscar…" aria-label="Buscar">
        </div>
        <div class="form-check form-switch mb-0">
          <input class="form-check-input" type="checkbox" role="switch" id="verInactivos">
          <label class="form-check-label" for="verInactivos">Mostrar eliminados</label>
        </div>
        <small class="text-body-secondary ms-auto" id="total"></small>
      </div>
      <div class="table-responsive">
        <table class="table table-hover tabla-admin mb-0">
          <thead><tr>
            <th>ID</th>
            ${columnas(r).map(c => `<th>${esc(c.etiqueta)}</th>`).join('')}
            <th>Estado</th><th class="text-end">Acciones</th>
          </tr></thead>
          <tbody id="filas"><tr><td colspan="99" class="text-center py-5"><div class="spinner-border text-warning"></div></td></tr></tbody>
        </table>
      </div>
      <div class="card-footer d-flex justify-content-between align-items-center flex-wrap gap-2">
        <small class="text-body-secondary" id="rango"></small>
        <nav aria-label="Paginación"><ul class="pagination pagination-sm mb-0" id="paginas"></ul></nav>
      </div>
    </div>`;
  $('nuevo').onclick = () => abrirFormulario(null);
  let espera;
  $('buscar').oninput = () => {
    clearTimeout(espera);
    espera = setTimeout(() => { estado.q = $('buscar').value.trim(); estado.pagina = 0; cargarFilas(); }, 300);
  };
  $('verInactivos').onchange = () => { estado.inactivos = $('verInactivos').checked; estado.pagina = 0; cargarFilas(); };
  cargarFilas();
}

const columnas = r => r.campos.filter(c => c.enTabla).slice(0, 6);

function celda(c, v) {
  if (v === null || v === undefined || v === '') return '<span class="text-body-secondary">—</span>';
  switch (c.tipo) {
    case 'REFERENCIA': return esc(v.etiqueta);
    case 'BOOLEANO': return v ? '<span class="badge text-bg-success">Sí</span>' : '<span class="badge text-bg-secondary">No</span>';
    case 'IMAGEN': return `<img class="miniatura" src="${esc(v)}" alt="" loading="lazy" onerror="this.replaceWith('—')">`;
    case 'OPCION': return `<span class="badge text-bg-light border">${esc(v)}</span>`;
    case 'DECIMAL': return 'S/ ' + Number(v).toFixed(2);
    case 'FECHA_HORA': return esc(String(v).replace('T', ' '));
    default: return `<span class="${String(v).length > 40 ? 'd-inline-block text-truncate' : ''}" style="max-width:280px">${esc(v)}</span>`;
  }
}

async function cargarFilas() {
  const r = estado.recurso;
  const p = new URLSearchParams({ pagina: estado.pagina, tamano: TAMANO_PAGINA, inactivos: estado.inactivos });
  if (estado.q) p.set('q', estado.q);
  let datos;
  try {
    datos = await api(`/${r.clave}?${p}`);
  } catch (e) {
    $('filas').innerHTML = `<tr><td colspan="99" class="text-danger p-4">${esc(e.message)}</td></tr>`;
    return;
  }
  const cols = columnas(r);
  $('filas').innerHTML = datos.filas.map(f => `
    <tr class="${f.estadoRegistro === 'I' ? 'fila-inactiva' : ''}">
      <td class="text-body-secondary">${f.id}</td>
      ${cols.map(c => `<td class="${c.tipo === 'TEXTO' ? 'texto' : ''}">${celda(c, f[c.propiedad])}</td>`).join('')}
      <td>${f.estadoRegistro === 'A' ? '<span class="badge text-bg-success">Activo</span>' : '<span class="badge text-bg-danger">Eliminado</span>'}</td>
      <td class="text-end">
        <button class="btn btn-sm btn-outline-secondary" data-editar="${f.id}" title="Editar" aria-label="Editar"><i class="bi bi-pencil"></i></button>
        ${f.estadoRegistro === 'A'
          ? `<button class="btn btn-sm btn-outline-danger" data-eliminar="${f.id}" title="Eliminar" aria-label="Eliminar"><i class="bi bi-trash"></i></button>`
          : `<button class="btn btn-sm btn-outline-success" data-reactivar="${f.id}" title="Reactivar" aria-label="Reactivar"><i class="bi bi-arrow-counterclockwise"></i></button>`}
      </td>
    </tr>`).join('') || `<tr><td colspan="99" class="text-center text-body-secondary py-5">
      <i class="bi bi-inbox display-6 d-block mb-2"></i>${estado.q ? 'Sin resultados para la búsqueda' : 'Aún no hay registros'}</td></tr>`;

  const desde = datos.total ? datos.pagina * datos.tamano + 1 : 0;
  const hasta = Math.min(datos.total, (datos.pagina + 1) * datos.tamano);
  $('total').textContent = `${datos.total} registro${datos.total === 1 ? '' : 's'}`;
  $('rango').textContent = datos.total ? `Mostrando ${desde}–${hasta} de ${datos.total}` : '';
  const paginas = Math.ceil(datos.total / datos.tamano);
  $('paginas').innerHTML = paginas > 1 ? Array.from({ length: paginas }, (_, i) => i)
    .filter(i => i === 0 || i === paginas - 1 || Math.abs(i - datos.pagina) <= 2)
    .map((i, k, arr) => `${k > 0 && i - arr[k - 1] > 1 ? '<li class="page-item disabled"><span class="page-link">…</span></li>' : ''}
      <li class="page-item ${i === datos.pagina ? 'active' : ''}"><button class="page-link" data-pagina="${i}">${i + 1}</button></li>`).join('') : '';

  $('paginas').querySelectorAll('[data-pagina]').forEach(b => b.onclick = () => { estado.pagina = Number(b.dataset.pagina); cargarFilas(); });
  $('filas').querySelectorAll('[data-editar]').forEach(b => b.onclick = () => abrirFormulario(Number(b.dataset.editar)));
  $('filas').querySelectorAll('[data-eliminar]').forEach(b => b.onclick = () => eliminar(Number(b.dataset.eliminar)));
  $('filas').querySelectorAll('[data-reactivar]').forEach(b => b.onclick = () => reactivar(Number(b.dataset.reactivar)));
}

// ------------------------------------------------------------------ formulario
async function opciones(clave) {
  return cacheOpciones[clave] ||= await api(`/${clave}/opciones`);
}

function control(c, valor, editando) {
  const id = 'f_' + c.propiedad;
  const deshabilitado = !c.editable || (c.soloAlCrear && editando);
  const dis = deshabilitado ? 'disabled' : '';
  const req = c.requerido && !deshabilitado ? 'required' : '';
  const asterisco = c.requerido && !deshabilitado ? ' <span class="text-danger">*</span>' : '';
  const ayuda = c.ayuda && !(c.tipo === 'CLAVE' && !editando) ? `<div class="form-text">${esc(c.ayuda)}</div>` : '';
  const etiqueta = `<label class="form-label" for="${id}">${esc(c.etiqueta)}${asterisco}</label>`;
  const ancho = c.tipo === 'TEXTO_LARGO' || c.tipo === 'IMAGEN' ? 'col-12' : 'col-md-6';
  let input;
  switch (c.tipo) {
    case 'TEXTO_LARGO':
      input = `<textarea class="form-control" id="${id}" rows="4" ${dis} ${req}>${esc(valor)}</textarea>`; break;
    case 'ENTERO':
      input = `<input class="form-control" id="${id}" type="number" step="1" value="${esc(valor)}" ${dis} ${req}>`; break;
    case 'DECIMAL':
      input = `<div class="input-group"><span class="input-group-text">S/</span><input class="form-control" id="${id}" type="number" step="0.01" min="0" value="${esc(valor)}" ${dis} ${req}></div>`; break;
    case 'BOOLEANO':
      return `<div class="${ancho} d-flex align-items-end"><div class="form-check form-switch mb-2">
        <input class="form-check-input" type="checkbox" role="switch" id="${id}" ${valor ? 'checked' : ''} ${dis}>
        <label class="form-check-label" for="${id}">${esc(c.etiqueta)}</label></div></div>`;
    case 'FECHA':
      input = `<input class="form-control" id="${id}" type="date" value="${esc(valor || '')}" ${dis} ${req}>`; break;
    case 'FECHA_HORA':
      input = `<input class="form-control" id="${id}" type="datetime-local" value="${esc((valor || '').slice(0, 16))}" ${dis} ${req}>`; break;
    case 'OPCION':
      input = `<select class="form-select" id="${id}" ${dis} ${req}>
        ${c.requerido ? '' : '<option value="">—</option>'}
        ${c.opciones.map(o => `<option ${o === valor ? 'selected' : ''}>${esc(o)}</option>`).join('')}</select>`; break;
    case 'REFERENCIA':
      input = `<select class="form-select" id="${id}" data-referencia="${c.referencia}" data-valor="${valor?.id ?? ''}" ${dis} ${req}>
        <option value="">Cargando…</option></select>`; break;
    case 'CLAVE':
      input = `<input class="form-control" id="${id}" type="password" autocomplete="new-password" minlength="8"
        placeholder="${editando ? '••••••••' : 'Mínimo 8 caracteres'}" ${dis} ${editando ? '' : 'required'}>`; break;
    case 'IMAGEN':
      input = `<input class="form-control" id="${id}" type="url" value="${esc(valor)}" placeholder="https://…" ${dis} ${req}>
        <img class="vista-previa mt-2 ${valor ? '' : 'd-none'}" id="${id}_previa" src="${esc(valor)}" alt="Vista previa">`; break;
    default:
      input = `<input class="form-control" id="${id}" value="${esc(valor)}" ${c.maximo ? `maxlength="${c.maximo}"` : ''} ${dis} ${req}>`;
  }
  return `<div class="${ancho}" data-campo="${c.propiedad}">${etiqueta}${input}${ayuda}</div>`;
}

async function abrirFormulario(id) {
  const r = estado.recurso;
  estado.editando = id;
  let datos = {};
  if (id) {
    try { datos = await api(`/${r.clave}/${id}`); } catch (e) { return toast(e.message, 'danger'); }
  }
  $('formTitulo').textContent = id ? `Editar ${r.singular} #${id}` : `Agregar ${r.singular}`;
  $('formError').classList.add('d-none');
  $('formCampos').innerHTML = r.campos.map(c => control(c, datos[c.propiedad] ?? (c.tipo === 'OPCION' && !id ? c.opciones[0] : ''), !!id)).join('');
  $('formAuditoria').classList.toggle('d-none', !id);
  if (id) {
    $('formAuditoria').innerHTML = `<i class="bi bi-clock-history me-1"></i> Creado por <b>${esc(datos.usuarioCreacion)}</b> el ${esc(datos.fechaCreacion)}
      ${datos.fechaModificacion ? ` · Modificado por <b>${esc(datos.usuarioModificacion)}</b> el ${esc(datos.fechaModificacion)}` : ''}`;
  }
  // Listas desplegables de las claves foráneas
  for (const sel of $('formCampos').querySelectorAll('[data-referencia]')) {
    const campo = r.campos.find(c => 'f_' + c.propiedad === sel.id);
    try {
      const lista = await opciones(sel.dataset.referencia);
      sel.innerHTML = `<option value="">${campo.requerido ? 'Elige una opción…' : '—'}</option>` +
        lista.map(o => `<option value="${o.id}" ${String(o.id) === sel.dataset.valor ? 'selected' : ''}>${esc(o.etiqueta)}</option>`).join('');
    } catch {
      sel.innerHTML = '<option value="">No se pudo cargar</option>';
    }
  }
  // Vista previa de imágenes
  $('formCampos').querySelectorAll('input[type=url]').forEach(inp => inp.addEventListener('input', () => {
    const img = $(inp.id + '_previa');
    img.src = inp.value;
    img.classList.toggle('d-none', !inp.value);
  }));
  // Contenido: los datos de PELICULA solo aplican a películas
  const tipo = $('f_tipoContenido');
  if (tipo) {
    const alternar = () => ['duracionMinutos', 'urlTrailer'].forEach(p =>
      document.querySelector(`[data-campo="${p}"]`).classList.toggle('d-none', tipo.value !== 'PELICULA'));
    tipo.addEventListener('change', alternar);
    alternar();
  }
  bootstrap.Modal.getOrCreateInstance($('modalForm')).show();
}

$('form').addEventListener('submit', async e => {
  e.preventDefault();
  const r = estado.recurso;
  const datos = {};
  for (const c of r.campos) {
    const el = $('f_' + c.propiedad);
    if (!el || el.disabled) continue;
    datos[c.propiedad] = c.tipo === 'BOOLEANO' ? el.checked : el.value;
  }
  const boton = $('formGuardar');
  boton.disabled = true;
  boton.querySelector('.spinner-border').classList.remove('d-none');
  try {
    const id = estado.editando;
    await api(id ? `/${r.clave}/${id}` : `/${r.clave}`, { method: id ? 'PUT' : 'POST', body: datos });
    bootstrap.Modal.getInstance($('modalForm')).hide();
    toast(id ? 'Cambios guardados' : 'Registro creado');
    delete cacheOpciones[r.clave];
    await actualizarResumen();
    cargarFilas();
  } catch (err) {
    $('formError').textContent = err.message;
    $('formError').classList.remove('d-none');
    $('formError').scrollIntoView({ block: 'center' });
  } finally {
    boton.disabled = false;
    boton.querySelector('.spinner-border').classList.add('d-none');
  }
});

async function eliminar(id) {
  const r = estado.recurso;
  if (!await confirmar(`¿Eliminar ${r.singular} #${id}? Quedará inactivo (borrado lógico) y podrás reactivarlo.`)) return;
  try {
    await api(`/${r.clave}/${id}`, { method: 'DELETE' });
    toast('Registro eliminado', 'secondary');
    delete cacheOpciones[r.clave];
    await actualizarResumen();
    cargarFilas();
  } catch (e) {
    toast(e.message, 'danger');
  }
}

async function reactivar(id) {
  const r = estado.recurso;
  try {
    await api(`/${r.clave}/${id}/reactivar`, { method: 'PUT' });
    toast('Registro reactivado');
    delete cacheOpciones[r.clave];
    await actualizarResumen();
    cargarFilas();
  } catch (e) {
    toast(e.message, 'danger');
  }
}

async function actualizarResumen() {
  resumen = await api('/resumen');
  pintarMenu();
}

// ------------------------------------------------------------------ inicio
$('usuarioActual').textContent = usuario.usuario || 'Usuario';
$('rolActual').textContent = usuario.rol ? `Rol: ${usuario.rol}` : '';
$('salir').onclick = salir;

if (token) {
  try {
    [recursos, resumen] = await Promise.all([api('/recursos'), api('/resumen')]);
    addEventListener('hashchange', ruta);
    ruta();
  } catch (e) {
    $('vista').innerHTML = `<div class="alert alert-danger">${esc(e.message)}</div>`;
  }
}
