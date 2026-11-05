// Botón "Continuar con Google" (Google Identity Services).
// Google entrega al navegador un ID token (credencial); el backend lo valida antes de confiar en él.
import { api } from '/js/api.js';

const CLAVE_PENDIENTE = 'np_google_pendiente';

function cargarScript() {
  return new Promise((resolver, rechazar) => {
    if (window.google?.accounts?.id) return resolver();
    const script = document.createElement('script');
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.onload = resolver;
    script.onerror = rechazar;
    document.head.appendChild(script);
  });
}

/**
 * Dibuja el botón en `contenedor` (y muestra su bloque .acceso-google) y llama a `alRecibir(credencial)` cuando el usuario elige su cuenta.
 * Si Google no está configurado en el servidor, no hace nada y devuelve false.
 */
export async function botonGoogle(contenedor, alRecibir, texto = 'continue_with') {
  try {
    const { googleClientId } = await api('/api/publico/config');
    if (!googleClientId) return false;
    await cargarScript();
    contenedor.closest('.acceso-google')?.classList.remove('oculto');
    google.accounts.id.initialize({ client_id: googleClientId, callback: r => alRecibir(r.credential) });
    const dibujar = () => {
      contenedor.innerHTML = '';
      google.accounts.id.renderButton(contenedor, {
        theme: novapopTema.actual() === 'dark' ? 'filled_black' : 'outline',
        size: 'large', shape: 'pill', text: texto, locale: 'es',
        width: Math.min(400, contenedor.offsetWidth || 320),
      });
    };
    dibujar();
    document.addEventListener('cambio-tema', dibujar);
    return true;
  } catch {
    return false; // sin conexión con Google: se sigue pudiendo usar correo y contraseña
  }
}

/** Lee el correo y el nombre de la credencial solo para mostrarlos; quien la valida es el backend. */
export function datosCredencial(credencial) {
  const base64 = credencial.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
  const json = decodeURIComponent(atob(base64).split('').map(c => '%' + c.charCodeAt(0).toString(16).padStart(2, '0')).join(''));
  const datos = JSON.parse(json);
  return { correo: datos.email, nombre: datos.given_name || datos.name || '' };
}

// Credencial de un correo sin cuenta: el login la deja aquí para que el registro la use
export const pendiente = {
  guardar(credencial) { sessionStorage.setItem(CLAVE_PENDIENTE, credencial); },
  tomar() {
    const credencial = sessionStorage.getItem(CLAVE_PENDIENTE);
    sessionStorage.removeItem(CLAVE_PENDIENTE);
    return credencial;
  },
};
