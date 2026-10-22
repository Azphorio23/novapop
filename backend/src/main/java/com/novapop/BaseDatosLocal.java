package com.novapop;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Modo local: un PostgreSQL 17 embebido con los scripts de la carpeta database/ ya ejecutados.
 * Se usa cuando no hay credenciales de Supabase, así el proyecto se puede probar en cualquier computadora
 * solo con "Run". Cada arranque parte de los datos de prueba originales.
 */
public final class BaseDatosLocal {

    private static final String VARIABLE_URL = "NOVAPOP_DB_URL";

    private BaseDatosLocal() {
    }

    /** ¿Hay credenciales de Supabase en variables de entorno o en un archivo .env? */
    public static boolean hayCredenciales() {
        String valor = System.getProperty(VARIABLE_URL, System.getenv(VARIABLE_URL));
        if (valor != null && !valor.isBlank()) {
            return true;
        }
        for (Path env : List.of(Path.of(".env"), Path.of("backend", ".env"))) {
            try {
                if (Files.exists(env) && Files.readAllLines(env).stream()
                        .anyMatch(l -> l.startsWith(VARIABLE_URL + "=") && l.length() > VARIABLE_URL.length() + 1)) {
                    return true;
                }
            } catch (IOException ignorada) {
                // si no se puede leer, se considera que no hay credenciales
            }
        }
        return false;
    }

    /** Levanta la base embebida y le indica a Spring que se conecte a ella. */
    public static void iniciarModoLocal() throws Exception {
        System.out.println("""

                ==================================================================
                  NovaPop - MODO LOCAL
                  No se encontraron credenciales de Supabase (backend/.env), así que
                  se usará una base PostgreSQL temporal en esta computadora.
                ==================================================================
                """);
        EmbeddedPostgres pg = iniciar(0, true);
        System.setProperty(VARIABLE_URL, pg.getJdbcUrl("postgres", "postgres"));
        System.setProperty("NOVAPOP_DB_USER", "postgres");
        System.setProperty("NOVAPOP_DB_PASSWORD", "postgres");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                pg.close();
            } catch (Exception ignorada) {
                // la aplicación se está cerrando
            }
        }));
    }

    /**
     * @param puerto              0 = cualquiera libre
     * @param incluirCatalogoTmdb ejecuta también 04_catalogo_tmdb.sql si existe (las pruebas no lo usan)
     */
    public static EmbeddedPostgres iniciar(int puerto, boolean incluirCatalogoTmdb) throws Exception {
        Path carpeta = carpetaDatabase();
        List<String> scripts = new ArrayList<>(List.of("01_esquema.sql", "02_datos_prueba.sql", "03_migracion_solo_trailer.sql", "05_administracion.sql"));
        if (incluirCatalogoTmdb && Files.exists(carpeta.resolve("04_catalogo_tmdb.sql"))) {
            scripts.add("04_catalogo_tmdb.sql");
        }
        EmbeddedPostgres.Builder builder = EmbeddedPostgres.builder();
        if (puerto > 0) {
            builder.setPort(puerto);
        }
        EmbeddedPostgres pg = builder.start();
        try (Connection c = pg.getPostgresDatabase().getConnection(); Statement st = c.createStatement()) {
            for (String script : scripts) {
                st.execute(Files.readString(carpeta.resolve(script)));
            }
        }
        return pg;
    }

    /** La carpeta database/ está junto a backend/: se busca desde la raíz del proyecto o desde backend. */
    private static Path carpetaDatabase() {
        for (Path p : List.of(Path.of("database"), Path.of("..", "database"))) {
            if (Files.exists(p.resolve("01_esquema.sql"))) {
                return p;
            }
        }
        throw new IllegalStateException("No se encontró la carpeta database/ con los scripts SQL. "
                + "Ejecuta NovaPop desde la carpeta del proyecto o desde backend/.");
    }
}
