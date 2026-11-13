-- =========================================================================
-- DATOS DE PRUEBA: cinecomfenalco_datos_prueba.sql
--
--
-- Requisitos:
--   1. Ejecutar despues de crear las tablas (Hibernate con ddl-auto=update
--      o docs/archive/cinecomfenalco_schema.sql).
--   2. La tabla facturas debe tener la columna version que agrega Hibernate.
--   3. Ejecutar sobre una base vacia; si los datos ya se cargaron, no repetir.
--
-- Base usada por application.properties:
--   cinecomfenalco_db
-- =========================================================================

USE cinecomfenalco_db;

SET NAMES utf8mb4;

START TRANSACTION;

-- =========================================================================
-- 1. USUARIOS
--    admin / admin123 (hash BCrypt valido)
--    laura no tiene contrasena, por eso contrasena_hash = NULL
-- =========================================================================
INSERT INTO usuarios
    (id_usuario, nombre_completo, username, rol, contrasena_hash, estado)
VALUES
    (1, 'Administrador General', 'admin', 'ADMINISTRADOR',
     '$2a$10$TmL9ZvnmgtWD583ANy4wUO1A05yEwhjvJHMAt.SYHHzEiJqXYsQN6', 'ACTIVO'),
    (2, 'Laura Taquilla', 'laura', 'TAQUILLERO', NULL, 'ACTIVO');

-- =========================================================================
-- 2. PELICULAS
-- =========================================================================
INSERT INTO peliculas
    (id_pelicula, titulo, sinopsis, duracion, clasificacion, genero, imagen_url, estado)
VALUES
    (1, 'Dune: Parte Dos',
     'Paul Atreides se une a los Fremen para vengar a su familia.',
     '2H 46M', 'MAYORES_15', 'CIENCIA_FICCION',
     'https://www.mubis.es/media/articles/32273/315655/primer-trailer-de-dune-parte-dos-original.jpg', 'ACTIVA'),
    (2, 'Interestelar',
     'Un grupo de exploradores busca un nuevo hogar para la humanidad.',
     '2H 49M', 'MAYORES_7', 'CIENCIA_FICCION',
     'https://s3.amazonaws.com/nightjarprod/content/uploads/sites/130/2021/08/19085635/gEU2QniE6E77NI6lCU6MxlNBvIx-scaled.jpg', 'ACTIVA'),
    (3, 'Coco',
     'Miguel viaja al mundo de los muertos para descubrir su historia.',
     '1H 45M', 'TODO_PUBLICO', 'ANIMACION',
     'https://static1.moviewebimages.com/wordpress/wp-content/uploads/sharedimages/2024/04/coco-movie-poster.jpg', 'ACTIVA'),
    (4, 'El padrino',
     'La historia de la familia Corleone y su control del crimen.',
     '2H 55M', 'MAYORES_15', 'DRAMA',
     'https://vignette.wikia.nocookie.net/doblaje/images/9/9a/Elpadrino.jpg/revision/latest?cb=20170311061852&path-prefix=es', 'ACTIVA');

-- =========================================================================
-- 3. SALAS
-- =========================================================================
INSERT INTO salas
    (id_sala, nombre, tipo, precio_boleto, estado)
VALUES
    (1, 'Sala 1', 'DOS_D', 18000.00, 'ACTIVA'),
    (2, 'Sala 2', 'TRES_D', 22000.00, 'ACTIVA'),
    (3, 'Sala IMAX', 'IMAX', 30000.00, 'ACTIVA');

-- =========================================================================
-- 4. FUNCIONES
--    Las fechas son relativas al dia en que se ejecuta este script,
--    igual que hacia LocalDate.now() en el seed original.
-- =========================================================================
INSERT INTO funciones
    (id_funcion, id_pelicula, id_sala, fecha, hora_inicio, estado)
VALUES
    (1, 1, 1, CURDATE(), '18:00:00', 'PROGRAMADA'),
    (2, 1, 2, CURDATE(), '20:30:00', 'PROGRAMADA'),
    (3, 2, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '19:00:00', 'PROGRAMADA'),
    (4, 3, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '16:00:00', 'PROGRAMADA'),
    (5, 4, 2, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '21:00:00', 'PROGRAMADA');

-- =========================================================================
-- 5. PRODUCTOS DE CONFITERIA
-- =========================================================================
INSERT INTO productos_confiteria
    (id_producto, nombre, descripcion, precio, imagen_url, categoria, estado)
VALUES
    (1, 'Crispetas grandes', 'Crispetas con mantequilla',
     12000.00, 'https://img.freepik.com/fotos-premium/palomitas-maiz-fondo-blanco_693425-8919.jpg', 'SNACKS', 'ACTIVO'),
    (2, 'Crispetas medianas', 'Crispetas con mantequilla',
     9000.00, 'https://img.freepik.com/fotos-premium/palomitas-maiz-fondo-blanco_693425-8919.jpg', 'SNACKS', 'ACTIVO'),
    (3, 'Gaseosa', 'Bebida fría de 500 ml',
     6000.00, 'https://i.pinimg.com/736x/07/88/0f/07880fc65af204222058eab172a07061.jpg', 'BEBIDAS', 'ACTIVO'),
    (4, 'Perro caliente', 'Perro caliente con papas',
     10000.00, 'https://img.freepik.com/foto-gratis/perro-caliente-comida-colombiana-papas-fritas_383153-73.jpg?w=2000', 'SNACKS', 'ACTIVO'),
    (5, 'Chocolatina', 'Chocolatina de leche',
     4500.00, 'https://lh3.googleusercontent.com/rur48nerMKaAiqFCdwT8CIJPDSKYPxEDIKrHbWeTmUXUh_CwmdVoo1IMpjUVOsszBVM5k4wGj5Y0iQI0TSpJSAz6uTCHDeLI7z52JohF38qfE6k', 'DULCES', 'ACTIVO');

-- =========================================================================
-- 6. FACTURAS
--    Ambas son de Laura (id_usuario = 2), emitidas al ejecutar el script.
-- =========================================================================
INSERT INTO facturas
    (id_factura, id_usuario, fecha_hora, metodo_pago, nombre_cliente,
     correo_cliente, estado, fecha_anulacion, version)
VALUES
    (1, 2, NOW(6), 'EFECTIVO', 'Cliente de mostrador',
     'mostrador@example.com', 'EMITIDA', NULL, 0),
    (2, 2, NOW(6), 'TARJETA', 'Cliente web',
     'web@example.com', 'EMITIDA', NULL, 0);

-- =========================================================================
-- 7. DETALLES DE BOLETO
--    Factura 1: Dune, funcion 1, Sala 1 (18000.00), asientos A1 y A2.
--    Factura 2: Interestelar, funcion 3, Sala 1 (18000.00), asiento B1.
-- =========================================================================
INSERT INTO detalles_boleto
    (id_detalle_boleto, id_factura, id_funcion, codigo_asiento,
     asiento_activo, precio_unitario)
VALUES
    (1, 1, 1, 'A1', 'A1', 18000.00),
    (2, 1, 1, 'A2', 'A2', 18000.00),
    (3, 2, 3, 'B1', 'B1', 18000.00);

-- =========================================================================
-- 8. DETALLES DE CONFITERIA
--    Factura 1: Crispetas grandes x1 (12000.00) y Gaseosa x2 (6000.00).
--    Factura 2: Perro caliente x1 (10000.00).
-- =========================================================================
INSERT INTO detalles_confiteria
    (id_detalle_confiteria, id_factura, id_producto, cantidad, precio_unitario)
VALUES
    (1, 1, 1, 1, 12000.00),
    (2, 1, 3, 2, 6000.00),
    (3, 2, 4, 1, 10000.00);

COMMIT;

-- =========================================================================
-- VERIFICACION
-- =========================================================================
SELECT 'usuarios' AS tabla, COUNT(*) AS filas FROM usuarios
UNION ALL
SELECT 'peliculas', COUNT(*) FROM peliculas
UNION ALL
SELECT 'salas', COUNT(*) FROM salas
UNION ALL
SELECT 'funciones', COUNT(*) FROM funciones
UNION ALL
SELECT 'productos_confiteria', COUNT(*) FROM productos_confiteria
UNION ALL
SELECT 'facturas', COUNT(*) FROM facturas
UNION ALL
SELECT 'detalles_boleto', COUNT(*) FROM detalles_boleto
UNION ALL
SELECT 'detalles_confiteria', COUNT(*) FROM detalles_confiteria;
