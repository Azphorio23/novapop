-- =====================================================================
--  NovaPop — Script 03: títulos "solo tráiler"
--  Para bases creadas con una versión anterior de 01_esquema.sql.
--  (Si acabas de ejecutar el 01 actual, este script no cambia nada.)
--
--  Permite catalogar películas y series con derechos de autor (Marvel,
--  Disney, DC, Breaking Bad…) mostrando su ficha y su tráiler oficial,
--  pero sin video completo.
-- =====================================================================
SET search_path TO novapop;

-- Una película o un episodio pueden existir sin video reproducible
ALTER TABLE pelicula ALTER COLUMN id_video DROP NOT NULL;
ALTER TABLE episodio ALTER COLUMN id_video DROP NOT NULL;

-- Las series guardan el tráiler en cada temporada
ALTER TABLE temporada ADD COLUMN IF NOT EXISTS url_trailer VARCHAR(500);

COMMENT ON COLUMN pelicula.id_video IS 'NULL = título solo con tráiler (sin licencia para el video completo)';
COMMENT ON COLUMN episodio.id_video IS 'NULL = episodio no disponible para reproducir';
COMMENT ON COLUMN temporada.url_trailer IS 'Tráiler oficial de la temporada (YouTube)';
