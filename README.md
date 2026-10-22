# 🍿 NovaPop

Plataforma académica de streaming de películas y series, construida sobre el modelo entidad-relación de 35 tablas (3FN).

- **Base de datos:** PostgreSQL en Supabase (esquema `novapop`)
- **Backend:** Java 21 + Spring Boot 3.5 (JPA, Security con JWT, Validation)
- **Frontend:** HTML, CSS y JavaScript (módulos ES), servido por el mismo Spring Boot
- **Panel de administración:** Bootstrap 5.3 + Bootstrap Icons, con CRUD de las 15 tablas fuertes
- **Tema claro / oscuro** en el sitio y en el panel (botón ☀️/🌙)
- **Despliegue:** Docker + Render (`Dockerfile` y `render.yaml`)
- **Contenido:** películas abiertas de Blender (Creative Commons), alojadas en Wikimedia Commons

```
StreamingProyecto/
├── database/
│   ├── 01_esquema.sql               35 tablas, índices, triggers y auditoría
│   ├── 02_datos_prueba.sql          catálogo, planes, cuentas demo, historial…
│   ├── 03_migracion_solo_trailer.sql títulos sin video (solo tráiler)
│   ├── 04_catalogo_tmdb.sql         catálogo importado con la API key de TMDB
│   ├── 05_administracion.sql        contraseñas del personal (panel /admin)
│   └── tmdb/                        importador (API key → PERSONA, PRODUCTORA, CONTENIDO)
└── backend/
    ├── pom.xml
    ├── .env.example          plantilla de credenciales de Supabase
    └── src/main/
        ├── java/com/novapop/
        │   ├── entidad/       35 entidades JPA + enums + EntidadAuditable
        │   ├── repositorio/   35 repositorios Spring Data
        │   ├── servicio/      lógica de negocio
        │   ├── controlador/   API REST
        │   ├── dto/           objetos de entrada/salida
        │   ├── seguridad/     JWT y configuración de Spring Security
        │   ├── admin/         panel de administración: CRUD genérico de las tablas fuertes
        │   ├── auditoria/     listener que llena los campos de auditoría
        │   └── excepcion/     errores y respuestas JSON uniformes
        └── resources/static/  frontend (index, login, registro, perfiles, inicio, titulo, ver, cuenta)
                               y admin/ (login y panel con Bootstrap)
```

## 1. Crear la base de datos en Supabase

1. Crea un proyecto en [supabase.com](https://supabase.com) y guarda la contraseña de la base de datos.
2. Ve a **SQL Editor → New query**, pega el contenido de `database/01_esquema.sql` y ejecútalo (**Run**).
3. En otra consulta, pega `database/02_datos_prueba.sql` y ejecútalo.
4. Ejecuta también `database/03_migracion_solo_trailer.sql` (permite títulos sin video, solo con tráiler).
5. Ejecuta `database/04_catalogo_tmdb.sql` (catálogo de Marvel, Disney, DC, series…).
6. Ejecuta `database/05_administracion.sql` (acceso al panel de administración).
7. Comprueba que existen las tablas: en **Table Editor** cambia el esquema a `novapop`.

### Catálogo con derechos de autor (Marvel, Disney, DC, series…)

Las películas de Blender se reproducen completas. Los títulos comerciales se cargan como fichas **"Solo tráiler"**:
datos, póster, elenco y tráiler oficial de YouTube, sin el video completo.

1. Crea una cuenta gratis en [themoviedb.org](https://www.themoviedb.org) → *Configuración → API* y copia tu clave.
2. Pégala en `backend/.env` en la línea `NOVAPOP_TMDB_KEY=`.
3. Edita `database/tmdb/titulos.json` si quieres otros títulos y ejecuta:
   ```bash
   node database/tmdb/importar-tmdb.mjs
   ```
4. Ejecuta el `database/04_catalogo_tmdb.sql` generado en el SQL Editor de Supabase.

> Las tablas van en el esquema `novapop` (no en `public`) para que **no** queden expuestas en la Data API de Supabase.
> Solo el backend accede a ellas, por JDBC.

## 2. Configurar las credenciales

1. En Supabase: **Connect** (o *Project Settings → Database*) → **Connection string → JDBC → Session pooler**.
   Usa el *Session pooler*: la conexión directa usa IPv6 y muchas redes no la soportan.
2. Copia `backend/.env.example` como `backend/.env` y completa:

```properties
NOVAPOP_DB_URL=jdbc:postgresql://aws-0-REGION.pooler.supabase.com:5432/postgres?sslmode=require
NOVAPOP_DB_USER=postgres.TU_PROJECT_REF
NOVAPOP_DB_PASSWORD=tu-clave
NOVAPOP_JWT_SECRET=una-clave-larga-y-aleatoria-de-al-menos-32-caracteres
```

`.env` está en `.gitignore`: **nunca subas tus credenciales a GitHub**.

## 3. Ejecutar

Requisitos: JDK 21 o superior y Maven (o VS Code con el *Extension Pack for Java*).

- **Desde VS Code:** abre la carpeta del proyecto y pulsa **Run** sobre `backend/src/main/java/com/novapop/NovaPopApplication.java`.
- **Desde la terminal:** `cd backend` y `mvn spring-boot:run`.

Abre <http://localhost:8080>.

| ¿Existe `backend/.env` con las credenciales? | Base de datos que usa |
|---|---|
| Sí | La de **Supabase** |
| No | **Modo local**: crea automáticamente una base PostgreSQL temporal en la computadora con los 4 scripts de `database/`. No requiere configurar nada. |

En modo local cada arranque parte de los datos de prueba originales.

### Cuentas demo (clave `NovaPop2026!`)

| Correo | País | Plan | Notas |
|---|---|---|---|
| `demo@novapop.pe` | Perú | Platino (4K, 4 pantallas) | Perfiles Ana (PIN **1234**), Luis y Peques (infantil) |
| `basico@novapop.pe` | México | Básico (480p, 1 pantalla) | *Charge* aparece como "Próximamente" |

### Pruebas automáticas

```bash
cd backend
mvn test
```

Ejecuta los dos scripts SQL sobre PostgreSQL 17 embebido, valida que las entidades coincidan con las tablas
(`ddl-auto=validate`) y recorre la API: login, catálogo por país y edad, búsqueda, Mi lista, calificaciones,
reproducción, límite de pantallas, calidad por plan, registro, cambio de plan y auditoría.

## Panel de administración

Disponible en **/admin** (por ejemplo <http://localhost:8080/admin>). Lo usa el personal de la plataforma (tabla `USUARIO`),
separado de las cuentas de clientes. Clave de los tres usuarios de prueba: **`Admin2026!`**

| Usuario | Rol | Acceso |
|---|---|---|
| `admin` | ADMINISTRADOR | Todo, incluido el módulo de Seguridad (roles y usuarios) |
| `editor` | EDITOR_CONTENIDO | Todo menos Seguridad |
| `soporte` | SOPORTE | Todo menos Seguridad |

CRUD de las **15 tablas fuertes**, agrupadas por módulo del modelo:

| Módulo | Tablas |
|---|---|
| Seguridad | ROL, USUARIO |
| Catálogos generales | PAIS, IDIOMA, GENERO, CLASIFICACION_EDAD |
| Catálogo de contenido | CONTENIDO (+ datos de PELICULA), COLECCION |
| Elenco y productoras | PERSONA, ROL_PRODUCCION, PRODUCTORA |
| Video y planes | PERFIL_CALIDAD, VIDEO, PLAN_SUSCRIPCION |
| Clientes | CUENTA |

Cada tabla tiene búsqueda, paginación, alta, edición, eliminación lógica (`estado_registro = I`) y reactivación.
Las tablas se describen una sola vez en `admin/Recursos.java` y un único servicio (`AdminCrudService`) hace el CRUD
de todas; los formularios de Bootstrap se generan a partir de esa descripción. Las claves foráneas se eligen en listas
desplegables, y la auditoría registra qué usuario del personal creó o modificó cada registro.

**API key (TMDB) en PERSONA y PRODUCTORA:** `database/tmdb/importar-tmdb.mjs` usa la API key de TMDB para traer el
elenco (nombres, fotos, fechas de nacimiento) y las productoras de cada título, y genera `04_catalogo_tmdb.sql`.

## Despliegue en Render

1. Sube el proyecto a GitHub (el `.env` está en `.gitignore` y no se sube).
2. En [render.com](https://render.com): **New → Blueprint** → elige el repositorio. Render lee `render.yaml`.
3. Completa las variables `NOVAPOP_DB_URL`, `NOVAPOP_DB_USER` y `NOVAPOP_DB_PASSWORD` con los datos de Supabase
   (`NOVAPOP_JWT_SECRET` la genera Render).
4. Render compila con el `Dockerfile` y publica la app en `https://<nombre>.onrender.com`.

En el plan gratuito el servicio se suspende tras ~15 minutos sin visitas; la siguiente visita tarda alrededor de 1 minuto en despertarlo.

## Funcionalidades

| Módulo del ERD | Qué hace la aplicación |
|---|---|
| Suscripciones y facturación | Registro con plan y método de pago (simulado), cambio de plan, renovación automática, historial de pagos, métodos de pago enmascarados |
| Perfiles | Hasta 5 perfiles por cuenta, avatares, PIN de control parental, perfiles infantiles (solo ATP y +7) |
| Catálogo | Vitrina con destacados, Top 10, colecciones, búsqueda sin tildes y filtro por género |
| Disponibilidad por país | Solo se muestra lo que tiene licencia vigente en el país de la cuenta; "Próximamente" y "Disponible hasta" |
| Video y streaming adaptativo | Calidades de 240p a 4K limitadas por el plan, subtítulos en varios idiomas, pistas de audio |
| Reproducción y consumo | "Seguir viendo", siguiente episodio automático, historial, límite de pantallas simultáneas, dispositivos |
| Elenco y productoras | Créditos con rol y personaje, productoras de cada título |

### Campos de auditoría (los 7 de cada tabla)

| Campo | Cómo se llena |
|---|---|
| `usuario_creacion` / `usuario_modificacion` | Correo de la cuenta autenticada (o `SISTEMA`) — `AuditoriaListener` |
| `fecha_creacion` / `fecha_modificacion` | Fecha y hora del servidor; un trigger sella además `fecha_modificacion` en cada UPDATE |
| `ip_creacion` / `ip_modificacion` | IP de la petición HTTP |
| `estado_registro` | `A` activo / `I` inactivo (borrado lógico) |

### Reglas implementadas en la base de datos

- `CHECK` en todos los campos con valores fijos (tipo de contenido, situación, valoración 1-5, etc.).
- Índices únicos parciales: una sola suscripción activa por cuenta, un método de pago predeterminado, un género principal por contenido.
- Triggers: `PELICULA` solo para contenidos tipo película y `TEMPORADA` solo para series; en sesión e historial, el episodio debe pertenecer al contenido indicado; los campos de creación no se pueden alterar.

## Principales endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/registro` · `/api/auth/login` | Crear cuenta / iniciar sesión (devuelve JWT) |
| GET | `/api/publico/planes` · `/paises` · `/idiomas` | Datos públicos |
| GET/POST/PUT/DELETE | `/api/perfiles` | Perfiles; `POST /api/perfiles/{id}/pin` verifica el PIN |
| GET | `/api/catalogo/inicio` | Vitrina del perfil (cabecera `X-Perfil-Id`) |
| GET | `/api/catalogo/contenidos?tipo=&genero=&q=` | Explorar y buscar |
| GET | `/api/catalogo/contenidos/{id}` | Detalle de un título |
| PUT/DELETE | `/api/catalogo/mi-lista/{id}` · `/api/catalogo/calificaciones/{id}` | Mi lista y calificaciones |
| POST | `/api/reproduccion/iniciar` | Abre una sesión de reproducción y devuelve las fuentes de video |
| PUT/POST | `/api/reproduccion/{sesion}/progreso` · `/finalizar` | Guarda el progreso |
| GET | `/api/subtitulos/{id}` | Subtítulos en WebVTT |
| GET/PUT/POST/DELETE | `/api/cuenta`, `/plan`, `/pagos`, `/metodos-pago`, `/dispositivos` | Gestión de la cuenta |

## Créditos y licencias

Películas y series © Blender Foundation / Blender Studio, bajo licencia Creative Commons (CC BY),
obtenidas de [Wikimedia Commons](https://commons.wikimedia.org). Tráileres embebidos desde los canales oficiales de YouTube.
NovaPop es un proyecto académico sin fines de lucro.
