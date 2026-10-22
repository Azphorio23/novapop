package com.novapop.admin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.novapop.entidad.ClasificacionEdad;
import com.novapop.entidad.Coleccion;
import com.novapop.entidad.Contenido;
import com.novapop.entidad.Cuenta;
import com.novapop.entidad.EntidadAuditable;
import com.novapop.entidad.Genero;
import com.novapop.entidad.Idioma;
import com.novapop.entidad.Pais;
import com.novapop.entidad.PerfilCalidad;
import com.novapop.entidad.Persona;
import com.novapop.entidad.PlanSuscripcion;
import com.novapop.entidad.Productora;
import com.novapop.entidad.Rol;
import com.novapop.entidad.RolProduccion;
import com.novapop.entidad.Usuario;
import com.novapop.entidad.Video;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Las 15 tablas fuertes del modelo que se administran desde el panel, agrupadas por módulo del ERD.
 * Cada recurso describe sus campos una sola vez; el CRUD y los formularios se generan a partir de aquí.
 */
public final class Recursos {

    public enum Tipo { TEXTO, TEXTO_LARGO, ENTERO, DECIMAL, BOOLEANO, FECHA, FECHA_HORA, OPCION, REFERENCIA, CLAVE, IMAGEN }

    /**
     * @param propiedad  nombre del atributo en la entidad JPA
     * @param referencia clave del recurso al que apunta (solo REFERENCIA)
     * @param opciones   valores permitidos (solo OPCION)
     * @param enTabla    se muestra como columna en el listado
     * @param editable   false = solo se muestra (o solo se llena al crear, si soloAlCrear)
     */
    public record Campo(String propiedad, String etiqueta, Tipo tipo, boolean requerido, Integer maximo,
                        String referencia, List<String> opciones, boolean enTabla, boolean editable,
                        boolean soloAlCrear, String ayuda) {
    }

    public record Recurso(String clave, String titulo, String singular, String grupo, String icono,
                          @JsonIgnore Class<? extends EntidadAuditable> entidad, String propiedadId, List<String> etiqueta,
                          List<Campo> campos, boolean soloAdministrador) {
    }

    // ------------------------------------------------------------------ constructores de campos
    private static Campo texto(String p, String e, boolean req, int max) {
        return new Campo(p, e, Tipo.TEXTO, req, max, null, null, true, true, false, null);
    }

    private static Campo largo(String p, String e) {
        return new Campo(p, e, Tipo.TEXTO_LARGO, false, null, null, null, false, true, false, null);
    }

    private static Campo entero(String p, String e, boolean req) {
        return new Campo(p, e, Tipo.ENTERO, req, null, null, null, true, true, false, null);
    }

    private static Campo decimal(String p, String e) {
        return new Campo(p, e, Tipo.DECIMAL, true, null, null, null, true, true, false, null);
    }

    private static Campo booleano(String p, String e) {
        return new Campo(p, e, Tipo.BOOLEANO, false, null, null, null, true, true, false, null);
    }

    private static Campo fecha(String p, String e) {
        return new Campo(p, e, Tipo.FECHA, false, null, null, null, true, true, false, null);
    }

    private static Campo ref(String p, String e, String recurso, boolean req) {
        return new Campo(p, e, Tipo.REFERENCIA, req, null, recurso, null, true, true, false, null);
    }

    private static Campo opcion(String p, String e, String... valores) {
        return new Campo(p, e, Tipo.OPCION, true, null, null, List.of(valores), true, true, false, null);
    }

    private static Campo imagen(String p, String e) {
        return new Campo(p, e, Tipo.IMAGEN, false, 500, null, null, true, true, false, null);
    }

    private static Campo clave(String p, String e) {
        return new Campo(p, e, Tipo.CLAVE, false, 72, null, null, false, true, false,
                "Al editar, déjala vacía para conservar la actual");
    }

    private static Campo soloLectura(Campo c) {
        return new Campo(c.propiedad(), c.etiqueta(), c.tipo(), false, c.maximo(), c.referencia(), c.opciones(),
                c.enTabla(), false, false, c.ayuda());
    }

    private static Campo alCrear(Campo c) {
        return new Campo(c.propiedad(), c.etiqueta(), c.tipo(), c.requerido(), c.maximo(), c.referencia(), c.opciones(),
                c.enTabla(), true, true, "No se puede cambiar después de crearlo");
    }

    private static Campo oculto(Campo c) {
        return new Campo(c.propiedad(), c.etiqueta(), c.tipo(), c.requerido(), c.maximo(), c.referencia(), c.opciones(),
                false, c.editable(), c.soloAlCrear(), c.ayuda());
    }

    private static Campo conAyuda(Campo c, String ayuda) {
        return new Campo(c.propiedad(), c.etiqueta(), c.tipo(), c.requerido(), c.maximo(), c.referencia(), c.opciones(),
                c.enTabla(), c.editable(), c.soloAlCrear(), ayuda);
    }

    // ------------------------------------------------------------------ módulos
    private static final String SEGURIDAD = "Seguridad";
    private static final String GENERALES = "Catálogos generales";
    private static final String CONTENIDO = "Catálogo de contenido";
    private static final String ELENCO = "Elenco y productoras";
    private static final String VIDEO = "Video y planes";
    private static final String CLIENTES = "Clientes";

    private static final Map<String, Recurso> TODOS = new LinkedHashMap<>();

    private static void registrar(Recurso r) {
        TODOS.put(r.clave(), r);
    }

    static {
        // Seguridad (solo ADMINISTRADOR)
        registrar(new Recurso("roles", "Roles", "rol", SEGURIDAD, "bi-shield-lock", Rol.class, "idRol",
                List.of("nombreRol"), List.of(
                texto("nombreRol", "Nombre del rol", true, 50),
                texto("descripcion", "Descripción", false, 255)), true));
        registrar(new Recurso("usuarios", "Usuarios del sistema", "usuario", SEGURIDAD, "bi-person-badge", Usuario.class,
                "idUsuario", List.of("nombreUsuario"), List.of(
                texto("nombreUsuario", "Usuario", true, 50),
                texto("nombres", "Nombres", true, 150),
                texto("correo", "Correo", true, 150),
                ref("rol", "Rol", "roles", true),
                booleano("bloqueado", "Bloqueado"),
                clave("claveHash", "Contraseña")), true));

        // Catálogos generales
        registrar(new Recurso("paises", "Países", "país", GENERALES, "bi-globe-americas", Pais.class, "idPais",
                List.of("nombrePais"), List.of(
                conAyuda(texto("codigoIso", "Código ISO", true, 2), "2 letras, p. ej. PE"),
                texto("nombrePais", "Nombre", true, 100)), false));
        registrar(new Recurso("idiomas", "Idiomas", "idioma", GENERALES, "bi-translate", Idioma.class, "idIdioma",
                List.of("nombreIdioma"), List.of(
                conAyuda(texto("codigoIso", "Código ISO", true, 5), "p. ej. es, en, pt"),
                texto("nombreIdioma", "Nombre", true, 100)), false));
        registrar(new Recurso("generos", "Géneros", "género", GENERALES, "bi-tags", Genero.class, "idGenero",
                List.of("nombreGenero"), List.of(
                texto("nombreGenero", "Nombre", true, 60)), false));
        registrar(new Recurso("clasificaciones", "Clasificaciones por edad", "clasificación", GENERALES, "bi-person-check",
                ClasificacionEdad.class, "idClasificacion", List.of("codigo"), List.of(
                conAyuda(texto("codigo", "Código", true, 10), "p. ej. ATP, +13"),
                texto("nombreClasificacion", "Nombre", true, 100),
                conAyuda(entero("edadMinima", "Edad mínima", true), "Los perfiles infantiles ven hasta 7")), false));

        // Catálogo de contenido
        registrar(new Recurso("contenidos", "Películas y series", "contenido", CONTENIDO, "bi-film", Contenido.class,
                "idContenido", List.of("tituloOriginal"), List.of(
                alCrear(opcion("tipoContenido", "Tipo", "PELICULA", "SERIE")),
                texto("tituloOriginal", "Título original", true, 200),
                texto("tituloLocalizado", "Título en español", false, 200),
                entero("anioLanzamiento", "Año", true),
                ref("clasificacion", "Clasificación", "clasificaciones", true),
                oculto(ref("idiomaOriginal", "Idioma original", "idiomas", true)),
                largo("sinopsis", "Sinopsis"),
                imagen("posterUrl", "Imagen (URL)"),
                // Datos de la tabla PELICULA (subtipo): se guardan junto con el contenido
                new Campo("duracionMinutos", "Duración (min, películas)", Tipo.ENTERO, false, null, null, null, false,
                        true, false, "Solo para películas"),
                new Campo("urlTrailer", "Tráiler de YouTube (películas)", Tipo.TEXTO, false, 500, null, null, false,
                        true, false, "Solo para películas. Ej.: https://www.youtube.com/watch?v=...")), false));
        registrar(new Recurso("colecciones", "Colecciones de la vitrina", "colección", CONTENIDO, "bi-collection-play",
                Coleccion.class, "idColeccion", List.of("nombreColeccion"), List.of(
                texto("nombreColeccion", "Nombre", true, 150),
                opcion("tipoColeccion", "Tipo", "DESTACADO", "FILA", "TOP10", "FRANQUICIA"),
                conAyuda(entero("ordenVitrina", "Orden en el inicio", true), "1 = primera fila"),
                imagen("bannerUrl", "Banner (URL)")), false));

        // Elenco y productoras (datos cargados con la API key de TMDB)
        registrar(new Recurso("personas", "Personas (elenco y equipo)", "persona", ELENCO, "bi-people", Persona.class,
                "idPersona", List.of("nombres", "apellidos"), List.of(
                texto("nombres", "Nombres", true, 100),
                texto("apellidos", "Apellidos", true, 100),
                ref("pais", "País", "paises", false),
                fecha("fechaNacimiento", "Fecha de nacimiento"),
                imagen("fotoUrl", "Foto (URL)")), false));
        registrar(new Recurso("roles-produccion", "Roles de producción", "rol de producción", ELENCO, "bi-camera-reels",
                RolProduccion.class, "idRolProduccion", List.of("nombreRolProduccion"), List.of(
                texto("nombreRolProduccion", "Nombre", true, 60)), false));
        registrar(new Recurso("productoras", "Productoras (empresas)", "productora", ELENCO, "bi-building",
                Productora.class, "idProductora", List.of("nombreProductora"), List.of(
                texto("nombreProductora", "Nombre", true, 150),
                ref("pais", "País", "paises", true)), false));

        // Video y planes
        registrar(new Recurso("calidades", "Perfiles de calidad", "perfil de calidad", VIDEO, "bi-badge-hd",
                PerfilCalidad.class, "idPerfilCalidad", List.of("nombreCalidad"), List.of(
                texto("nombreCalidad", "Nombre", true, 30),
                conAyuda(texto("resolucion", "Resolución", true, 20), "p. ej. 1920x1080"),
                entero("bitrateKbps", "Bitrate (kbps)", true)), false));
        registrar(new Recurso("videos", "Videos", "video", VIDEO, "bi-camera-video", Video.class, "idVideo",
                List.of("codecVideo", "duracionSegundos"), List.of(
                entero("duracionSegundos", "Duración (segundos)", true),
                texto("codecVideo", "Códec", true, 20),
                new Campo("fechaProcesamiento", "Procesado el", Tipo.FECHA_HORA, false, null, null, null, true, true,
                        false, null)), false));
        registrar(new Recurso("planes", "Planes de suscripción", "plan", VIDEO, "bi-credit-card", PlanSuscripcion.class,
                "idPlan", List.of("nombrePlan"), List.of(
                texto("nombrePlan", "Nombre", true, 50),
                decimal("precioMensual", "Precio mensual (S/)"),
                entero("pantallasSimultaneas", "Pantallas a la vez", true),
                ref("perfilCalidadMaxima", "Calidad máxima", "calidades", true),
                booleano("permiteDescarga", "Permite descargas")), false));

        // Clientes
        registrar(new Recurso("cuentas", "Cuentas de clientes", "cuenta", CLIENTES, "bi-person-vcard", Cuenta.class,
                "idCuenta", List.of("correo"), List.of(
                texto("correo", "Correo", true, 150),
                ref("pais", "País", "paises", true),
                opcion("situacion", "Situación", "ACTIVA", "SUSPENDIDA", "CANCELADA"),
                soloLectura(new Campo("fechaRegistro", "Registrada el", Tipo.FECHA_HORA, false, null, null, null, true,
                        false, false, null)),
                clave("claveHash", "Contraseña")), false));
    }

    private Recursos() {
    }

    public static List<Recurso> todos() {
        return List.copyOf(TODOS.values());
    }

    public static Optional<Recurso> buscar(String clave) {
        return Optional.ofNullable(TODOS.get(clave));
    }
}
