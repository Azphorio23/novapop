package com.novapop.admin;

import com.novapop.admin.Recursos.Campo;
import com.novapop.admin.Recursos.Recurso;
import com.novapop.admin.Recursos.Tipo;
import com.novapop.entidad.Contenido;
import com.novapop.entidad.ContenidoPais;
import com.novapop.entidad.Cuenta;
import com.novapop.entidad.EntidadAuditable;
import com.novapop.entidad.Pais;
import com.novapop.entidad.Pelicula;
import com.novapop.entidad.SituacionCuenta;
import com.novapop.entidad.TipoContenido;
import com.novapop.excepcion.NovaPopException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * CRUD genérico de las tablas fuertes: listar, ver, crear, editar, eliminar (borrado lógico) y reactivar.
 * Usa la descripción de cada tabla en {@link Recursos}; los campos de auditoría los llena AuditoriaListener.
 */
@Service
@RequiredArgsConstructor
public class AdminCrudService {

    private static final ZoneId ZONA = ZoneId.of("America/Lima");

    private final EntityManager em;
    private final PasswordEncoder passwordEncoder;

    public record Pagina(long total, int pagina, int tamano, List<Map<String, Object>> filas) {
    }

    public record Opcion(Long id, String etiqueta) {
    }

    // ------------------------------------------------------------------ lectura

    @Transactional(readOnly = true)
    public Pagina listar(Recurso r, String busqueda, boolean incluirInactivos, int pagina, int tamano) {
        String q = busqueda == null ? "" : normalizar(busqueda);
        List<? extends EntidadAuditable> filtrados = todos(r).stream()
                .filter(e -> incluirInactivos || e.estaActivo())
                .filter(e -> q.isEmpty() || coincide(r, e, q))
                .toList();
        int desde = Math.min(pagina * tamano, filtrados.size());
        int hasta = Math.min(desde + tamano, filtrados.size());
        List<Map<String, Object>> filas = filtrados.subList(desde, hasta).stream().map(e -> aMapa(r, e)).toList();
        return new Pagina(filtrados.size(), pagina, tamano, filas);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtener(Recurso r, Long id) {
        return aMapa(r, buscar(r, id));
    }

    @Transactional(readOnly = true)
    public List<Opcion> opciones(Recurso r) {
        return todos(r).stream()
                .filter(EntidadAuditable::estaActivo)
                .map(e -> new Opcion(id(r, e), etiqueta(r, e)))
                .sorted(Comparator.comparing(Opcion::etiqueta, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarActivos(Recurso r) {
        return em.createQuery("select count(e) from " + r.entidad().getSimpleName() + " e where e.estadoRegistro = 'A'",
                Long.class).getSingleResult();
    }

    // ------------------------------------------------------------------ escritura

    @Transactional
    public Map<String, Object> crear(Recurso r, Map<String, Object> datos) {
        EntidadAuditable entidad;
        try {
            entidad = r.entidad().getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No se pudo crear " + r.entidad().getSimpleName(), e);
        }
        aplicar(r, entidad, datos, true);
        if (entidad instanceof Cuenta cuenta) {
            cuenta.setFechaRegistro(OffsetDateTime.now());
            if (cuenta.getSituacion() == null) {
                cuenta.setSituacion(SituacionCuenta.ACTIVA);
            }
        }
        guardar(() -> em.persist(entidad));
        if (entidad instanceof Contenido contenido) {
            guardarDatosPelicula(contenido, datos, true);
            habilitarEnTodosLosPaises(contenido);
        }
        return aMapa(r, entidad);
    }

    @Transactional
    public Map<String, Object> actualizar(Recurso r, Long id, Map<String, Object> datos) {
        EntidadAuditable entidad = buscar(r, id);
        aplicar(r, entidad, datos, false);
        guardar(em::flush);
        if (entidad instanceof Contenido contenido) {
            guardarDatosPelicula(contenido, datos, false);
        }
        return aMapa(r, entidad);
    }

    /** Borrado lógico: estado_registro = 'I'. El registro se conserva y se puede reactivar. */
    @Transactional
    public void eliminar(Recurso r, Long id) {
        buscar(r, id).inactivar();
    }

    @Transactional
    public Map<String, Object> reactivar(Recurso r, Long id) {
        EntidadAuditable entidad = buscar(r, id);
        entidad.setEstadoRegistro(EntidadAuditable.ACTIVO);
        guardar(em::flush);
        return aMapa(r, entidad);
    }

    // ------------------------------------------------------------------ conversión de datos

    private void aplicar(Recurso r, EntidadAuditable entidad, Map<String, Object> datos, boolean creando) {
        BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(entidad);
        for (Campo c : r.campos()) {
            if (!c.editable() || (c.soloAlCrear() && !creando) || !bean.isWritableProperty(c.propiedad())) {
                continue; // solo lectura, o campo de otra tabla (p. ej. datos de PELICULA)
            }
            Object valor = datos.get(c.propiedad());
            boolean vacio = valor == null || valor.toString().isBlank();

            if (c.tipo() == Tipo.CLAVE) {
                if (vacio && creando) {
                    throw NovaPopException.invalido("La contraseña es obligatoria");
                }
                if (!vacio) {
                    String clave = valor.toString();
                    if (clave.length() < 8) {
                        throw NovaPopException.invalido("La contraseña debe tener al menos 8 caracteres");
                    }
                    bean.setPropertyValue(c.propiedad(), passwordEncoder.encode(clave));
                }
                continue;
            }
            if (vacio) {
                if (c.requerido()) {
                    throw NovaPopException.invalido("El campo \"" + c.etiqueta() + "\" es obligatorio");
                }
                bean.setPropertyValue(c.propiedad(), c.tipo() == Tipo.BOOLEANO ? Boolean.FALSE : null);
                continue;
            }
            bean.setPropertyValue(c.propiedad(), convertir(c, bean.getPropertyType(c.propiedad()), valor));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object convertir(Campo c, Class<?> destino, Object valor) {
        String texto = valor.toString().trim();
        try {
            return switch (c.tipo()) {
                case REFERENCIA -> {
                    Recurso ref = Recursos.buscar(c.referencia()).orElseThrow();
                    Object encontrado = em.find(ref.entidad(), Long.valueOf(texto));
                    if (encontrado == null) {
                        throw NovaPopException.invalido("El valor elegido en \"" + c.etiqueta() + "\" no existe");
                    }
                    yield encontrado;
                }
                case ENTERO -> {
                    long n = new BigDecimal(texto).longValueExact();
                    if (destino == Short.class) {
                        if (n < Short.MIN_VALUE || n > Short.MAX_VALUE) {
                            throw NovaPopException.invalido("\"" + c.etiqueta() + "\" está fuera de rango");
                        }
                        yield (short) n;
                    }
                    yield destino == Integer.class ? Math.toIntExact(n) : n;
                }
                case DECIMAL -> new BigDecimal(texto);
                case BOOLEANO -> Boolean.valueOf(texto);
                case FECHA -> LocalDate.parse(texto.length() > 10 ? texto.substring(0, 10) : texto);
                case FECHA_HORA -> texto.length() > 16 && (texto.endsWith("Z") || texto.matches(".*[+-]\\d\\d:\\d\\d$"))
                        ? OffsetDateTime.parse(texto)
                        : LocalDateTime.parse(texto.length() == 16 ? texto + ":00" : texto).atZone(ZONA).toOffsetDateTime();
                case OPCION -> {
                    if (c.opciones() != null && !c.opciones().contains(texto)) {
                        throw NovaPopException.invalido("Valor no permitido en \"" + c.etiqueta() + "\"");
                    }
                    yield destino.isEnum() ? Enum.valueOf((Class<Enum>) destino, texto) : texto;
                }
                default -> {
                    if (c.maximo() != null && texto.length() > c.maximo()) {
                        throw NovaPopException.invalido("\"" + c.etiqueta() + "\" admite como máximo " + c.maximo()
                                + " caracteres");
                    }
                    yield texto;
                }
            };
        } catch (NumberFormatException | ArithmeticException e) {
            throw NovaPopException.invalido("\"" + c.etiqueta() + "\" debe ser un número válido");
        } catch (DateTimeParseException e) {
            throw NovaPopException.invalido("\"" + c.etiqueta() + "\" debe ser una fecha válida");
        }
    }

    private Map<String, Object> aMapa(Recurso r, EntidadAuditable e) {
        BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(e);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id(r, e));
        m.put("etiqueta", etiqueta(r, e));
        for (Campo c : r.campos()) {
            if (c.tipo() == Tipo.CLAVE || !bean.isReadableProperty(c.propiedad())) {
                continue; // las contraseñas nunca salen del servidor
            }
            Object valor = bean.getPropertyValue(c.propiedad());
            if (c.tipo() == Tipo.REFERENCIA && valor != null) {
                Recurso ref = Recursos.buscar(c.referencia()).orElseThrow();
                EntidadAuditable relacionada = (EntidadAuditable) valor;
                valor = Map.of("id", id(ref, relacionada), "etiqueta", etiqueta(ref, relacionada));
            } else if (valor instanceof Enum<?> en) {
                valor = en.name();
            } else if (valor instanceof OffsetDateTime f) {
                valor = f.atZoneSameInstant(ZONA).toLocalDateTime().withSecond(0).withNano(0).toString();
            } else if (valor != null && !(valor instanceof Number) && !(valor instanceof Boolean)) {
                valor = valor.toString();
            }
            m.put(c.propiedad(), valor);
        }
        if (e instanceof Contenido c && c.getTipoContenido() == TipoContenido.PELICULA) {
            Pelicula p = em.find(Pelicula.class, c.getIdContenido());
            m.put("duracionMinutos", p != null ? p.getDuracionMinutos() : null);
            m.put("urlTrailer", p != null ? p.getUrlTrailer() : null);
        }
        m.put("estadoRegistro", e.getEstadoRegistro());
        m.put("usuarioCreacion", e.getUsuarioCreacion());
        m.put("fechaCreacion", fechaTexto(e.getFechaCreacion()));
        m.put("usuarioModificacion", e.getUsuarioModificacion());
        m.put("fechaModificacion", fechaTexto(e.getFechaModificacion()));
        return m;
    }

    // ------------------------------------------------------------------ reglas propias de CONTENIDO

    /** Las películas guardan su duración y tráiler en la tabla PELICULA (subtipo de CONTENIDO). */
    private void guardarDatosPelicula(Contenido c, Map<String, Object> datos, boolean creando) {
        if (c.getTipoContenido() != TipoContenido.PELICULA) {
            return;
        }
        Pelicula p = em.find(Pelicula.class, c.getIdContenido());
        if (p == null) {
            p = new Pelicula();
            p.setContenido(c);
        }
        Object duracion = datos.get("duracionMinutos");
        if (duracion != null && !duracion.toString().isBlank()) {
            try {
                short minutos = Short.parseShort(duracion.toString().trim());
                if (minutos <= 0) {
                    throw new NumberFormatException();
                }
                p.setDuracionMinutos(minutos);
            } catch (NumberFormatException e) {
                throw NovaPopException.invalido("La duración debe ser un número de minutos mayor que 0");
            }
        } else if (p.getDuracionMinutos() == null) {
            throw NovaPopException.invalido("Las películas necesitan una duración en minutos");
        }
        if (datos.containsKey("urlTrailer")) {
            Object url = datos.get("urlTrailer");
            p.setUrlTrailer(url == null || url.toString().isBlank() ? null : url.toString().trim());
        }
        Pelicula aGuardar = p;
        guardar(() -> {
            if (creando || !em.contains(aGuardar)) {
                em.persist(aGuardar);
            }
            em.flush();
        });
    }

    /** Un contenido nuevo queda disponible desde hoy en todos los países activos. */
    private void habilitarEnTodosLosPaises(Contenido c) {
        List<Pais> paises = em.createQuery("select p from Pais p where p.estadoRegistro = 'A'", Pais.class).getResultList();
        for (Pais pais : paises) {
            ContenidoPais cp = new ContenidoPais();
            cp.setContenido(c);
            cp.setPais(pais);
            cp.setFechaDisponibleDesde(LocalDate.now());
            em.persist(cp);
        }
        guardar(em::flush);
    }

    // ------------------------------------------------------------------ auxiliares

    private List<? extends EntidadAuditable> todos(Recurso r) {
        return em.createQuery("select e from " + r.entidad().getSimpleName() + " e order by e." + r.propiedadId() + " desc",
                r.entidad()).getResultList();
    }

    private EntidadAuditable buscar(Recurso r, Long id) {
        EntidadAuditable e = em.find(r.entidad(), id);
        if (e == null) {
            throw NovaPopException.noEncontrado("No existe " + r.singular() + " con id " + id);
        }
        return e;
    }

    private static Long id(Recurso r, EntidadAuditable e) {
        return (Long) PropertyAccessorFactory.forBeanPropertyAccess(e).getPropertyValue(r.propiedadId());
    }

    private static String etiqueta(Recurso r, EntidadAuditable e) {
        BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(e);
        return r.etiqueta().stream()
                .map(bean::getPropertyValue)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(" "));
    }

    private boolean coincide(Recurso r, EntidadAuditable e, String q) {
        BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(e);
        if (String.valueOf(id(r, e)).equals(q)) {
            return true;
        }
        return r.campos().stream()
                .filter(c -> c.tipo() == Tipo.TEXTO || c.tipo() == Tipo.OPCION)
                .filter(c -> bean.isReadableProperty(c.propiedad()))
                .map(c -> bean.getPropertyValue(c.propiedad()))
                .filter(Objects::nonNull)
                .anyMatch(v -> normalizar(v.toString()).contains(q));
    }

    private static String fechaTexto(OffsetDateTime f) {
        return f == null ? null : f.atZoneSameInstant(ZONA).toLocalDateTime().withNano(0).toString().replace('T', ' ');
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).trim();
    }

    /** Ejecuta un INSERT/UPDATE y traduce los errores de PostgreSQL a mensajes entendibles. */
    private void guardar(Runnable operacion) {
        try {
            operacion.run();
            em.flush();
        } catch (PersistenceException e) {
            throw NovaPopException.conflicto(mensajeBaseDatos(e));
        }
    }

    static String mensajeBaseDatos(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SQLException sql && sql.getSQLState() != null) {
                return switch (sql.getSQLState()) {
                    case "23505" -> "Ya existe un registro con ese valor (debe ser único)";
                    case "23503" -> "Un valor hace referencia a un registro que no existe";
                    case "23514" -> "Algún valor no cumple las reglas de la tabla (restricción CHECK)";
                    case "23502" -> "Falta un dato obligatorio";
                    case "P0001" -> sql.getMessage().replaceFirst("(?s)^ERROR:\\s*", "").split("\n")[0];
                    default -> "La base de datos rechazó la operación (" + sql.getSQLState() + ")";
                };
            }
        }
        return "La base de datos rechazó la operación";
    }
}
