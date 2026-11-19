# Manual del programador — NovaPop

Guía técnica para instalar, entender y modificar NovaPop.

## 1. Qué es y con qué está hecho

NovaPop es una plataforma de streaming académica. El backend sirve una API REST y las páginas estáticas del frontend.

| Capa | Tecnología |
|---|---|
| Backend | **Java 21** + **Spring Boot 3.5** (web, data-jpa, security, oauth2-resource-server, validation) |
| Base de datos | **PostgreSQL** en **Supabase** (35 tablas, 3FN, 7 campos de auditoría por tabla) |
| Frontend | HTML, CSS y JavaScript puro (sin framework), servidos por Spring desde `resources/static` |
| Build / pruebas | **Maven**; JUnit + MockMvc + PostgreSQL embebido; **JaCoCo** para cobertura |
| Pruebas E2E / calidad | **Playwright** y **Lighthouse** (carpeta `pruebas/`) |
| Despliegue | **Render** (el backend) + **Supabase** (la base) |

## 2. Estructura del proyecto

```
StreamingProyecto/
├── backend/                  aplicación Spring Boot (Maven)
│   ├── src/main/java/com/novapop/
│   │   ├── NovaPopApplication.java   arranque; si no hay .env, levanta PostgreSQL embebido (BaseDatosLocal)
│   │   ├── controlador/      API REST para clientes (catálogo, cuenta, perfiles, reproducción)
│   │   ├── admin/            API y lógica del panel de administración (CRUD genérico + respaldo)
│   │   ├── servicio/         lógica de negocio (Auth, Catalogo, Cuenta, Perfil, Reproduccion, Tmdb, Respaldo…)
│   │   ├── repositorio/      interfaces Spring Data JPA
│   │   ├── entidad/          entidades JPA (una por tabla)
│   │   ├── dto/              objetos de entrada/salida de la API
│   │   ├── seguridad/        Spring Security + JWT
│   │   ├── auditoria/        rellena los campos usuario/fecha/ip de creación y modificación
│   │   └── excepcion/        manejo uniforme de errores (JSON {estado, mensaje, fecha})
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── static/           frontend (HTML, css/, js/, img/, manifest.json, sw.js)
│   └── src/test/java/        pruebas de caja blanca (JUnit)
├── database/                 scripts SQL (01 a 07) — ver sección 5
├── pruebas/                  Playwright + Lighthouse (caja negra y rendimiento)
├── docs/                     manuales y revisión de seguridad
├── render.yaml               configuración del despliegue en Render
└── Dockerfile
```

## 3. Instalación en local

**Requisitos:** Java 21 (JDK) y Maven. (Opcional: Node.js para las pruebas de `pruebas/`.)

```bash
cd backend
mvn spring-boot:run
```

Luego abre http://localhost:8080.

- **Sin configuración** (sin `backend/.env`): arranca con un **PostgreSQL embebido** y los datos de prueba ya cargados.
  Ideal para probar en cualquier PC con solo "Run".
- **Con Supabase:** crea `backend/.env` a partir de `backend/.env.example` con las credenciales reales.

> En Windows con un antivirus que intercepta HTTPS, Java puede fallar al conectar (TMDB, Maven) con
> `PKIX path building failed`. Añade `-Djavax.net.ssl.trustStoreType=Windows-ROOT` (en `MAVEN_OPTS` o en los
> argumentos de arranque) para que use los certificados de Windows.

### Variables de entorno

| Variable | Para qué |
|---|---|
| `NOVAPOP_DB_URL`, `NOVAPOP_DB_USER`, `NOVAPOP_DB_PASSWORD` | Conexión a Supabase |
| `NOVAPOP_JWT_SECRET` | Clave para firmar los tokens (mínimo 32 caracteres) |
| `NOVAPOP_GOOGLE_CLIENT_ID` | "Continuar con Google" (vacío = el botón no se muestra) |
| `NOVAPOP_TMDB_KEY` | Filmografía de actores y el importador de catálogo (opcional) |

## 4. Seguridad

Configurada en `seguridad/SecurityConfig.java`:

- **Autenticación por JWT** firmado con HS256 (`NOVAPOP_JWT_SECRET`). El token viaja en `Authorization: Bearer ...`.
  La API es **stateless** (sin sesiones ni cookies); por eso CSRF está desactivado.
- **Permisos** por el claim `permisos` del token:
  - `/api/auth/**`, `/api/publico/**`, `GET /api/subtitulos/**` → públicos.
  - `/api/admin/**` → requiere `STAFF` (tabla USUARIO). Dentro del panel, el módulo de Seguridad y el respaldo exigen
    el rol `ADMINISTRADOR`.
  - resto de `/api/**` → requiere `CLIENTE` (tabla CUENTA).
- **Contraseñas** con BCrypt. **Login con Google** validado en el servidor contra Google (firma, `aud`, correo verificado).
- **Cabeceras** de seguridad: `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, `Permissions-Policy`.
- Revisión completa y pendientes: [`pentesting.md`](pentesting.md).

## 5. Base de datos

El esquema `novapop` tiene **35 tablas** en 3FN, cada una con 7 campos de auditoría
(`usuario_/fecha_/ip_` de creación y modificación, más `estado_registro` para el borrado lógico). Scripts en `database/`:

| Script | Contenido |
|---|---|
| `01_esquema.sql` | Las 35 tablas, índices, triggers y la auditoría |
| `02_datos_prueba.sql` | Catálogo de Blender, planes, cuentas demo, historial… |
| `03_migracion_solo_trailer.sql` | Permite títulos sin video (solo tráiler) |
| `04_catalogo_tmdb.sql` | Catálogo importado de TMDB (Marvel, Disney, DC, series) |
| `05_administracion.sql` | Usuarios y roles del panel |
| `06_catalogo_libre.sql` | Clásicos de dominio público y más cortos de Blender |
| `07_posters_verticales.sql` | Columna de póster vertical (2:3) |

Orden para una base nueva en Supabase: 01 → 02 → 03 → 04 → 05 → 06 → 07.
El importador `database/tmdb/importar-tmdb.mjs` regenera el script 04 desde TMDB.

## 6. API REST (resumen)

Todas devuelven JSON. Las privadas requieren `Authorization: Bearer <token>`; las de catálogo, además, la cabecera
`X-Perfil-Id`.

**Autenticación** `/api/auth`
- `POST /registro`, `POST /login`, `POST /google`

**Público** `/api/publico` — `GET /config`, `/paises`, `/idiomas`, `/planes`, `/posters`

**Catálogo** `/api/catalogo` (cliente + `X-Perfil-Id`)
- `GET /inicio`, `/generos`, `/contenidos`, `/contenidos/{id}`, `/personas/{id}`, `/mi-lista`
- `PUT|DELETE /mi-lista/{id}`, `PUT|DELETE /calificaciones/{id}`

**Cuenta** `/api/cuenta` (cliente)
- `GET /` · `GET /mis-datos` · `PUT /plan` · `PUT /renovacion`
- `GET /pagos` · `GET|POST /metodos-pago` · `PUT /metodos-pago/{id}/predeterminado` · `DELETE /metodos-pago/{id}`
- `GET|POST /dispositivos` · `DELETE /dispositivos/{id}`

**Perfiles** `/api/perfiles` (cliente) — `GET|POST /` · `PUT|DELETE /{id}` · `POST /{id}/pin`

**Reproducción** `/api/reproduccion` — `POST /iniciar` · `PUT /{sesion}/progreso` · `POST /{sesion}/finalizar`

**Panel** `/api/admin` (STAFF)
- `POST /auth/login` · `GET /recursos` · `GET /resumen` · `GET /respaldo` (solo ADMINISTRADOR)
- CRUD genérico: `GET /{recurso}`, `/{recurso}/{id}`, `/{recurso}/opciones`; `POST /{recurso}`;
  `PUT /{recurso}/{id}`; `DELETE /{recurso}/{id}`; `PUT /{recurso}/{id}/reactivar`

El panel es **genérico**: se arma solo a partir de `/api/admin/recursos`, que describe cada tabla fuerte y sus campos
(ver `admin/Recursos.java`). Para exponer una tabla nueva en el panel, se registra ahí.

## 7. Pruebas y calidad

| Qué | Cómo | Dónde |
|---|---|---|
| Caja blanca (JUnit) | `cd backend && mvn test` | `backend/src/test` |
| Cobertura (JaCoCo) | tras `mvn test` → `target/site/jacoco/index.html` | ~77 % de instrucciones |
| Caja negra (E2E) | `cd pruebas && npm test` | `pruebas/e2e` |
| Rendimiento (Lighthouse) | `cd pruebas && npm run lighthouse` | `pruebas/reportes/lighthouse` |
| Seguridad (pentesting) | revisión documentada | [`pentesting.md`](pentesting.md) |

Detalle de las pruebas E2E y Lighthouse: [`pruebas/README.md`](../pruebas/README.md).

## 8. Despliegue

- **Base de datos:** se ejecutan los scripts SQL en el editor de Supabase (una sola vez).
- **Backend:** Render construye desde el repositorio (`render.yaml` + `Dockerfile`) y despliega en cada `push` a `main`.
  Las variables de entorno (sección 3) se configuran en el panel de Render.
- Solo se despliega la base y el backend; el frontend va dentro del backend como archivos estáticos.

## 9. Copias de seguridad

- **Toda la base:** panel → tablero → *Descargar respaldo* (rol ADMINISTRADOR) genera un `.sql` restaurable.
- **Datos de un usuario:** *Mi cuenta* → *Descargar mis datos* genera un JSON.

Detalle y restauración: sección "Copias de seguridad" del [README](../README.md).

## 10. PWA

`manifest.json` y `sw.js` (service worker) en `resources/static` permiten instalar NovaPop y mostrar una página sin
conexión (`offline.html`). El service worker se registra desde `js/tema.js`. Estrategia de caché: las páginas, el CSS y
el JS van "primero la red" (así cada despliegue se ve al instante); las imágenes, "primero la caché"; la API nunca se guarda.
