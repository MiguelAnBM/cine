-- =========================================================================
-- ESQUEMA: cinecomfenalco_schema.sql
-- Requiere MySQL 8.0.16+ para que se apliquen los CHECK.
-- ⚠️ ENGINE no se puede fijar a nivel de esquema; verifica que el servidor
--    tenga default-storage-engine=InnoDB antes de ejecutar este script:
--       SELECT @@default_storage_engine;
-- =========================================================================

-- Configuración del esquema: charset y collation se heredan en tablas NUEVAS.
CREATE DATABASE IF NOT EXISTS cinecomfenalco
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE cinecomfenalco;

-- =========================================================================
-- 1. TABLA USUARIO
-- =========================================================================
CREATE TABLE usuarios (
    id_usuario      BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(100) NOT NULL,
    username        VARCHAR(50)  NOT NULL,
    rol             ENUM('TAQUILLERO','ADMINISTRADOR') NOT NULL,
    contrasena_hash VARCHAR(255) NULL,
    estado          ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',

    -- Restricciones

    -- Login único: no puede haber dos usuarios con el mismo username.
    CONSTRAINT uq_usuario_username UNIQUE (username),

    -- Regla de negocio: solo Administrador tiene contraseña; Taquillero no.
    CONSTRAINT chk_usuario_admin_password CHECK (
        (rol = 'ADMINISTRADOR' AND contrasena_hash IS NOT NULL) OR
        (rol = 'TAQUILLERO'   AND contrasena_hash IS NULL)
    )
);

-- =========================================================================
-- 2. TABLA PELICULA
-- =========================================================================
CREATE TABLE peliculas (
    id_pelicula   BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo        VARCHAR(150) NOT NULL,
    sinopsis      TEXT NOT NULL,
    duracion      VARCHAR(6) NOT NULL,
    clasificacion ENUM('TODO_PUBLICO','MAYORES_7','MAYORES_15','MAYORES_18') NOT NULL,
    genero        ENUM('ACCION', 'ANIMACION', 'AVENTURA', 'CIENCIA_FICCION', 
                        'COMEDIA', 'DRAMA', 'FANTASIA', 'TERROR', 'SUSPENSO', 
                        'ROMANCE', 'DOCUMENTAL', 'MUSICAL') NOT NULL,
    imagen_url    VARCHAR(2000),
    estado        ENUM('ACTIVA','INACTIVA') NOT NULL DEFAULT 'ACTIVA',

    -- Restricciones

    -- La duración debe ser positiva y con el patrón correcto: "2H 30M" (no existen películas de 0 o menos minutos).
    CONSTRAINT chk_pelicula_duracion CHECK (
        REGEXP_LIKE(duracion, '^[0-9]H [0-5]?[0-9]M$', 'c')
        AND NOT REGEXP_LIKE(duracion, '^0H 0?0M$', 'c')
    )
);

-- =========================================================================
-- 3. TABLA SALA
-- =========================================================================
CREATE TABLE salas (
    id_sala        BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(50) NOT NULL,
    tipo           ENUM('DOS_D','TRES_D','IMAX') NOT NULL,
    precio_boleto  DECIMAL(10,2) NOT NULL,
    estado         ENUM('ACTIVA','INACTIVA') NOT NULL DEFAULT 'ACTIVA',

    -- Restricciones

    -- No puede haber dos salas con el mismo número/nombre.
    CONSTRAINT uq_sala_nombre UNIQUE (nombre),

    -- El precio base (con recargo por tipo ya incluido) no puede ser negativo.
    CONSTRAINT chk_sala_precio CHECK (precio_boleto >= 0)
);

-- =========================================================================
-- 4. TABLA FUNCION
-- =========================================================================
CREATE TABLE funciones (
    id_funcion  BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_pelicula BIGINT NOT NULL,
    id_sala     BIGINT NOT NULL,
    fecha       DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    estado      ENUM('PROGRAMADA','EN_CURSO','FINALIZADA','CANCELADA')
                NOT NULL DEFAULT 'PROGRAMADA',
    
    -- Índices

    INDEX idx_funcion_fecha_hora (fecha, hora_inicio),
    INDEX idx_funcion_pelicula_fecha (id_pelicula, fecha),

    -- Restricciones

    -- Impide programar dos funciones en la misma sala, mismo día y misma hora.
    CONSTRAINT uq_funcion_sala_fecha_hora UNIQUE (id_sala, fecha, hora_inicio),

    -- Toda función debe apuntar a una película existente; no se puede borrar
    -- una película que tenga funciones programadas.
    CONSTRAINT fk_funcion_pelicula FOREIGN KEY (id_pelicula)
        REFERENCES peliculas(id_pelicula) ON DELETE RESTRICT ON UPDATE CASCADE,

    -- Toda función debe apuntar a una sala existente; no se puede borrar
    -- una sala que tenga funciones programadas.
    CONSTRAINT fk_funcion_sala FOREIGN KEY (id_sala)
        REFERENCES salas(id_sala) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- =========================================================================
-- 5. TABLA PRODUCTO_CONFITERIA
-- =========================================================================
CREATE TABLE productos_confiteria (
    id_producto BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio      DECIMAL(10,2) NOT NULL,
    imagen_url  VARCHAR(500),
    estado      ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',

    -- Restricciones

    -- No puede haber dos productos con el mismo nombre en el catálogo.
    CONSTRAINT uq_producto_nombre UNIQUE (nombre),

    -- El precio del producto no puede ser negativo.
    CONSTRAINT chk_producto_precio CHECK (precio >= 0)
);

-- =========================================================================
-- 6. TABLA FACTURA
--    fecha_hora es el dato de negocio para reportes (7 días, mes, etc.).
--    NO se añade created_at: sería redundante con fecha_hora.
-- =========================================================================
CREATE TABLE facturas (
    id_factura     BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario     BIGINT NOT NULL,
    fecha_hora     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metodo_pago    ENUM('EFECTIVO','TARJETA') NOT NULL,
    nombre_cliente VARCHAR(100) NULL,
    correo_cliente VARCHAR(150) NULL,

    -- Índices

    INDEX idx_factura_fecha (fecha_hora),
    INDEX idx_factura_usuario_fecha (id_usuario, fecha_hora),

    -- Restricciones

    -- Toda factura debe estar asociada a un usuario que la registró;
    -- no se puede borrar un usuario que tenga facturas.
    CONSTRAINT fk_factura_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- =========================================================================
-- 7. TABLA DETALLE_BOLETO
-- =========================================================================
CREATE TABLE detalles_boleto (
    id_detalle_boleto BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_factura        BIGINT NOT NULL,
    id_funcion        BIGINT NOT NULL,
    codigo_asiento    VARCHAR(3) NOT NULL,
    precio_unitario   DECIMAL(10,2) NOT NULL,

    -- Restricciones

    -- Impide vender el mismo asiento dos veces en la misma función.
    CONSTRAINT uq_boleto_funcion_asiento UNIQUE (id_funcion, codigo_asiento),

    -- El precio del boleto no puede ser negativo.
    CONSTRAINT chk_boleto_precio CHECK (precio_unitario >= 0),

    -- No se puede borrar una factura que tenga boletos.
    CONSTRAINT fk_detboleto_factura FOREIGN KEY (id_factura)
        REFERENCES facturas(id_factura) ON DELETE RESTRICT ON UPDATE CASCADE,

    -- Todo boleto debe apuntar a una función existente; no se puede borrar
    -- una función que tenga boletos vendidos.
    CONSTRAINT fk_detboleto_funcion FOREIGN KEY (id_funcion)
        REFERENCES funciones(id_funcion) ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT chk_boleto_asiento CHECK (REGEXP_LIKE(codigo_asiento, '^[A-H]([1-9]|10)$', 'c'))
);

-- =========================================================================
-- 8. TABLA DETALLE_CONFITERIA
-- =========================================================================
CREATE TABLE detalles_confiteria (
    id_detalle_confiteria BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_factura            BIGINT NOT NULL,
    id_producto           BIGINT NOT NULL,
    cantidad              INT NOT NULL,
    precio_unitario       DECIMAL(10,2) NOT NULL,
    subtotal              DECIMAL(10,2) AS (cantidad * precio_unitario) STORED,

    -- Restricciones

    -- Consolida una sola línea por producto en cada factura.
    CONSTRAINT uq_conf_factura_producto UNIQUE (id_factura, id_producto),

    -- La cantidad comprada debe ser al menos 1.
    CONSTRAINT chk_conf_cantidad CHECK (cantidad > 0),

    -- El precio unitario del producto no puede ser negativo.
    CONSTRAINT chk_conf_precio CHECK (precio_unitario >= 0),

    -- No se puede borrar una factura que tenga detalles de confitería.
    CONSTRAINT fk_detconf_factura FOREIGN KEY (id_factura)
        REFERENCES facturas(id_factura) ON DELETE RESTRICT ON UPDATE CASCADE,

    -- Todo detalle debe apuntar a un producto existente; no se puede borrar
    -- un producto que tenga ventas registradas.
    CONSTRAINT fk_detconf_producto FOREIGN KEY (id_producto)
        REFERENCES productos_confiteria(id_producto) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- =========================================================================
-- VISTA: vw_factura_total
-- Total de cada factura = boletos + confitería. No almacena datos: suma los
-- precios guardados en los detalles, no los precios actuales del catálogo.
-- =========================================================================
CREATE OR REPLACE VIEW vw_factura_total AS
SELECT
    f.id_factura,
    f.id_usuario,
    f.fecha_hora,
    f.metodo_pago,
    f.nombre_cliente,
    f.correo_cliente,
    COALESCE(b.total_boletos, 0)                                   AS total_boletos,
    COALESCE(c.total_confiteria, 0)                                AS total_confiteria,
    COALESCE(b.total_boletos, 0) + COALESCE(c.total_confiteria, 0) AS total_factura
FROM facturas f
LEFT JOIN (
    SELECT id_factura, SUM(precio_unitario) AS total_boletos
    FROM detalles_boleto
    GROUP BY id_factura
) b ON b.id_factura = f.id_factura
LEFT JOIN (
    SELECT id_factura, SUM(subtotal) AS total_confiteria
    FROM detalles_confiteria
    GROUP BY id_factura
) c ON c.id_factura = f.id_factura;

-- =========================================================================
-- VISTA: vw_asientos_funcion
-- Los 80 asientos (filas A-H x columnas 1-10) de cada función.
-- ocupado = 1 si el asiento ya tiene un boleto vendido, 0 si está libre.
-- =========================================================================
CREATE OR REPLACE VIEW vw_asientos_funcion AS
SELECT
    f.id_funcion,
    f.id_sala,
    a.codigo_asiento,
    CASE WHEN b.id_detalle_boleto IS NULL THEN 0 ELSE 1 END AS ocupado
FROM funciones f
CROSS JOIN (
    SELECT CONCAT(fi.fila, n.numero) AS codigo_asiento
    FROM (SELECT 'A' AS fila UNION ALL SELECT 'B' UNION ALL SELECT 'C'
          UNION ALL SELECT 'D' UNION ALL SELECT 'E' UNION ALL SELECT 'F'
          UNION ALL SELECT 'G' UNION ALL SELECT 'H') fi
    CROSS JOIN (SELECT 1 AS numero UNION ALL SELECT 2 UNION ALL SELECT 3
                UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6
                UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
                UNION ALL SELECT 10) n
) a
LEFT JOIN detalles_boleto b
       ON b.id_funcion = f.id_funcion
      AND b.codigo_asiento = a.codigo_asiento;

-- =========================================================================
-- Mapa completo de una función, ordenado como una cuadrícula
-- =========================================================================
-- SELECT codigo_asiento, ocupado
-- FROM vw_asientos_funcion
-- WHERE id_funcion = 1
-- ORDER BY LEFT(codigo_asiento, 1), CAST(SUBSTRING(codigo_asiento, 2) AS UNSIGNED);

-- =========================================================================
-- Ventas de la última semana
-- =========================================================================
-- SELECT id_factura, fecha_hora, total_boletos, total_confiteria, total_factura
-- FROM vw_factura_total
-- WHERE fecha_hora >= NOW() - INTERVAL 7 DAY
-- ORDER BY fecha_hora;