package com.novapop.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copias de seguridad.
 * <ul>
 *   <li><b>Administrador</b>: un script SQL con los datos de todas las tablas del esquema, en orden de dependencias
 *       (primero las tablas referenciadas) y con los contadores de los IDs reajustados al final.</li>
 *   <li><b>Usuario</b>: sus propios datos (cuenta, plan, pagos, perfiles y actividad), sin contraseña ni PIN.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class RespaldoService {

    private static final String ESQUEMA = "novapop";
    private static final int FILAS_POR_INSERT = 200;
    private static final Set<Integer> NUMERICOS = Set.of(Types.INTEGER, Types.BIGINT, Types.SMALLINT, Types.TINYINT,
            Types.NUMERIC, Types.DECIMAL, Types.REAL, Types.FLOAT, Types.DOUBLE);

    private final JdbcTemplate jdbc;

    // ------------------------------------------------------------------ respaldo completo (administrador)

    /** Script SQL con los datos de todas las tablas. Se restaura sobre una base con el esquema creado y vacía. */
    @Transactional(readOnly = true)
    public String respaldoCompleto(String generadoPor) {
        List<String> tablas = tablasEnOrden();
        StringBuilder sql = new StringBuilder(1 << 20);
        sql.append("""
                -- =====================================================================
                --  NovaPop — Respaldo de datos
                --  Generado: %s por %s
                --  Tablas: %d (esquema "%s")
                --
                --  Para restaurar:
                --    1. En una base vacía, ejecuta database/01_esquema.sql, 03_migracion_solo_trailer.sql
                --       y 07_posters_verticales.sql (crean las tablas y columnas, sin datos).
                --    2. Ejecuta este archivo completo. Si algo falla, no se guarda nada (BEGIN ... COMMIT).
                --
                --  CONTIENE DATOS PERSONALES Y CONTRASEÑAS CIFRADAS: guárdalo en un lugar seguro.
                -- =====================================================================
                BEGIN;
                SET search_path TO %s;

                """.formatted(OffsetDateTime.now(), generadoPor, tablas.size(), ESQUEMA, ESQUEMA));
        Map<String, Integer> filas = new LinkedHashMap<>();
        for (String tabla : tablas) {
            filas.put(tabla, volcar(tabla, sql));
        }
        sql.append("-- Contadores de los IDs: el próximo registro continúa después del último restaurado\n");
        for (Map<String, Object> c : jdbc.queryForList("""
                select table_name, column_name from information_schema.columns
                 where table_schema = ? and is_identity = 'YES' order by table_name""", ESQUEMA)) {
            String t = (String) c.get("table_name"), col = (String) c.get("column_name");
            sql.append("SELECT setval(pg_get_serial_sequence('%s.%s', '%s'), COALESCE((SELECT MAX(%s) FROM %s), 0) + 1, false);\n"
                    .formatted(ESQUEMA, t, col, col, t));
        }
        sql.append("\nCOMMIT;\n\n-- Filas por tabla: ").append(filas).append('\n');
        return sql.toString();
    }

    /** Escribe los INSERT de una tabla y devuelve cuántas filas tenía. */
    private int volcar(String tabla, StringBuilder sql) {
        int[] total = {0};
        List<String> valores = new ArrayList<>();
        String[] columnas = {null};
        jdbc.query("select * from " + ESQUEMA + "." + tabla + " order by 1", rs -> {
            ResultSetMetaData md = rs.getMetaData();
            if (columnas[0] == null) {
                List<String> nombres = new ArrayList<>();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    nombres.add(md.getColumnName(i));
                }
                columnas[0] = String.join(", ", nombres);
            }
            List<String> fila = new ArrayList<>();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                int tipo = md.getColumnType(i);
                if (rs.getObject(i) == null) {
                    fila.add("NULL");
                } else if (tipo == Types.BOOLEAN || tipo == Types.BIT) {
                    fila.add(rs.getBoolean(i) ? "TRUE" : "FALSE");
                } else if (NUMERICOS.contains(tipo)) {
                    fila.add(rs.getString(i));
                } else { // texto, fechas (con su zona horaria), etc.: el texto que da PostgreSQL, entre comillas
                    fila.add("'" + rs.getString(i).replace("'", "''") + "'");
                }
            }
            valores.add("(" + String.join(", ", fila) + ")");
            total[0]++;
        });
        sql.append("-- ").append(tabla).append(": ").append(total[0]).append(" filas\n");
        for (int i = 0; i < valores.size(); i += FILAS_POR_INSERT) {
            sql.append("INSERT INTO ").append(tabla).append(" (").append(columnas[0]).append(") VALUES\n  ")
                    .append(String.join(",\n  ", valores.subList(i, Math.min(i + FILAS_POR_INSERT, valores.size()))))
                    .append(";\n");
        }
        sql.append('\n');
        return total[0];
    }

    /** Tablas del esquema ordenadas para que cada una vaya después de las que referencia (orden topológico). */
    private List<String> tablasEnOrden() {
        List<String> todas = jdbc.queryForList("""
                select table_name from information_schema.tables
                 where table_schema = ? and table_type = 'BASE TABLE' order by table_name""", String.class, ESQUEMA);
        Map<String, Set<String>> depende = new HashMap<>();
        todas.forEach(t -> depende.put(t, new HashSet<>()));
        for (Map<String, Object> fk : jdbc.queryForList("""
                select tc.table_name as tabla, ccu.table_name as referencia
                  from information_schema.table_constraints tc
                  join information_schema.constraint_column_usage ccu
                    on ccu.constraint_name = tc.constraint_name and ccu.constraint_schema = tc.constraint_schema
                 where tc.constraint_type = 'FOREIGN KEY' and tc.table_schema = ?""", ESQUEMA)) {
            String t = (String) fk.get("tabla"), ref = (String) fk.get("referencia");
            if (!t.equals(ref) && depende.containsKey(ref)) {
                depende.get(t).add(ref);
            }
        }
        List<String> orden = new ArrayList<>();
        Set<String> listas = new HashSet<>();
        while (orden.size() < todas.size()) {
            boolean avanzo = false;
            for (String t : todas) {
                if (!listas.contains(t) && listas.containsAll(depende.get(t))) {
                    orden.add(t);
                    listas.add(t);
                    avanzo = true;
                }
            }
            if (!avanzo) {
                throw new IllegalStateException("Hay dependencias circulares entre tablas: " + depende);
            }
        }
        return orden;
    }

    // ------------------------------------------------------------------ mis datos (usuario)

    /** Todos los datos de una cuenta, para que el usuario los descargue (sin la contraseña cifrada ni el PIN). */
    @Transactional(readOnly = true)
    public Map<String, Object> datosDeCuenta(Long idCuenta) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("generado", OffsetDateTime.now().toString());
        datos.put("cuenta", jdbc.queryForMap("""
                select c.id_cuenta, c.correo, p.nombre_pais as pais, c.fecha_registro, c.situacion
                  from novapop.cuenta c join novapop.pais p on p.id_pais = c.id_pais
                 where c.id_cuenta = ?""", idCuenta));
        datos.put("suscripciones", jdbc.queryForList("""
                select s.id_suscripcion, pl.nombre_plan as plan, s.fecha_inicio, s.fecha_fin, s.situacion_suscripcion,
                       s.renovacion_automatica
                  from novapop.suscripcion s join novapop.plan_suscripcion pl on pl.id_plan = s.id_plan
                 where s.id_cuenta = ? order by s.fecha_inicio""", idCuenta));
        datos.put("metodosDePago", jdbc.queryForList("""
                select tipo_metodo, numero_enmascarado, es_predeterminado from novapop.metodo_pago
                 where id_cuenta = ? and estado_registro = 'A'""", idCuenta));
        datos.put("pagos", jdbc.queryForList("""
                select pg.fecha_pago, pg.monto, pg.situacion_pago, m.tipo_metodo, m.numero_enmascarado
                  from novapop.pago_suscripcion pg
                  join novapop.suscripcion s on s.id_suscripcion = pg.id_suscripcion
                  join novapop.metodo_pago m on m.id_metodo_pago = pg.id_metodo_pago
                 where s.id_cuenta = ? order by pg.fecha_pago""", idCuenta));
        datos.put("dispositivos", jdbc.queryForList("""
                select tipo_dispositivo, nombre_dispositivo, ultima_conexion from novapop.dispositivo
                 where id_cuenta = ? and estado_registro = 'A'""", idCuenta));
        List<Map<String, Object>> perfiles = new ArrayList<>();
        for (Map<String, Object> p : jdbc.queryForList("""
                select p.id_perfil, p.nombre_perfil, p.avatar_url, p.es_infantil,
                       (p.pin_control_parental is not null) as tiene_pin, i.nombre_idioma as idioma_preferido
                  from novapop.perfil p join novapop.idioma i on i.id_idioma = p.id_idioma_preferido
                 where p.id_cuenta = ? and p.estado_registro = 'A' order by p.id_perfil""", idCuenta)) {
            Object id = p.get("id_perfil");
            p.put("miLista", jdbc.queryForList("""
                    select c.titulo_original as titulo, ml.fecha_agregado from novapop.mi_lista ml
                      join novapop.contenido c on c.id_contenido = ml.id_contenido
                     where ml.id_perfil = ? and ml.estado_registro = 'A' order by ml.fecha_agregado""", id));
            p.put("historial", jdbc.queryForList("""
                    select c.titulo_original as titulo, e.titulo_episodio as episodio, h.segundo_visto, h.completado,
                           h.ultima_actualizacion
                      from novapop.historial_reproduccion h
                      join novapop.contenido c on c.id_contenido = h.id_contenido
                      left join novapop.episodio e on e.id_episodio = h.id_episodio
                     where h.id_perfil = ? order by h.ultima_actualizacion desc""", id));
            p.put("calificaciones", jdbc.queryForList("""
                    select c.titulo_original as titulo, cc.valoracion, cc.fecha_calificacion
                      from novapop.calificacion_contenido cc join novapop.contenido c on c.id_contenido = cc.id_contenido
                     where cc.id_perfil = ? and cc.estado_registro = 'A' order by cc.fecha_calificacion""", id));
            perfiles.add(p);
        }
        datos.put("perfiles", perfiles);
        return datos;
    }
}
