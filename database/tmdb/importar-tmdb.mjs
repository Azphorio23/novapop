// =====================================================================
//  NovaPop — Importador de catálogo desde TMDB (The Movie Database)
//
//  Lee la lista de títulos de titulos.json, consulta TMDB en español
//  latino y genera ../04_catalogo_tmdb.sql con: póster, sinopsis,
//  clasificación, géneros, productoras, elenco (con foto), tráiler oficial
//  de YouTube y, en series, todas las temporadas y episodios.
//
//  Estos títulos tienen derechos de autor: se cargan SIN video
//  (pelicula.id_video / episodio.id_video = NULL) → "Solo tráiler".
//
//  Uso:   node database/tmdb/importar-tmdb.mjs
//  Clave: variable NOVAPOP_TMDB_KEY (o en backend/.env). Sirve la
//         "API Key" (v3) o el "API Read Access Token" (v4).
// =====================================================================
import { readFileSync, writeFileSync, existsSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const AQUI = dirname(fileURLToPath(import.meta.url));
const SALIDA = join(AQUI, '..', '04_catalogo_tmdb.sql');
const IDIOMA = 'es-MX';
const IMG = 'https://image.tmdb.org/t/p/';

// IDs reservados para lo importado: así el script se puede volver a ejecutar sin duplicar
const ID_CONTENIDO = 1000, ID_PERSONA = 1000, ID_TEMPORADA = 1000, ID_EPISODIO = 10000, ID_COLECCION = 100;
const PAISES_DISPONIBLES = ['PE', 'MX', 'AR', 'CO', 'CL', 'EC', 'BO', 'ES', 'US'];

// ---------------------------------------------------------------- clave
function leerClave() {
  if (process.env.NOVAPOP_TMDB_KEY) return process.env.NOVAPOP_TMDB_KEY.trim();
  const env = join(AQUI, '..', '..', 'backend', '.env');
  if (existsSync(env)) {
    const m = readFileSync(env, 'utf8').match(/^NOVAPOP_TMDB_KEY=(.+)$/m);
    if (m && m[1].trim()) return m[1].trim();
  }
  console.error('Falta la clave de TMDB: agrega NOVAPOP_TMDB_KEY=... en backend/.env');
  process.exit(1);
}
const CLAVE = leerClave();
const ES_TOKEN = CLAVE.startsWith('eyJ'); // el token v4 es un JWT

async function tmdb(ruta, params = {}, intento = 1) {
  const url = new URL('https://api.themoviedb.org/3' + ruta);
  for (const [k, v] of Object.entries(params)) if (v !== undefined) url.searchParams.set(k, v);
  if (!ES_TOKEN) url.searchParams.set('api_key', CLAVE);
  const r = await fetch(url, { headers: ES_TOKEN ? { Authorization: 'Bearer ' + CLAVE } : {} });
  if (r.status === 429 && intento < 5) {
    await new Promise(res => setTimeout(res, 1500 * intento));
    return tmdb(ruta, params, intento + 1);
  }
  if (r.status === 401) {
    console.error('TMDB rechazó la clave (401). Revisa NOVAPOP_TMDB_KEY.');
    process.exit(1);
  }
  if (!r.ok) throw new Error(`TMDB ${r.status} en ${ruta}`);
  return r.json();
}

// ---------------------------------------------------------------- utilidades SQL
const q = v => v === null || v === undefined ? 'NULL'
  : typeof v === 'number' ? String(v)
  : typeof v === 'boolean' ? (v ? 'TRUE' : 'FALSE')
  : "'" + String(v).replace(/'/g, "''") + "'";
const insertar = (tabla, cols, filas, sufijo = '') => filas.length
  ? `INSERT INTO ${tabla} (${cols.join(', ')}) VALUES\n${filas.map(f => '  (' + f.join(', ') + ')').join(',\n')}${sufijo};\n\n`
  : '';
const sub = (tabla, id, col, valor) => `(SELECT ${id} FROM ${tabla} WHERE ${col} = ${q(valor)})`;
const recortar = (texto, max) => texto && texto.length > max ? texto.slice(0, max - 1) + '…' : texto;

// ---------------------------------------------------------------- mapeos
const CLASIF_PELICULA = { G: 'ATP', PG: '+7', 'PG-13': '+13', R: '+16', 'NC-17': '+18' };
const CLASIF_TV = { 'TV-Y': 'ATP', 'TV-G': 'ATP', 'TV-Y7': '+7', 'TV-PG': '+7', 'TV-14': '+13', 'TV-MA': '+16' };
const NOMBRE_IDIOMA = {
  en: 'Inglés', es: 'Español', fr: 'Francés', de: 'Alemán', it: 'Italiano', pt: 'Portugués', ja: 'Japonés',
  ko: 'Coreano', zh: 'Chino', nl: 'Neerlandés', ru: 'Ruso', hi: 'Hindi',
};
const NOMBRE_PAIS = {
  US: 'Estados Unidos', GB: 'Reino Unido', CA: 'Canadá', AU: 'Australia', NZ: 'Nueva Zelanda', JP: 'Japón',
  FR: 'Francia', DE: 'Alemania', ES: 'España', MX: 'México', IT: 'Italia', IE: 'Irlanda', NL: 'Países Bajos',
  KR: 'Corea del Sur', CN: 'China', IN: 'India', BR: 'Brasil', AR: 'Argentina',
};
// Géneros de TMDB (es) → géneros de NovaPop
const GENERO = {
  'Acción': 'Acción', 'Aventura': 'Aventura', 'Animación': 'Animación', 'Comedia': 'Comedia', 'Crimen': 'Crimen',
  'Documental': 'Documental', 'Drama': 'Drama', 'Familia': 'Familiar', 'Fantasía': 'Fantasía', 'Historia': 'Historia',
  'Terror': 'Terror', 'Música': 'Musical', 'Misterio': 'Misterio', 'Romance': 'Romance',
  'Ciencia ficción': 'Ciencia ficción', 'Suspense': 'Suspenso', 'Suspenso': 'Suspenso', 'Bélica': 'Bélica',
  'Western': 'Western', 'Película de TV': null,
  'Action & Adventure': 'Acción', 'Sci-Fi & Fantasy': 'Ciencia ficción', 'Kids': 'Familiar',
  'War & Politics': 'Bélica', 'Soap': 'Drama', 'Reality': null, 'Talk': null, 'News': null,
};

// YouTube responde 200 en oEmbed solo si el video es público y permite insertarse en otras páginas
const embebible = new Map();
async function sePuedeInsertar(clave) {
  if (!embebible.has(clave)) {
    const r = await fetch(`https://www.youtube.com/oembed?format=json&url=https://www.youtube.com/watch?v=${clave}`);
    embebible.set(clave, r.ok);
  }
  return embebible.get(clave);
}

/** El mejor tráiler oficial de YouTube que se pueda ver dentro de NovaPop (prefiere español). */
async function trailerOficial(videos) {
  const lista = (videos?.results || []).filter(v => v.site === 'YouTube' && ['Trailer', 'Teaser'].includes(v.type));
  const puntaje = v => (v.type === 'Trailer' ? 4 : 0) + (v.official ? 2 : 0) + (v.iso_639_1 === 'es' ? 1 : 0);
  lista.sort((a, b) => puntaje(b) - puntaje(a) || (b.size || 0) - (a.size || 0));
  for (const v of lista) {
    if (await sePuedeInsertar(v.key)) return `https://www.youtube.com/watch?v=${v.key}`;
  }
  return null;
}

function separarNombre(nombre) {
  const partes = nombre.trim().split(/\s+/);
  if (partes.length === 1) return [partes[0], '-'];
  return [partes.slice(0, -1).join(' '), partes.at(-1)];
}

// ---------------------------------------------------------------- estado acumulado
const contenidos = [], peliculas = [], temporadas = [], episodios = [];
const generosContenido = [], productorasContenido = [], creditos = [];
const coleccionesSql = [], coleccionContenido = [];
const personas = new Map();       // id TMDB → { id, nombres, apellidos, foto }
const productoras = new Map();    // nombre → código de país
const idiomas = new Map(), paises = new Map(), generosUsados = new Set();
let sigContenido = ID_CONTENIDO, sigPersona = ID_PERSONA, sigTemporada = ID_TEMPORADA, sigEpisodio = ID_EPISODIO;
const advertencias = [];

function persona(p) {
  if (!personas.has(p.id)) {
    const [nombres, apellidos] = separarNombre(p.name);
    personas.set(p.id, { id: sigPersona++, nombres, apellidos, foto: p.profile_path ? IMG + 'w185' + p.profile_path : null });
  }
  return personas.get(p.id).id;
}

function registrarComunes(idContenido, d, esSerie) {
  idiomas.set(d.original_language, NOMBRE_IDIOMA[d.original_language] || d.original_language);
  let principal = true;
  for (const g of d.genres || []) {
    const nombre = GENERO[g.name] === undefined ? g.name : GENERO[g.name];
    if (!nombre || generosContenido.some(x => x[0] === idContenido && x[1] === nombre)) continue;
    generosUsados.add(nombre);
    generosContenido.push([idContenido, nombre, principal]);
    principal = false;
  }
  (d.production_companies || []).slice(0, 3).forEach((pc, i) => {
    const pais = pc.origin_country || 'US';
    paises.set(pais, NOMBRE_PAIS[pais] || pais);
    if (!productoras.has(pc.name)) productoras.set(pc.name, pais);
    productorasContenido.push([idContenido, pc.name, i === 0]);
  });
  let orden = 1;
  const directores = esSerie
    ? (d.created_by || []).map(c => ({ ...c, rol: 'Creador' }))
    : (d.credits?.crew || []).filter(c => c.job === 'Director').map(c => ({ ...c, rol: 'Director' }));
  for (const c of directores.slice(0, 2)) creditos.push([idContenido, persona(c), c.rol, null, orden++]);
  const reparto = esSerie ? (d.aggregate_credits?.cast || []) : (d.credits?.cast || []);
  for (const a of reparto.slice(0, 8)) {
    const personaje = esSerie ? a.roles?.[0]?.character : a.character;
    const rol = (d.genres || []).some(g => g.name === 'Animación') ? 'Voz' : 'Actor';
    if (creditos.some(x => x[0] === idContenido && x[1] === persona(a) && x[2] === rol)) continue;
    creditos.push([idContenido, persona(a), rol, recortar(personaje, 150) || null, orden++]);
  }
}

// ---------------------------------------------------------------- películas
async function importarPelicula([titulo, anio]) {
  const busqueda = await tmdb('/search/movie', { query: titulo, year: anio, language: IDIOMA });
  const hallado = busqueda.results?.[0];
  if (!hallado) { advertencias.push(`No encontrada: ${titulo} (${anio})`); return null; }
  const d = await tmdb(`/movie/${hallado.id}`, {
    language: IDIOMA, append_to_response: 'credits,videos,release_dates', include_video_language: 'es,en,null',
  });
  let sinopsis = d.overview;
  if (!sinopsis) sinopsis = (await tmdb(`/movie/${hallado.id}`, { language: 'en-US' })).overview;
  const cert = (d.release_dates?.results || []).find(r => r.iso_3166_1 === 'US')?.release_dates
    .map(x => x.certification).find(Boolean);
  const imagen = d.backdrop_path || d.poster_path;

  const id = sigContenido++;
  contenidos.push({
    id, tipo: 'PELICULA', clasif: CLASIF_PELICULA[cert] || '+13', idioma: d.original_language,
    original: d.original_title, local: d.title, anio: Number((d.release_date || String(anio)).slice(0, 4)),
    sinopsis, poster: imagen ? IMG + 'w1280' + imagen : null,
  });
  const trailer = await trailerOficial(d.videos);
  peliculas.push([id, Math.max(1, d.runtime || 90), trailer]);
  registrarComunes(id, d, false);
  console.log(`  🎬 ${d.title} (${anio})${trailer ? '' : '  ⚠ sin tráiler'}`);
  return id;
}

// ---------------------------------------------------------------- series
async function importarSerie([titulo, anio]) {
  let busqueda = await tmdb('/search/tv', { query: titulo, first_air_date_year: anio, language: IDIOMA });
  if (!busqueda.results?.length) busqueda = await tmdb('/search/tv', { query: titulo, language: IDIOMA });
  const hallado = busqueda.results?.[0];
  if (!hallado) { advertencias.push(`No encontrada: ${titulo} (${anio})`); return null; }
  const d = await tmdb(`/tv/${hallado.id}`, {
    language: IDIOMA, append_to_response: 'aggregate_credits,videos,content_ratings', include_video_language: 'es,en,null',
  });
  let sinopsis = d.overview;
  if (!sinopsis) sinopsis = (await tmdb(`/tv/${hallado.id}`, { language: 'en-US' })).overview;
  const cert = (d.content_ratings?.results || []).find(r => r.iso_3166_1 === 'US')?.rating;
  const imagen = d.backdrop_path || d.poster_path;

  const id = sigContenido++;
  contenidos.push({
    id, tipo: 'SERIE', clasif: CLASIF_TV[cert] || '+13', idioma: d.original_language,
    original: d.original_name, local: d.name, anio: Number((d.first_air_date || String(anio)).slice(0, 4)),
    sinopsis, poster: imagen ? IMG + 'w1280' + imagen : null,
  });
  registrarComunes(id, d, true);
  const trailerSerie = await trailerOficial(d.videos);
  const duracionTipica = d.episode_run_time?.[0] || 30;

  let totalEpisodios = 0;
  for (const s of (d.seasons || []).filter(s => s.season_number > 0 && s.episode_count > 0)) {
    const t = await tmdb(`/tv/${hallado.id}/season/${s.season_number}`, {
      language: IDIOMA, append_to_response: 'videos', include_video_language: 'es,en,null',
    });
    const idTemporada = sigTemporada++;
    const trailer = (await trailerOficial(t.videos)) || (s.season_number === 1 ? trailerSerie : null);
    temporadas.push([idTemporada, id, s.season_number, recortar(t.name || `Temporada ${s.season_number}`, 200),
      t.air_date ? Number(t.air_date.slice(0, 4)) : null, trailer]);
    for (const e of t.episodes || []) {
      episodios.push([sigEpisodio++, idTemporada, e.episode_number,
        recortar(e.name || `Episodio ${e.episode_number}`, 200), Math.max(1, e.runtime || duracionTipica)]);
      totalEpisodios++;
    }
  }
  console.log(`  📺 ${d.name}: ${temporadas.filter(t => t[1] === id).length} temporadas, ${totalEpisodios} episodios${trailerSerie ? '' : '  ⚠ sin tráiler'}`);
  return id;
}

// ---------------------------------------------------------------- ejecución
const lista = JSON.parse(readFileSync(join(AQUI, 'titulos.json'), 'utf8'));
let idColeccion = ID_COLECCION;
let ordenVitrina = 7;
for (const col of lista.colecciones) {
  console.log(`\n${col.nombre}`);
  const ids = [];
  for (const p of col.peliculas || []) { const id = await importarPelicula(p); if (id) ids.push(id); }
  for (const s of col.series || []) { const id = await importarSerie(s); if (id) ids.push(id); }
  const banner = contenidos.find(c => c.id === ids[0])?.poster || null;
  coleccionesSql.push([idColeccion, q(col.nombre), q(col.tipo), q(banner), ordenVitrina++]);
  ids.forEach((id, i) => coleccionContenido.push([idColeccion, id, i + 1]));
  idColeccion++;
}

// Fechas de nacimiento del elenco (consulta por persona, en paralelo de a 6)
console.log(`\nDatos de ${personas.size} personas…`);
const entradas = [...personas.entries()];
for (let i = 0; i < entradas.length; i += 6) {
  await Promise.all(entradas.slice(i, i + 6).map(async ([idTmdb, p]) => {
    try { p.nacimiento = (await tmdb(`/person/${idTmdb}`)).birthday || null; } catch { p.nacimiento = null; }
  }));
}

// ---------------------------------------------------------------- SQL
let sql = `-- =====================================================================
--  NovaPop — Script 04: catálogo importado de TMDB (solo tráiler)
--  Generado el ${new Date().toISOString().slice(0, 10)} por database/tmdb/importar-tmdb.mjs
--  Ejecutar DESPUÉS de 01, 02 y 03. Se puede volver a ejecutar: primero borra lo importado.
--
--  Fuente de datos e imágenes: TMDB (https://www.themoviedb.org).
--  "This product uses the TMDB API but is not endorsed or certified by TMDB."
--  Títulos con derechos de autor: se cargan SIN video completo, solo con su tráiler oficial.
-- =====================================================================
SET search_path TO novapop;

-- 1) Limpia una importación anterior (IDs reservados ≥ ${ID_CONTENIDO})
DELETE FROM calificacion_contenido WHERE id_contenido >= ${ID_CONTENIDO};
DELETE FROM mi_lista WHERE id_contenido >= ${ID_CONTENIDO};
DELETE FROM historial_reproduccion WHERE id_contenido >= ${ID_CONTENIDO};
DELETE FROM sesion_reproduccion WHERE id_contenido >= ${ID_CONTENIDO};
DELETE FROM coleccion_contenido WHERE id_contenido >= ${ID_CONTENIDO} OR id_coleccion >= ${ID_COLECCION};
DELETE FROM coleccion WHERE id_coleccion >= ${ID_COLECCION};
DELETE FROM contenido_pais WHERE id_contenido >= ${ID_CONTENIDO};
DELETE FROM contenido_productora WHERE id_contenido >= ${ID_CONTENIDO};
DELETE FROM contenido_persona WHERE id_contenido >= ${ID_CONTENIDO};
DELETE FROM contenido_genero WHERE id_contenido >= ${ID_CONTENIDO};
DELETE FROM episodio WHERE id_temporada IN (SELECT id_temporada FROM temporada WHERE id_contenido >= ${ID_CONTENIDO});
DELETE FROM temporada WHERE id_contenido >= ${ID_CONTENIDO};
DELETE FROM pelicula WHERE id_contenido >= ${ID_CONTENIDO};
DELETE FROM contenido WHERE id_contenido >= ${ID_CONTENIDO};
DELETE FROM persona WHERE id_persona >= ${ID_PERSONA};

-- 2) Catálogos que podrían faltar (no duplica si ya existen)
`;
sql += insertar('idioma', ['codigo_iso', 'nombre_idioma'], [...idiomas].map(([c, n]) => [q(c), q(n)]), ' ON CONFLICT (codigo_iso) DO NOTHING');
sql += insertar('pais', ['codigo_iso', 'nombre_pais'], [...paises].filter(([c]) => c.length === 2).map(([c, n]) => [q(c), q(n)]), ' ON CONFLICT (codigo_iso) DO NOTHING');
sql += insertar('genero', ['nombre_genero'], [...generosUsados].map(g => [q(g)]), ' ON CONFLICT (nombre_genero) DO NOTHING');
sql += insertar('rol_produccion', ['nombre_rol_produccion'], [['Creador'], ['Actor'], ['Voz'], ['Director']].map(r => [q(r[0])]), ' ON CONFLICT (nombre_rol_produccion) DO NOTHING');
sql += insertar('productora', ['id_pais', 'nombre_productora'],
  [...productoras].map(([n, pais]) => [sub('pais', 'id_pais', 'codigo_iso', pais.length === 2 ? pais : 'US'), q(recortar(n, 150))]),
  ' ON CONFLICT (nombre_productora) DO NOTHING');

sql += '-- 3) Contenido\n';
sql += insertar('contenido', ['id_contenido', 'id_clasificacion', 'id_idioma_original', 'tipo_contenido', 'titulo_original',
  'titulo_localizado', 'anio_lanzamiento', 'sinopsis', 'poster_url', 'usuario_creacion'],
  contenidos.map(c => [c.id, sub('clasificacion_edad', 'id_clasificacion', 'codigo', c.clasif),
    sub('idioma', 'id_idioma', 'codigo_iso', c.idioma), q(c.tipo), q(recortar(c.original, 200)),
    q(c.local !== c.original ? recortar(c.local, 200) : null), c.anio, q(c.sinopsis || null), q(c.poster), q('IMPORTADOR_TMDB')]));
sql += insertar('pelicula', ['id_contenido', 'id_video', 'duracion_minutos', 'url_trailer'],
  peliculas.map(([id, min, tr]) => [id, 'NULL', min, q(tr)]));
sql += insertar('temporada', ['id_temporada', 'id_contenido', 'numero_temporada', 'titulo_temporada', 'anio_estreno', 'url_trailer'],
  temporadas.map(([id, c, n, t, a, tr]) => [id, c, n, q(t), q(a), q(tr)]));
sql += insertar('episodio', ['id_episodio', 'id_temporada', 'id_video', 'numero_episodio', 'titulo_episodio', 'duracion_minutos'],
  episodios.map(([id, t, n, titulo, min]) => [id, t, 'NULL', n, q(titulo), min]));

sql += '-- 4) Géneros, productoras, elenco y disponibilidad\n';
sql += insertar('contenido_genero', ['id_contenido', 'id_genero', 'es_principal'],
  generosContenido.map(([c, g, p]) => [c, sub('genero', 'id_genero', 'nombre_genero', g), q(p)]));
const prodUnicas = [...new Map(productorasContenido.map(x => [x[0] + '|' + x[1], x])).values()];
sql += insertar('contenido_productora', ['id_contenido', 'id_productora', 'es_productora_principal'],
  prodUnicas.map(([c, n, p]) => [c, sub('productora', 'id_productora', 'nombre_productora', recortar(n, 150)), q(p)]));
sql += insertar('persona', ['id_persona', 'id_pais', 'nombres', 'apellidos', 'fecha_nacimiento', 'foto_url'],
  [...personas.values()].map(p => [p.id, 'NULL', q(recortar(p.nombres, 100)), q(recortar(p.apellidos, 100)), q(p.nacimiento), q(p.foto)]));
sql += insertar('contenido_persona', ['id_contenido', 'id_persona', 'id_rol_produccion', 'nombre_personaje', 'orden_creditos'],
  creditos.map(([c, p, rol, pj, o]) => [c, p, sub('rol_produccion', 'id_rol_produccion', 'nombre_rol_produccion', rol), q(pj), o]));
sql += insertar('contenido_pais', ['id_contenido', 'id_pais', 'fecha_disponible_desde', 'fecha_disponible_hasta'],
  contenidos.flatMap(c => PAISES_DISPONIBLES.map(p => [c.id, sub('pais', 'id_pais', 'codigo_iso', p), q('2024-01-01'), 'NULL'])));

sql += '-- 5) Colecciones de la vitrina\n';
sql += insertar('coleccion', ['id_coleccion', 'nombre_coleccion', 'tipo_coleccion', 'banner_url', 'orden_vitrina'], coleccionesSql);
sql += insertar('coleccion_contenido', ['id_coleccion', 'id_contenido', 'orden_aparicion'], coleccionContenido);

sql += `-- 6) Reajusta las secuencias IDENTITY
DO $$
DECLARE c RECORD; v_max BIGINT;
BEGIN
    FOR c IN SELECT table_name, column_name FROM information_schema.columns
             WHERE table_schema = 'novapop' AND is_identity = 'YES'
    LOOP
        EXECUTE format('SELECT COALESCE(MAX(%I), 0) FROM novapop.%I', c.column_name, c.table_name) INTO v_max;
        PERFORM setval(pg_get_serial_sequence(format('novapop.%I', c.table_name), c.column_name), GREATEST(v_max, 1), v_max > 0);
    END LOOP;
END $$;
`;

writeFileSync(SALIDA, sql);
console.log(`\n✅ ${contenidos.length} títulos (${peliculas.length} películas, ${contenidos.length - peliculas.length} series, ${episodios.length} episodios), ${personas.size} personas.`);
console.log(`   Generado: ${SALIDA}`);
if (advertencias.length) console.log('\n⚠ ' + advertencias.join('\n⚠ '));
