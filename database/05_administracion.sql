-- =====================================================================
--  NovaPop — Script 05: acceso al panel de administración
--  Agrega la contraseña al personal de la plataforma (tabla USUARIO)
--  para que pueda iniciar sesión en /admin y gestionar los CRUD.
--
--  Usuarios del panel (clave de los tres: Admin2026!)
--    admin    → ADMINISTRADOR    (todo, incluida la seguridad)
--    editor   → EDITOR_CONTENIDO (catálogos y contenido)
--    soporte  → SOPORTE          (catálogos y contenido)
-- =====================================================================
SET search_path TO novapop;

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS clave_hash VARCHAR(100);
COMMENT ON COLUMN usuario.clave_hash IS 'Contraseña del personal (BCrypt) para el panel de administración';

UPDATE usuario
   SET clave_hash = '$2a$10$Ied/zxGFYNI8wecKdgtT4eMTCM5HwkE32cZ6EB42FW7ualDCita6S' -- Admin2026!
 WHERE nombre_usuario IN ('admin', 'editor', 'soporte') AND clave_hash IS NULL;
