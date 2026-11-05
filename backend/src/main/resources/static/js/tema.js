// Tema claro / oscuro de NovaPop. Se carga en el <head> (sin "defer") para aplicar el tema
// antes de pintar la página y evitar un parpadeo. Lo usan la app y el panel de administración.
(function () {
  var CLAVE = 'np_tema';
  var raiz = document.documentElement;

  function guardado() {
    try { return localStorage.getItem(CLAVE); } catch (e) { return null; }
  }

  function aplicar(tema) {
    raiz.setAttribute('data-theme', tema);
    raiz.setAttribute('data-bs-theme', tema); // Bootstrap 5.3 usa este atributo
  }

  aplicar(guardado() === 'light' ? 'light' : 'dark');

  window.novapopTema = {
    actual: function () { return raiz.getAttribute('data-theme'); },
    alternar: function () {
      var nuevo = this.actual() === 'dark' ? 'light' : 'dark';
      aplicar(nuevo);
      try { localStorage.setItem(CLAVE, nuevo); } catch (e) { /* sin almacenamiento: solo esta página */ }
      document.dispatchEvent(new CustomEvent('cambio-tema', { detail: nuevo }));
    },
  };
})();
