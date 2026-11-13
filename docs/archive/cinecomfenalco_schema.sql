-- =========================================================================
-- ESQUEMA: cinecomfenalco_schema.sql
--
-- Requiere MySQL 8.0.16+ para que se apliquen los CHECK.
-- Verifica el motor por defecto:
--    SELECT @@default_storage_engine;
--
-- Base usada por application.properties:
--   cinecomfenalco_db
-- =========================================================================

CREATE DATABASE IF NOT EXISTS cinecomfenalco_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE cinecomfenalco_db;

SET NAMES utf8mb4;

-- =========================================================================
-- 1. DETALLES DE BOLETO
--    Hibernate crea la tabla antes que sus tablas padre y agrega las FK
--    al final, mediante ALTER TABLE.
-- =========================================================================
CREATE TABLE detalles_boleto (
    asiento_activo    VARCHAR(3),
    codigo_asiento    VARCHAR(3)    NOT NULL,
    precio_unitario   DECIMAL(10,2) NOT NULL,
    id_detalle_boleto BIGINT        NOT NULL AUTO_INCREMENT,
    id_factura        BIGINT        NOT NULL,
    id_funcion        BIGINT        NOT NULL,
    PRIMARY KEY (id_detalle_boleto),
    CONSTRAINT chk_boleto_precio CHECK (precio_unitario >= 0),
    CONSTRAINT chk_boleto_asiento CHECK (
        REGEXP_LIKE(codigo_asiento, '^[A-H]([1-9]|10)$', 'c')
    ),
    CONSTRAINT chk_boleto_asiento_activo CHECK (
        asiento_activo IS NULL OR asiento_activo = codigo_asiento
    )
) ENGINE=InnoDB;

-- =========================================================================
-- 2. DETALLES DE CONFITERIA
--    Hibernate agrega dos CHECK de cantidad: uno implicito por @Min y el
--    nombrado por @CheckConstraint. Se conservan ambos.
-- =========================================================================
CREATE TABLE detalles_confiteria (
    cantidad              INT           NOT NULL,
    precio_unitario       DECIMAL(10,2) NOT NULL,
    id_detalle_confiteria BIGINT        NOT NULL AUTO_INCREMENT,
    id_factura            BIGINT        NOT NULL,
    id_producto           BIGINT        NOT NULL,
    PRIMARY KEY (id_detalle_confiteria),
    CHECK ((cantidad >= 1)),
    CONSTRAINT chk_conf_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_conf_precio CHECK (precio_unitario >= 0)
) ENGINE=InnoDB;

-- =========================================================================
-- 3. FACTURAS
--    version es la columna de bloqueo optimista de @Version.
-- =========================================================================
CREATE TABLE facturas (
    fecha_anulacion DATETIME(6),
    fecha_hora      DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    id_factura      BIGINT      NOT NULL AUTO_INCREMENT,
    id_usuario      BIGINT      NOT NULL,
    version         BIGINT      NOT NULL,
    nombre_cliente  VARCHAR(100),
    correo_cliente  VARCHAR(150),
    estado          ENUM('ANULADA','EMITIDA') DEFAULT 'EMITIDA' NOT NULL,
    metodo_pago     ENUM('EFECTIVO','TARJETA') NOT NULL,
    PRIMARY KEY (id_factura),
    CONSTRAINT chk_factura_anulacion CHECK (
        (estado = 'EMITIDA' AND fecha_anulacion IS NULL) OR
        (estado = 'ANULADA' AND fecha_anulacion IS NOT NULL)
    )
) ENGINE=InnoDB;

-- =========================================================================
-- 4. FUNCIONES
-- =========================================================================
CREATE TABLE funciones (
    fecha       DATE    NOT NULL,
    hora_inicio TIME(0) NOT NULL,
    id_funcion  BIGINT  NOT NULL AUTO_INCREMENT,
    id_pelicula BIGINT  NOT NULL,
    id_sala     BIGINT  NOT NULL,
    estado      ENUM('CANCELADA','EN_CURSO','FINALIZADA','PROGRAMADA')
                DEFAULT 'PROGRAMADA' NOT NULL,
    PRIMARY KEY (id_funcion)
) ENGINE=InnoDB;

-- =========================================================================
-- 5. PELICULAS
-- =========================================================================
CREATE TABLE peliculas (
    duracion      VARCHAR(6)   NOT NULL,
    id_pelicula   BIGINT       NOT NULL AUTO_INCREMENT,
    titulo        VARCHAR(150) NOT NULL,
    imagen_url    VARCHAR(2000),
    sinopsis      TEXT         NOT NULL,
    clasificacion ENUM('MAYORES_15','MAYORES_18','MAYORES_7','TODO_PUBLICO') NOT NULL,
    estado        ENUM('ACTIVA','INACTIVA') DEFAULT 'ACTIVA' NOT NULL,
    genero        ENUM('ACCION','ANIMACION','AVENTURA','CIENCIA_FICCION',
                       'COMEDIA','DOCUMENTAL','DRAMA','FANTASIA','MUSICAL',
                       'ROMANCE','SUSPENSO','TERROR') NOT NULL,
    PRIMARY KEY (id_pelicula),
    CONSTRAINT chk_pelicula_duracion CHECK (
        REGEXP_LIKE(duracion, '^[0-9]H [0-5]?[0-9]M$', 'c')
        AND NOT REGEXP_LIKE(duracion, '^0H 0?0M$', 'c')
    )
) ENGINE=InnoDB;

-- =========================================================================
-- 6. PRODUCTOS DE CONFITERIA
-- =========================================================================
CREATE TABLE productos_confiteria (
    precio      DECIMAL(10,2) NOT NULL,
    id_producto BIGINT        NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(100)  NOT NULL,
    imagen_url  VARCHAR(500),
    descripcion TEXT,
    categoria   ENUM('BEBIDAS','COMBOS','DULCES','SNACKS') NOT NULL,
    estado      ENUM('ACTIVO','INACTIVO') DEFAULT 'ACTIVO' NOT NULL,
    PRIMARY KEY (id_producto),
    CONSTRAINT chk_producto_precio CHECK (precio >= 0)
) ENGINE=InnoDB;

-- =========================================================================
-- 7. SALAS
-- =========================================================================
CREATE TABLE salas (
    precio_boleto DECIMAL(10,2) NOT NULL,
    id_sala       BIGINT        NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(50)   NOT NULL,
    estado        ENUM('ACTIVA','INACTIVA') DEFAULT 'ACTIVA' NOT NULL,
    tipo          ENUM('DOS_D','IMAX','TRES_D') NOT NULL,
    PRIMARY KEY (id_sala),
    CONSTRAINT chk_sala_precio CHECK (precio_boleto >= 0)
) ENGINE=InnoDB;

-- =========================================================================
-- 8. USUARIOS
-- =========================================================================
CREATE TABLE usuarios (
    id_usuario      BIGINT       NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)  NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    contrasena_hash VARCHAR(255),
    estado          ENUM('ACTIVO','INACTIVO') DEFAULT 'ACTIVO' NOT NULL,
    rol             ENUM('ADMINISTRADOR','TAQUILLERO') NOT NULL,
    PRIMARY KEY (id_usuario),
    CONSTRAINT chk_usuario_admin_password CHECK (
        (rol = 'ADMINISTRADOR' AND contrasena_hash IS NOT NULL) OR
        (rol = 'TAQUILLERO' AND contrasena_hash IS NULL)
    )
) ENGINE=InnoDB;

-- =========================================================================
-- RESTRICCIONES UNICAS
-- =========================================================================
ALTER TABLE detalles_boleto
    ADD CONSTRAINT uq_boleto_funcion_asiento UNIQUE (id_funcion, asiento_activo);

ALTER TABLE detalles_confiteria
    ADD CONSTRAINT uq_conf_factura_producto UNIQUE (id_factura, id_producto);

ALTER TABLE peliculas
    ADD CONSTRAINT uq_pelicula_titulo UNIQUE (titulo);

ALTER TABLE productos_confiteria
    ADD CONSTRAINT uq_producto_nombre UNIQUE (nombre);

ALTER TABLE salas
    ADD CONSTRAINT uq_sala_nombre UNIQUE (nombre);

ALTER TABLE usuarios
    ADD CONSTRAINT uq_usuario_username UNIQUE (username);

-- =========================================================================
-- INDICES
-- =========================================================================
CREATE INDEX idx_factura_fecha
    ON facturas (fecha_hora);

CREATE INDEX idx_factura_usuario_fecha
    ON facturas (id_usuario, fecha_hora);

CREATE INDEX idx_funcion_fecha_hora
    ON funciones (fecha, hora_inicio);

CREATE INDEX idx_funcion_pelicula_fecha
    ON funciones (id_pelicula, fecha);

-- =========================================================================
-- CLAVES FORANEAS
-- =========================================================================
ALTER TABLE detalles_boleto
    ADD CONSTRAINT fk_detboleto_factura FOREIGN KEY (id_factura)
        REFERENCES facturas (id_factura)
        ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE detalles_boleto
    ADD CONSTRAINT fk_detboleto_funcion FOREIGN KEY (id_funcion)
        REFERENCES funciones (id_funcion)
        ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE detalles_confiteria
    ADD CONSTRAINT fk_detconf_factura FOREIGN KEY (id_factura)
        REFERENCES facturas (id_factura)
        ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE detalles_confiteria
    ADD CONSTRAINT fk_detconf_producto FOREIGN KEY (id_producto)
        REFERENCES productos_confiteria (id_producto)
        ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE facturas
    ADD CONSTRAINT fk_factura_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios (id_usuario)
        ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE funciones
    ADD CONSTRAINT fk_funcion_pelicula FOREIGN KEY (id_pelicula)
        REFERENCES peliculas (id_pelicula)
        ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE funciones
    ADD CONSTRAINT fk_funcion_sala FOREIGN KEY (id_sala)
        REFERENCES salas (id_sala)
        ON DELETE RESTRICT ON UPDATE CASCADE;
