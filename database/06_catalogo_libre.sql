-- =====================================================================
--  NovaPop — Script 06: catálogo libre (dominio público y Creative Commons)
--  Ejecutar DESPUÉS de 01 a 05. Se puede volver a ejecutar: primero borra lo cargado por este script.
--
--  Videos completos alojados en Wikimedia Commons, todos con licencia libre:
--    Dominio público : Nosferatu (1922), Viaje a la Luna (1902), Asalto y robo de un tren (1903)
--    CC BY 4.0       : Wing It!, Hero, Glass Half (Blender Foundation / Blender Studio)
--    CC0             : Sita Sings the Blues (Nina Paley)
--  IDs reservados: contenido y video 500-599, persona 500-599.
-- =====================================================================
SET search_path TO novapop;

-- 1) Limpia una carga anterior de este script
DELETE FROM calificacion_contenido WHERE id_contenido BETWEEN 500 AND 599;
DELETE FROM mi_lista WHERE id_contenido BETWEEN 500 AND 599;
DELETE FROM historial_reproduccion WHERE id_contenido BETWEEN 500 AND 599;
DELETE FROM sesion_reproduccion WHERE id_contenido BETWEEN 500 AND 599;
DELETE FROM coleccion_contenido WHERE id_contenido BETWEEN 500 AND 599;
DELETE FROM contenido_pais WHERE id_contenido BETWEEN 500 AND 599;
DELETE FROM contenido_productora WHERE id_contenido BETWEEN 500 AND 599;
DELETE FROM contenido_persona WHERE id_contenido BETWEEN 500 AND 599;
DELETE FROM contenido_genero WHERE id_contenido BETWEEN 500 AND 599;
DELETE FROM pelicula WHERE id_contenido BETWEEN 500 AND 599;
DELETE FROM contenido WHERE id_contenido BETWEEN 500 AND 599;
DELETE FROM subtitulo WHERE id_video BETWEEN 500 AND 599;
DELETE FROM pista_audio WHERE id_video BETWEEN 500 AND 599;
DELETE FROM video_calidad WHERE id_video BETWEEN 500 AND 599;
DELETE FROM video WHERE id_video BETWEEN 500 AND 599;
DELETE FROM persona WHERE id_persona BETWEEN 500 AND 599;

-- 2) Videos y sus calidades (240p, 480p, 1080p y, si existe, el original 4K)
INSERT INTO video (id_video, duracion_segundos, codec_video, fecha_procesamiento) VALUES
  (500, 5519, 'VP9', '2026-11-05 10:00:00-05'),
  (501, 766, 'VP9', '2026-11-05 10:00:00-05'),
  (502, 808, 'VP9', '2026-11-05 10:00:00-05'),
  (503, 238, 'VP9', '2026-11-05 10:00:00-05'),
  (504, 237, 'VP9', '2026-11-05 10:00:00-05'),
  (505, 193, 'VP9', '2026-11-05 10:00:00-05'),
  (506, 4891, 'VP9', '2026-11-05 10:00:00-05');
INSERT INTO video_calidad (id_video, id_perfil_calidad, url_manifiesto, tamano_mb) VALUES
  (500, 1, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/7/78/Nosferatu_%281922%29.webm/Nosferatu_%281922%29.webm.240p.vp9.webm', 115.48),
  (500, 2, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/7/78/Nosferatu_%281922%29.webm/Nosferatu_%281922%29.webm.480p.vp9.webm', 417.73),
  (500, 4, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/7/78/Nosferatu_%281922%29.webm/Nosferatu_%281922%29.webm.1080p.vp9.webm', 2083.16),
  (501, 1, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/6/6d/Le_Voyage_dans_la_Lune_%281902%29.webm/Le_Voyage_dans_la_Lune_%281902%29.webm.240p.vp9.webm', 22.60),
  (501, 2, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/6/6d/Le_Voyage_dans_la_Lune_%281902%29.webm/Le_Voyage_dans_la_Lune_%281902%29.webm.480p.vp9.webm', 73.19),
  (501, 4, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/6/6d/Le_Voyage_dans_la_Lune_%281902%29.webm/Le_Voyage_dans_la_Lune_%281902%29.webm.1080p.vp9.webm', 290.27),
  (502, 1, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/d/d7/The_Great_Train_Robbery_%281903%29.webm/The_Great_Train_Robbery_%281903%29.webm.240p.vp9.webm', 24.19),
  (502, 2, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/d/d7/The_Great_Train_Robbery_%281903%29.webm/The_Great_Train_Robbery_%281903%29.webm.480p.vp9.webm', 78.07),
  (502, 4, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/d/d7/The_Great_Train_Robbery_%281903%29.webm/The_Great_Train_Robbery_%281903%29.webm.1080p.vp9.webm', 300.61),
  (503, 1, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/3/38/WING_IT%21_-_Blender_Open_Movie-full_movie.webm/WING_IT%21_-_Blender_Open_Movie-full_movie.webm.240p.vp9.webm', 8.30),
  (503, 2, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/3/38/WING_IT%21_-_Blender_Open_Movie-full_movie.webm/WING_IT%21_-_Blender_Open_Movie-full_movie.webm.480p.vp9.webm', 18.74),
  (503, 4, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/3/38/WING_IT%21_-_Blender_Open_Movie-full_movie.webm/WING_IT%21_-_Blender_Open_Movie-full_movie.webm.1080p.vp9.webm', 51.06),
  (504, 1, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/a/a9/HERO_-_Blender_Open_Movie-full_movie.webm/HERO_-_Blender_Open_Movie-full_movie.webm.240p.vp9.webm', 7.82),
  (504, 2, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/a/a9/HERO_-_Blender_Open_Movie-full_movie.webm/HERO_-_Blender_Open_Movie-full_movie.webm.480p.vp9.webm', 15.79),
  (504, 4, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/a/a9/HERO_-_Blender_Open_Movie-full_movie.webm/HERO_-_Blender_Open_Movie-full_movie.webm.1080p.vp9.webm', 40.86),
  (505, 1, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/0/02/Glass_Half_-_Blender_Open_Movie-full_movie.webm/Glass_Half_-_Blender_Open_Movie-full_movie.webm.240p.vp9.webm', 7.56),
  (505, 2, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/0/02/Glass_Half_-_Blender_Open_Movie-full_movie.webm/Glass_Half_-_Blender_Open_Movie-full_movie.webm.480p.vp9.webm', 17.40),
  (505, 4, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/0/02/Glass_Half_-_Blender_Open_Movie-full_movie.webm/Glass_Half_-_Blender_Open_Movie-full_movie.webm.1080p.vp9.webm', 46.46),
  (505, 5, 'https://upload.wikimedia.org/wikipedia/commons/0/02/Glass_Half_-_Blender_Open_Movie-full_movie.webm', 167.02),
  (506, 1, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/e/e4/Sita_Sings_the_Blues.webm/Sita_Sings_the_Blues.webm.240p.vp9.webm', 172.26),
  (506, 2, 'https://upload.wikimedia.org/wikipedia/commons/transcoded/e/e4/Sita_Sings_the_Blues.webm/Sita_Sings_the_Blues.webm.480p.vp9.webm', 563.08);
INSERT INTO pista_audio (id_video, id_idioma, tipo_pista, canal_audio) VALUES
  (500, 5, 'ORIGINAL', 'ESTEREO'),
  (501, 9, 'ORIGINAL', 'ESTEREO'),
  (502, 9, 'ORIGINAL', 'ESTEREO'),
  (503, 9, 'ORIGINAL', 'ESTEREO'),
  (504, 9, 'ORIGINAL', 'ESTEREO'),
  (505, 2, 'ORIGINAL', 'ESTEREO'),
  (506, 2, 'ORIGINAL', 'ESTEREO');
INSERT INTO subtitulo (id_video, id_idioma, tipo_subtitulo, url_archivo) VALUES
  (500, 1, 'COMPLETO', 'https://commons.wikimedia.org/w/api.php?action=timedtext&trackformat=vtt&lang=es&title=File%3ANosferatu_(1922).webm'),
  (500, 2, 'COMPLETO', 'https://commons.wikimedia.org/w/api.php?action=timedtext&trackformat=vtt&lang=en&title=File%3ANosferatu_(1922).webm');

-- 3) Fichas de los títulos
INSERT INTO contenido (id_contenido, id_clasificacion, id_idioma_original, tipo_contenido, titulo_original, titulo_localizado, anio_lanzamiento, sinopsis, poster_url) VALUES
  (500, 3, 5, 'PELICULA', 'Nosferatu, eine Symphonie des Grauens', 'Nosferatu, una sinfonía del horror', 1922, 'Thomas Hutter, un joven agente inmobiliario, viaja a los Cárpatos para venderle una casa al misterioso conde Orlok. Pronto descubre que su anfitrión es un vampiro que pondrá rumbo a su ciudad… y hacia su esposa Ellen. La obra maestra del expresionismo alemán.', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/78/Nosferatu_%281922%29.webm/1280px-seek%3D1450-Nosferatu_%281922%29.webm.jpg'),
  (501, 1, 9, 'PELICULA', 'Le Voyage dans la Lune', 'Viaje a la Luna', 1902, 'Un congreso de astrónomos aprueba el audaz plan del profesor Barbenfouillis: llegar a la Luna dentro de una cápsula disparada por un cañón gigante. Allí los esperan paisajes fantásticos y los selenitas. El primer gran filme de ciencia ficción de la historia.', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6d/Le_Voyage_dans_la_Lune_%281902%29.webm/1280px-seek%3D250-Le_Voyage_dans_la_Lune_%281902%29.webm.jpg'),
  (502, 3, 9, 'PELICULA', 'The Great Train Robbery', 'Asalto y robo de un tren', 1903, 'Una banda de forajidos somete al telegrafista de una estación, asalta un tren de pasajeros y huye con el botín, mientras los vecinos del pueblo salen en su persecución. Uno de los primeros westerns y una obra pionera del montaje cinematográfico.', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d7/The_Great_Train_Robbery_%281903%29.webm/1280px-seek%3D780-The_Great_Train_Robbery_%281903%29.webm.jpg'),
  (503, 1, 9, 'PELICULA', 'Wing It!', 'Wing It!', 2023, 'Un gato y un perro, con más entusiasmo que planos, intentan despegar en un cohete construido con lo que tienen a mano. ¿Qué podría salir mal? Comedia animada de Blender Studio.', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/38/WING_IT%21_-_Blender_Open_Movie-full_movie.webm/1280px-seek%3D100-WING_IT%21_-_Blender_Open_Movie-full_movie.webm.jpg'),
  (504, 2, 9, 'PELICULA', 'Hero', 'Hero', 2018, 'Una historia épica de un guerrero y su pueblo, dibujada a mano con Grease Pencil, la herramienta de Blender para animar en 2D dentro de un espacio 3D.', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a9/HERO_-_Blender_Open_Movie-full_movie.webm/1280px-seek%3D120-HERO_-_Blender_Open_Movie-full_movie.webm.jpg'),
  (505, 1, 2, 'PELICULA', 'Glass Half', 'Glass Half', 2015, 'En una galería de arte, dos críticos con gustos opuestos discuten frente a las mismas obras, y cada uno ve algo muy distinto. Una comedia animada sobre cómo miramos el arte.', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/02/Glass_Half_-_Blender_Open_Movie-full_movie.webm/1280px-seek%3D100-Glass_Half_-_Blender_Open_Movie-full_movie.webm.jpg'),
  (506, 3, 2, 'PELICULA', 'Sita Sings the Blues', 'Sita canta el blues', 2008, 'Basada en el Ramayana: la diosa Sita es separada de su amado Rama, mientras en la vida real la animadora Nina Paley es abandonada por correo electrónico. Tres marionetas de sombras narran la tragedia antigua y la comedia moderna, al ritmo de los blues de Annette Hanshaw.', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e4/Sita_Sings_the_Blues.webm/1280px-seek%3D3300-Sita_Sings_the_Blues.webm.jpg');
INSERT INTO contenido_genero (id_contenido, id_genero, es_principal) VALUES
  (500, 8, TRUE),
  (500, 4, FALSE),
  (500, 6, FALSE),
  (501, 3, TRUE),
  (501, 2, FALSE),
  (501, 4, FALSE),
  (501, 10, FALSE),
  (502, 7, TRUE),
  (502, 2, FALSE),
  (502, 10, FALSE),
  (503, 1, TRUE),
  (503, 5, FALSE),
  (503, 9, FALSE),
  (503, 10, FALSE),
  (504, 1, TRUE),
  (504, 4, FALSE),
  (504, 6, FALSE),
  (504, 10, FALSE),
  (505, 5, TRUE),
  (505, 1, FALSE),
  (505, 10, FALSE),
  (506, 1, TRUE),
  (506, 5, FALSE),
  (506, 4, FALSE),
  (506, 6, FALSE);
INSERT INTO pelicula (id_contenido, id_video, duracion_minutos, url_trailer) VALUES
  (500, 500, 92, NULL),
  (501, 501, 13, NULL),
  (502, 502, 13, NULL),
  (503, 503, 4, NULL),
  (504, 504, 4, NULL),
  (505, 505, 3, NULL),
  (506, 506, 82, NULL);

-- 4) Elenco y productoras
INSERT INTO persona (id_persona, id_pais, nombres, apellidos, fecha_nacimiento) VALUES
  (500, 12, 'F. W.', 'Murnau', '1888-12-28'),
  (501, 12, 'Max', 'Schreck', '1879-09-06'),
  (502, 12, 'Gustav', 'von Wangenheim', '1895-02-18'),
  (503, 12, 'Greta', 'Schröder', '1892-06-27'),
  (504, 11, 'Georges', 'Méliès', '1861-12-08'),
  (505, 9, 'Edwin S.', 'Porter', '1870-04-21'),
  (506, 9, 'Justus D.', 'Barnes', NULL),
  (507, 8, 'Daniel', 'Martínez Lara', NULL),
  (508, NULL, 'Beorn', 'Leonard', NULL),
  (509, 9, 'Nina', 'Paley', '1968-05-03');
INSERT INTO contenido_persona (id_contenido, id_persona, id_rol_produccion, nombre_personaje, orden_creditos) VALUES
  (500, 500, 1, NULL, 1),
  (500, 501, 3, 'Conde Orlok', 2),
  (500, 502, 3, 'Thomas Hutter', 3),
  (500, 503, 3, 'Ellen Hutter', 4),
  (501, 504, 1, NULL, 5),
  (501, 504, 3, 'Profesor Barbenfouillis', 6),
  (502, 505, 1, NULL, 7),
  (502, 506, 3, 'Líder de los bandidos', 8),
  (504, 507, 1, NULL, 9),
  (505, 508, 1, NULL, 10),
  (506, 509, 1, NULL, 11),
  (506, 509, 6, NULL, 12);
INSERT INTO productora (id_pais, nombre_productora) VALUES
  ((SELECT id_pais FROM pais WHERE codigo_iso = 'DE'), 'Prana-Film'),
  ((SELECT id_pais FROM pais WHERE codigo_iso = 'FR'), 'Star Film'),
  ((SELECT id_pais FROM pais WHERE codigo_iso = 'US'), 'Edison Manufacturing Company'),
  ((SELECT id_pais FROM pais WHERE codigo_iso = 'NL'), 'Blender Studio'),
  ((SELECT id_pais FROM pais WHERE codigo_iso = 'NL'), 'Blender Foundation'),
  ((SELECT id_pais FROM pais WHERE codigo_iso = 'NL'), 'Blender Institute')
  ON CONFLICT (nombre_productora) DO NOTHING;
INSERT INTO contenido_productora (id_contenido, id_productora, es_productora_principal)
SELECT v.id_contenido, p.id_productora, TRUE
FROM (VALUES
  (500, 'Prana-Film'),
  (501, 'Star Film'),
  (502, 'Edison Manufacturing Company'),
  (503, 'Blender Studio'),
  (504, 'Blender Foundation'),
  (505, 'Blender Institute')
) AS v(id_contenido, nombre)
JOIN productora p ON p.nombre_productora = v.nombre;

-- 5) Disponibles en los mismos 9 países que el resto del catálogo abierto
INSERT INTO contenido_pais (id_contenido, id_pais, fecha_disponible_desde, fecha_disponible_hasta) VALUES
  (500, 1, '2026-11-05', NULL),
  (500, 2, '2026-11-05', NULL),
  (500, 3, '2026-11-05', NULL),
  (500, 4, '2026-11-05', NULL),
  (500, 5, '2026-11-05', NULL),
  (500, 6, '2026-11-05', NULL),
  (500, 7, '2026-11-05', NULL),
  (500, 8, '2026-11-05', NULL),
  (500, 9, '2026-11-05', NULL),
  (501, 1, '2026-11-05', NULL),
  (501, 2, '2026-11-05', NULL),
  (501, 3, '2026-11-05', NULL),
  (501, 4, '2026-11-05', NULL),
  (501, 5, '2026-11-05', NULL),
  (501, 6, '2026-11-05', NULL),
  (501, 7, '2026-11-05', NULL),
  (501, 8, '2026-11-05', NULL),
  (501, 9, '2026-11-05', NULL),
  (502, 1, '2026-11-05', NULL),
  (502, 2, '2026-11-05', NULL),
  (502, 3, '2026-11-05', NULL),
  (502, 4, '2026-11-05', NULL),
  (502, 5, '2026-11-05', NULL),
  (502, 6, '2026-11-05', NULL),
  (502, 7, '2026-11-05', NULL),
  (502, 8, '2026-11-05', NULL),
  (502, 9, '2026-11-05', NULL),
  (503, 1, '2026-11-05', NULL),
  (503, 2, '2026-11-05', NULL),
  (503, 3, '2026-11-05', NULL),
  (503, 4, '2026-11-05', NULL),
  (503, 5, '2026-11-05', NULL),
  (503, 6, '2026-11-05', NULL),
  (503, 7, '2026-11-05', NULL),
  (503, 8, '2026-11-05', NULL),
  (503, 9, '2026-11-05', NULL),
  (504, 1, '2026-11-05', NULL),
  (504, 2, '2026-11-05', NULL),
  (504, 3, '2026-11-05', NULL),
  (504, 4, '2026-11-05', NULL),
  (504, 5, '2026-11-05', NULL),
  (504, 6, '2026-11-05', NULL),
  (504, 7, '2026-11-05', NULL),
  (504, 8, '2026-11-05', NULL),
  (504, 9, '2026-11-05', NULL),
  (505, 1, '2026-11-05', NULL),
  (505, 2, '2026-11-05', NULL),
  (505, 3, '2026-11-05', NULL),
  (505, 4, '2026-11-05', NULL),
  (505, 5, '2026-11-05', NULL),
  (505, 6, '2026-11-05', NULL),
  (505, 7, '2026-11-05', NULL),
  (505, 8, '2026-11-05', NULL),
  (505, 9, '2026-11-05', NULL),
  (506, 1, '2026-11-05', NULL),
  (506, 2, '2026-11-05', NULL),
  (506, 3, '2026-11-05', NULL),
  (506, 4, '2026-11-05', NULL),
  (506, 5, '2026-11-05', NULL),
  (506, 6, '2026-11-05', NULL),
  (506, 7, '2026-11-05', NULL),
  (506, 8, '2026-11-05', NULL),
  (506, 9, '2026-11-05', NULL);

-- 6) Vitrinas: Clásicos del cine abierto, Universo Blender Studio y Cortos para un café
INSERT INTO coleccion_contenido (id_coleccion, id_contenido, orden_aparicion) VALUES
  (3, 500, 50),
  (3, 501, 51),
  (3, 502, 52),
  (3, 506, 53),
  (5, 503, 54),
  (5, 504, 55),
  (5, 505, 56),
  (6, 503, 57),
  (6, 504, 58),
  (6, 505, 59),
  (6, 501, 60);

-- Reajusta las columnas IDENTITY para que los próximos INSERT continúen después del último ID
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
