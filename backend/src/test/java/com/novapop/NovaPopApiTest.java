package com.novapop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de punta a punta: levanta PostgreSQL 17 embebido, ejecuta database/01_esquema.sql y
 * 02_datos_prueba.sql, arranca Spring (ddl-auto=validate comprueba entidades vs. tablas) y recorre la API.
 */
@SpringBootTest
@AutoConfigureMockMvc
class NovaPopApiTest {

    private static EmbeddedPostgres pg;

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;
    @Autowired
    JdbcTemplate jdbc;

    @DynamicPropertySource
    static void baseDeDatos(DynamicPropertyRegistry registro) throws Exception {
        pg = BaseDatosLocal.iniciar(0, false);
        registro.add("NOVAPOP_DB_URL", () -> pg.getJdbcUrl("postgres", "postgres"));
        registro.add("NOVAPOP_DB_USER", () -> "postgres");
        registro.add("NOVAPOP_DB_PASSWORD", () -> "postgres");
    }

    @AfterAll
    static void apagar() throws Exception {
        if (pg != null) {
            pg.close();
        }
    }

    // ------------------------------------------------------------------ helpers

    private String login(String correo) throws Exception {
        return leer(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"" + correo + "\",\"clave\":\"NovaPop2026!\"}"))
                .andExpect(status().isOk())).get("token").asText();
    }

    private MockHttpServletRequestBuilder conPerfil(MockHttpServletRequestBuilder req, String token, long perfil) {
        return req.header("Authorization", "Bearer " + token).header("X-Perfil-Id", perfil)
                .contentType(MediaType.APPLICATION_JSON);
    }

    private JsonNode leer(ResultActions r) throws Exception {
        String cuerpo = r.andReturn().getResponse().getContentAsString();
        return cuerpo.isEmpty() ? null : json.readTree(cuerpo);
    }

    private static List<Long> ids(JsonNode tarjetas) {
        List<Long> ids = new ArrayList<>();
        tarjetas.forEach(t -> ids.add(t.get("idContenido").asLong()));
        return ids;
    }

    // ------------------------------------------------------------------ pruebas

    @Test
    void esquemaTieneLas35TablasConAuditoria() {
        Integer tablas = jdbc.queryForObject("""
                select count(*) from information_schema.tables
                 where table_schema = 'novapop' and table_type = 'BASE TABLE'""", Integer.class);
        Integer sinAuditoria = jdbc.queryForObject("""
                select count(*) from information_schema.tables t
                 where t.table_schema = 'novapop' and t.table_type = 'BASE TABLE'
                   and (select count(*) from information_schema.columns c
                         where c.table_schema = t.table_schema and c.table_name = t.table_name
                           and c.column_name in ('usuario_creacion','fecha_creacion','ip_creacion','usuario_modificacion',
                                                 'fecha_modificacion','ip_modificacion','estado_registro')) <> 7
                """, Integer.class);
        assertThat(tablas).isEqualTo(35);
        assertThat(sinAuditoria).isZero();
    }

    @Test
    void triggerImpideEpisodioDeOtraSerie() {
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> jdbc.update("""
                insert into novapop.historial_reproduccion (id_perfil, id_contenido, id_episodio, segundo_visto)
                values (2, 1, 1, 10)"""));
    }

    @Test
    void loginIncorrectoDevuelve401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"demo@novapop.pe\",\"clave\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/perfiles")).andExpect(status().isUnauthorized());
    }

    @Test
    void inicioDePerfilAdulto() throws Exception {
        String token = login("demo@novapop.pe");
        JsonNode perfiles = leer(mvc.perform(get("/api/perfiles").header("Authorization", "Bearer " + token)));
        assertThat(perfiles).hasSize(3);

        JsonNode inicio = leer(mvc.perform(conPerfil(get("/api/catalogo/inicio"), token, 1)).andExpect(status().isOk()));
        assertThat(ids(inicio.get("destacados"))).containsExactly(8L, 7L, 4L);
        assertThat(inicio.get("filas").size()).isGreaterThanOrEqualTo(4);
        assertThat(inicio.get("seguirViendo").get(0).get("contenido").get("idContenido").asLong()).isEqualTo(1L);
        assertThat(inicio.get("seguirViendo").get(0).get("segundo").asInt()).isEqualTo(312);
        assertThat(ids(inicio.get("miLista"))).containsExactlyInAnyOrder(4L, 5L, 7L);
    }

    @Test
    void perfilInfantilSoloVeContenidoApto() throws Exception {
        String token = login("demo@novapop.pe");
        JsonNode todo = leer(mvc.perform(conPerfil(get("/api/catalogo/contenidos"), token, 3)).andExpect(status().isOk()));
        assertThat(ids(todo)).containsExactlyInAnyOrder(1L, 2L, 6L, 9L, 10L);

        mvc.perform(conPerfil(get("/api/catalogo/contenidos/7"), token, 3)).andExpect(status().isNotFound());

        JsonNode inicio = leer(mvc.perform(conPerfil(get("/api/catalogo/inicio"), token, 3)));
        JsonNode seguir = inicio.get("seguirViendo").get(0);
        assertThat(seguir.get("idEpisodio").asLong()).isEqualTo(2L); // Caminandes T1:E2 a medias
        assertThat(seguir.get("etiquetaEpisodio").asText()).startsWith("T1:E2");

        JsonNode serie = leer(mvc.perform(conPerfil(get("/api/catalogo/contenidos/10"), token, 3)));
        assertThat(serie.get("temporadas").get(0).get("episodios")).hasSize(3);
        assertThat(serie.get("temporadas").get(0).get("episodios").get(0).get("completado").asBoolean()).isTrue();
    }

    @Test
    void busquedaIgnoraTildesYFiltraPorGenero() throws Exception {
        String token = login("demo@novapop.pe");
        JsonNode r = leer(mvc.perform(conPerfil(get("/api/catalogo/contenidos").param("q", "lagrimas"), token, 2)));
        assertThat(ids(r)).containsExactly(4L);
        JsonNode terror = leer(mvc.perform(conPerfil(get("/api/catalogo/contenidos").param("genero", "8"), token, 2)));
        assertThat(ids(terror)).containsExactly(7L);
        JsonNode series = leer(mvc.perform(conPerfil(get("/api/catalogo/contenidos").param("tipo", "SERIE"), token, 2)));
        assertThat(ids(series)).containsExactly(10L);
    }

    @Test
    void perfilDeOtraCuentaEsRechazado() throws Exception {
        String token = login("demo@novapop.pe");
        mvc.perform(conPerfil(get("/api/catalogo/inicio"), token, 4)).andExpect(status().isForbidden());
    }

    @Test
    void detalleYMiListaYCalificacion() throws Exception {
        String token = login("demo@novapop.pe");
        JsonNode d = leer(mvc.perform(conPerfil(get("/api/catalogo/contenidos/3"), token, 2)).andExpect(status().isOk()));
        assertThat(d.get("titulo").asText()).isEqualTo("Sintel");
        assertThat(d.get("urlTrailer").asText()).contains("youtube.com");
        assertThat(d.get("totalCalificaciones").asLong()).isEqualTo(3);
        assertThat(d.get("subtitulos").toString()).contains("Español");

        mvc.perform(conPerfil(put("/api/catalogo/mi-lista/3"), token, 2)).andExpect(status().isNoContent());
        mvc.perform(conPerfil(put("/api/catalogo/calificaciones/3"), token, 2).content("{\"valoracion\":5}"))
                .andExpect(status().isNoContent());
        JsonNode d2 = leer(mvc.perform(conPerfil(get("/api/catalogo/contenidos/3"), token, 2)));
        assertThat(d2.get("enMiLista").asBoolean()).isTrue();
        assertThat(d2.get("miCalificacion").asInt()).isEqualTo(5);

        mvc.perform(conPerfil(delete("/api/catalogo/mi-lista/3"), token, 2)).andExpect(status().isNoContent());
        mvc.perform(conPerfil(put("/api/catalogo/calificaciones/3"), token, 2).content("{\"valoracion\":9}"))
                .andExpect(status().isBadRequest());

        String auditor = jdbc.queryForObject(
                "select usuario_creacion from novapop.calificacion_contenido where id_perfil = 2 and id_contenido = 3",
                String.class);
        assertThat(auditor).isEqualTo("SISTEMA"); // ya existía en los datos de prueba
        String modificador = jdbc.queryForObject(
                "select usuario_modificacion from novapop.calificacion_contenido where id_perfil = 2 and id_contenido = 3",
                String.class);
        assertThat(modificador).isEqualTo("demo@novapop.pe");
    }

    @Test
    void reproduccionRespetaCalidadDelPlanYGuardaProgreso() throws Exception {
        String token = login("demo@novapop.pe");
        JsonNode r = leer(mvc.perform(conPerfil(post("/api/reproduccion/iniciar"), token, 2)
                .content("{\"idContenido\":1,\"idDispositivo\":2}")).andExpect(status().isOk()));
        assertThat(r.get("fuentes")).hasSize(5); // Platino: hasta 4K
        assertThat(r.get("fuentes").get(0).get("calidad").asText()).isEqualTo("4K Ultra HD");
        assertThat(r.get("subtitulos").size()).isEqualTo(4);
        long sesion = r.get("idSesion").asLong();

        mvc.perform(conPerfil(put("/api/reproduccion/" + sesion + "/progreso"), token, 2).content("{\"segundo\":120}"))
                .andExpect(status().isNoContent());
        mvc.perform(conPerfil(post("/api/reproduccion/" + sesion + "/finalizar"), token, 2).content("{\"segundo\":630}"))
                .andExpect(status().isNoContent());
        Boolean completado = jdbc.queryForObject("""
                select completado from novapop.historial_reproduccion
                 where id_perfil = 2 and id_contenido = 1 and id_episodio is null""", Boolean.class);
        assertThat(completado).isTrue();

        // Serie sin episodio indicado: retoma donde corresponde
        JsonNode serie = leer(mvc.perform(conPerfil(post("/api/reproduccion/iniciar"), token, 3)
                .content("{\"idContenido\":10,\"idDispositivo\":2}")).andExpect(status().isOk()));
        assertThat(serie.get("idEpisodio").asLong()).isEqualTo(2L);
        assertThat(serie.get("segundoInicial").asInt()).isEqualTo(74);
        assertThat(serie.get("siguiente").get("idEpisodio").asLong()).isEqualTo(3L);
    }

    @Test
    void planBasicoLimitaCalidadPantallasYPais() throws Exception {
        String token = login("basico@novapop.pe");
        JsonNode r = leer(mvc.perform(conPerfil(post("/api/reproduccion/iniciar"), token, 4)
                .content("{\"idContenido\":4,\"idDispositivo\":3}")).andExpect(status().isOk()));
        assertThat(r.get("calidadMaximaPlan").asText()).isEqualTo("SD 480p");
        assertThat(r.get("fuentes")).hasSize(2);

        JsonNode tv = leer(mvc.perform(post("/api/cuenta/dispositivos").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"SMART_TV\",\"nombre\":\"TV del cuarto\"}"))
                .andExpect(status().isCreated()));
        mvc.perform(conPerfil(post("/api/reproduccion/iniciar"), token, 4)
                        .content("{\"idContenido\":3,\"idDispositivo\":" + tv.get("id").asLong() + "}"))
                .andExpect(status().isConflict()); // 1 pantalla simultánea

        // Charge aún no se estrena en México
        mvc.perform(conPerfil(get("/api/catalogo/contenidos/8"), token, 4)).andExpect(status().isNotFound());
        JsonNode inicio = leer(mvc.perform(conPerfil(get("/api/catalogo/inicio"), token, 4)));
        assertThat(inicio.get("proximamente").get(0).get("contenido").get("idContenido").asLong()).isEqualTo(8L);
    }

    @Test
    void registroCreaCuentaSuscripcionPagoYPerfil() throws Exception {
        JsonNode t = leer(mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content("""
                        {"correo":"nueva@correo.com","clave":"secreta123","idPais":1,"idPlan":2,
                         "nombrePerfil":"Yo","tipoMetodo":"TARJETA_CREDITO","numeroPago":"4111111111111111"}"""))
                .andExpect(status().isCreated()));
        String token = t.get("token").asText();
        JsonNode cuenta = leer(mvc.perform(get("/api/cuenta").header("Authorization", "Bearer " + token)));
        assertThat(cuenta.get("suscripcion").get("plan").get("nombre").asText()).isEqualTo("Estándar");
        JsonNode metodos = leer(mvc.perform(get("/api/cuenta/metodos-pago").header("Authorization", "Bearer " + token)));
        assertThat(metodos.get(0).get("numeroEnmascarado").asText()).isEqualTo("**** **** **** 1111");

        // Cambio de plan: cancela la suscripción anterior y registra un nuevo pago
        JsonNode cambio = leer(mvc.perform(put("/api/cuenta/plan").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"idPlan\":3}")).andExpect(status().isOk()));
        assertThat(cambio.get("suscripcion").get("plan").get("nombre").asText()).isEqualTo("Platino");
        JsonNode pagos = leer(mvc.perform(get("/api/cuenta/pagos").header("Authorization", "Bearer " + token)));
        assertThat(pagos).hasSize(2);

        String creador = jdbc.queryForObject(
                "select usuario_creacion from novapop.suscripcion where id_plan = 3 and id_cuenta = "
                        + "(select id_cuenta from novapop.cuenta where correo = 'nueva@correo.com')", String.class);
        assertThat(creador).isEqualTo("nueva@correo.com");

        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content("""
                        {"correo":"nueva@correo.com","clave":"secreta123","idPais":1,"idPlan":2,
                         "nombrePerfil":"Yo","tipoMetodo":"YAPE","numeroPago":"987654321"}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void perfilesConPin() throws Exception {
        String token = login("demo@novapop.pe");
        mvc.perform(post("/api/perfiles/1/pin").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"pin\":\"0000\"}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/perfiles/1/pin").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"pin\":\"1234\"}")).andExpect(status().isNoContent());
        mvc.perform(post("/api/perfiles").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"ana\",\"esInfantil\":false}"))
                .andExpect(status().isConflict()); // nombre repetido (sin distinguir mayúsculas)
    }

    @Test
    void tituloSoloTrailerSeMuestraPeroNoSeReproduce() throws Exception {
        jdbc.update("""
                insert into novapop.contenido (id_contenido, id_clasificacion, id_idioma_original, tipo_contenido,
                       titulo_original, anio_lanzamiento, sinopsis)
                values (900, 3, 2, 'PELICULA', 'Pelicula Con Derechos', 2019, 'Solo tráiler');
                insert into novapop.pelicula (id_contenido, id_video, duracion_minutos, url_trailer)
                values (900, null, 120, 'https://www.youtube.com/watch?v=YE7VzlLtp-4');
                insert into novapop.contenido_pais (id_contenido, id_pais, fecha_disponible_desde) values (900, 1, '2024-01-01');
                """);
        String token = login("demo@novapop.pe");
        JsonNode r = leer(mvc.perform(conPerfil(get("/api/catalogo/contenidos").param("q", "derechos"), token, 2)));
        assertThat(r.get(0).get("soloTrailer").asBoolean()).isTrue();
        assertThat(r.get(0).get("urlTrailer").asText()).contains("youtube");

        JsonNode d = leer(mvc.perform(conPerfil(get("/api/catalogo/contenidos/900"), token, 2)).andExpect(status().isOk()));
        assertThat(d.get("disponibleParaVer").asBoolean()).isFalse();
        assertThat(d.get("duracionMinutos").asInt()).isEqualTo(120);

        mvc.perform(conPerfil(post("/api/reproduccion/iniciar"), token, 2)
                        .content("{\"idContenido\":900,\"idDispositivo\":2}"))
                .andExpect(status().isConflict());
        // Las películas de Blender siguen siendo reproducibles
        JsonNode bbb = leer(mvc.perform(conPerfil(get("/api/catalogo/contenidos/1"), token, 2)));
        assertThat(bbb.get("disponibleParaVer").asBoolean()).isTrue();
    }

    // ------------------------------------------------------------------ panel de administración

    private String loginStaff(String usuario) throws Exception {
        return leer(mvc.perform(post("/api/admin/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuario\":\"" + usuario + "\",\"clave\":\"Admin2026!\"}"))
                .andExpect(status().isOk())).get("token").asText();
    }

    private MockHttpServletRequestBuilder conToken(MockHttpServletRequestBuilder req, String token) {
        return req.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void panelSeparaPersonalDeClientes() throws Exception {
        String admin = loginStaff("admin");
        String cliente = login("demo@novapop.pe");
        mvc.perform(conToken(get("/api/admin/generos"), cliente)).andExpect(status().isForbidden());
        mvc.perform(conPerfil(get("/api/catalogo/inicio"), admin, 1)).andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuario\":\"admin\",\"clave\":\"mala\"}")).andExpect(status().isUnauthorized());

        JsonNode recursos = leer(mvc.perform(conToken(get("/api/admin/recursos"), admin)));
        assertThat(recursos).hasSize(15);
        // El editor no ve el módulo de Seguridad
        String editor = loginStaff("editor");
        assertThat(leer(mvc.perform(conToken(get("/api/admin/recursos"), editor)))).hasSize(13);
        mvc.perform(conToken(get("/api/admin/usuarios"), editor)).andExpect(status().isForbidden());
    }

    @Test
    void crudCompletoDeUnaTabla() throws Exception {
        String admin = loginStaff("admin");
        JsonNode creado = leer(mvc.perform(conToken(post("/api/admin/generos"), admin)
                .content("{\"nombreGenero\":\"Anime\"}")).andExpect(status().isCreated()));
        long id = creado.get("id").asLong();
        assertThat(creado.get("usuarioCreacion").asText()).isEqualTo("admin"); // auditoría

        JsonNode lista = leer(mvc.perform(conToken(get("/api/admin/generos").param("q", "anime"), admin)));
        assertThat(lista.get("total").asInt()).isEqualTo(1);

        JsonNode editado = leer(mvc.perform(conToken(put("/api/admin/generos/" + id), admin)
                .content("{\"nombreGenero\":\"Animé japonés\"}")).andExpect(status().isOk()));
        assertThat(editado.get("nombreGenero").asText()).isEqualTo("Animé japonés");
        assertThat(editado.get("usuarioModificacion").asText()).isEqualTo("admin");

        mvc.perform(conToken(post("/api/admin/generos"), admin).content("{\"nombreGenero\":\"Drama\"}"))
                .andExpect(status().isConflict()); // nombre único
        mvc.perform(conToken(post("/api/admin/generos"), admin).content("{}"))
                .andExpect(status().isBadRequest()); // obligatorio

        mvc.perform(conToken(delete("/api/admin/generos/" + id), admin)).andExpect(status().isNoContent());
        assertThat(leer(mvc.perform(conToken(get("/api/admin/generos/" + id), admin))).get("estadoRegistro").asText())
                .isEqualTo("I");
        mvc.perform(conToken(put("/api/admin/generos/" + id + "/reactivar"), admin)).andExpect(status().isOk());
    }

    @Test
    void peliculaCreadaDesdeElPanelApareceEnElCatalogo() throws Exception {
        String admin = loginStaff("admin");
        JsonNode p = leer(mvc.perform(conToken(post("/api/admin/contenidos"), admin).content("""
                        {"tipoContenido":"PELICULA","tituloOriginal":"Pelicula Del Panel","anioLanzamiento":2025,
                         "clasificacion":3,"idiomaOriginal":1,"duracionMinutos":101,
                         "urlTrailer":"https://www.youtube.com/watch?v=YE7VzlLtp-4"}"""))
                .andExpect(status().isCreated()));
        assertThat(p.get("duracionMinutos").asInt()).isEqualTo(101);
        assertThat(p.get("clasificacion").get("etiqueta").asText()).isEqualTo("+13");

        String cliente = login("demo@novapop.pe");
        JsonNode r = leer(mvc.perform(conPerfil(get("/api/catalogo/contenidos").param("q", "del panel"), cliente, 2)));
        assertThat(r.get(0).get("soloTrailer").asBoolean()).isTrue();
        assertThat(r.get(0).get("duracionMinutos").asInt()).isEqualTo(101);

        // Película sin duración: la tabla PELICULA la exige
        mvc.perform(conToken(post("/api/admin/contenidos"), admin).content("""
                        {"tipoContenido":"PELICULA","tituloOriginal":"Sin duracion","anioLanzamiento":2025,
                         "clasificacion":1,"idiomaOriginal":1}"""))
                .andExpect(status().isBadRequest());
        // Usuario del sistema sin contraseña
        mvc.perform(conToken(post("/api/admin/usuarios"), admin)
                        .content("{\"nombreUsuario\":\"nuevo\",\"nombres\":\"N\",\"correo\":\"n@n.pe\",\"rol\":2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publicoSinToken() throws Exception {
        JsonNode planes = leer(mvc.perform(get("/api/publico/planes")).andExpect(status().isOk()));
        assertThat(planes).hasSize(3);
        mvc.perform(get("/")).andExpect(status().isOk());
    }
}
