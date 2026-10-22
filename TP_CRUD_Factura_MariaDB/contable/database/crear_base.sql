-- Crea la base de datos "contable" con soporte completo de acentos y caracteres especiales (utf8mb4).
-- Cómo usarlo: XAMPP -> iniciar MySQL -> abrir http://localhost/phpmyadmin -> pestaña "SQL" -> pegar y ejecutar.
-- Las tablas NO hace falta crearlas: las crea Spring Boot (Hibernate) al arrancar.
CREATE DATABASE IF NOT EXISTS contable
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
