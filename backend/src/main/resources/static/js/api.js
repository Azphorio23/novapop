// Cliente de la API de NovaPop: token, perfil activo y peticiones fetch.

const CLAVE_TOKEN = 'np_token';
const CLAVE_CORREO = 'np_correo';
const CLAVE_PERFIL = 'np_perfil';

function leer(clave) {
  try { return localStorage.getItem(clave); } catch { return null; }
}
function escribir(clave, valor) {
  try { valor === null ? localStorage.removeItem(clave) : localStorage.setItem(clave, valor); } catch { /* sin almacenamiento */ }
}

export const sesion = {
  get token() { return leer(CLAVE_TOKEN); },
  get correo() { return leer(CLAVE_CORREO); },
  get perfil() {
    try { return JSON.parse(leer(CLAVE_PERFIL)); } catch { return null; }
  },
  iniciar({ token, correo }) {
    escribir(CLAVE_TOKEN, token);
    escribir(CLAVE_CORREO, correo);
    escribir(CLAVE_PERFIL, null);
  },
  elegirPerfil(perfil) {
    escribir(CLAVE_PERFIL, JSON.stringify({ id: perfil.id, nombre: perfil.nombre, avatar: perfil.avatar, esInfantil: perfil.esInfantil }));
  },
  olvidarPerfil() { escribir(CLAVE_PERFIL, null); },
  cerrar() {
    [CLAVE_TOKEN, CLAVE_CORREO, CLAVE_PERFIL].forEach(c => escribir(c, null));
  },
  // El id de dispositivo se guarda por cuenta: este navegador es un DISPOSITIVO registrado
  get dispositivo() { return leer('np_dispositivo_' + this.correo); },
  set dispositivo(id) { escribir('np_dispositivo_' + this.correo, id === null ? null : String(id)); },
};

export class ErrorApi extends Error {
  constructor(estado, mensaje) {
    super(mensaje);
    this.estado = estado;
  }
}

/**
 * Llama a la API. Opciones: method, body (objeto → JSON), perfil (true = envía X-Perfil-Id), keepalive.
 * Si el token expiró (401) devuelve al login.
 */
export async function api(ruta, { method = 'GET', body, perfil = false, keepalive = false } = {}) {
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (sesion.token) headers.Authorization = 'Bearer ' + sesion.token;
  if (perfil && sesion.perfil) headers['X-Perfil-Id'] = sesion.perfil.id;

  let respuesta;
  try {
    respuesta = await fetch(ruta, { method, headers, keepalive, body: body !== undefined ? JSON.stringify(body) : undefined });
  } catch {
    throw new ErrorApi(0, 'No se pudo conectar con el servidor. ¿Está encendido el backend?');
  }

  if (respuesta.status === 401 && sesion.token && !ruta.startsWith('/api/auth/')) {
    sesion.cerrar();
    location.href = '/login.html?expirada=1';
    throw new ErrorApi(401, 'Tu sesión expiró');
  }
  if (respuesta.status === 204) return null;

  const texto = await respuesta.text();
  // 403 sin mensaje = el token no tiene permisos de cliente (p. ej. uno antiguo): se vuelve a iniciar sesión
  if (respuesta.status === 403 && !texto && sesion.token) {
    sesion.cerrar();
    location.href = '/login.html?expirada=1';
    throw new ErrorApi(403, 'Tu sesión expiró');
  }
  const datos = texto ? JSON.parse(texto) : null;
  if (!respuesta.ok) {
    throw new ErrorApi(respuesta.status, datos?.mensaje || datos?.error || `Error ${respuesta.status}`);
  }
  return datos;
}

/** Redirige al login si no hay token. */
export function exigirSesion() {
  if (!sesion.token) {
    location.replace('/login.html');
    return false;
  }
  return true;
}

/** Redirige a la selección de perfiles si aún no se eligió uno. */
export function exigirPerfil() {
  if (!exigirSesion()) return false;
  if (!sesion.perfil) {
    location.replace('/perfiles.html');
    return false;
  }
  return true;
}
