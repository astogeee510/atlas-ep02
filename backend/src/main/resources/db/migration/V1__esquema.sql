-- ============================================================================
-- Atlas - esquema inicial (PostgreSQL)
-- Trazabilidad: RF-1..RF-6, RB-1..RB-5, NFR-SEG-3 (columnas cifradas)
-- ============================================================================

CREATE TABLE usuario (
    id                   BIGSERIAL PRIMARY KEY,
    nombre_usuario       VARCHAR(50)  NOT NULL UNIQUE,
    correo               VARCHAR(120) NOT NULL UNIQUE,
    password_hash        VARCHAR(100) NOT NULL,
    activo               BOOLEAN      NOT NULL DEFAULT TRUE,
    consentimiento_datos BOOLEAN      NOT NULL DEFAULT FALSE,
    creado_en            TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- RB-1: un mismo correo principal no puede existir en dos empresas
CREATE TABLE empresa (
    id        BIGSERIAL PRIMARY KEY,
    nombre    VARCHAR(120) NOT NULL,
    direccion VARCHAR(200),
    telefono  VARCHAR(20),
    correo    VARCHAR(120) NOT NULL,
    creado_en TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX ux_empresa_correo ON empresa (LOWER(correo));

-- RB-2: solo usuarios con relacion en empresa_usuario ven o editan datos
CREATE TABLE empresa_usuario (
    id         BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT      NOT NULL REFERENCES empresa (id),
    usuario_id BIGINT      NOT NULL REFERENCES usuario (id),
    rol        VARCHAR(10) NOT NULL CHECK (rol IN ('ADMIN', 'EDITOR')),
    CONSTRAINT ux_empresa_usuario UNIQUE (empresa_id, usuario_id)
);

-- RF-4: clientes; datos bancarios y de contacto cifrados en reposo (AES-256-GCM)
CREATE TABLE cliente (
    id                BIGSERIAL PRIMARY KEY,
    empresa_id        BIGINT       NOT NULL REFERENCES empresa (id),
    nombre            VARCHAR(120) NOT NULL,
    rut               VARCHAR(12),
    descripcion       VARCHAR(500),
    direccion         VARCHAR(200),
    correo_contacto   VARCHAR(512),
    telefono_contacto VARCHAR(512),
    banco             VARCHAR(512),
    numero_cuenta     VARCHAR(512),
    eliminado         BOOLEAN      NOT NULL DEFAULT FALSE,
    eliminado_en      TIMESTAMPTZ,
    creado_en         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    actualizado_en    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_cliente_empresa_nombre ON cliente (empresa_id, nombre);

-- RF-5 / RB-3: un contrato se asocia unicamente a un cliente
CREATE TABLE contrato (
    id            BIGSERIAL PRIMARY KEY,
    empresa_id    BIGINT       NOT NULL REFERENCES empresa (id),
    cliente_id    BIGINT       NOT NULL REFERENCES cliente (id),
    titulo        VARCHAR(150) NOT NULL,
    descripcion   VARCHAR(1000),
    fecha_inicio  DATE         NOT NULL,
    fecha_termino DATE         NOT NULL,
    eliminado     BOOLEAN      NOT NULL DEFAULT FALSE,
    creado_en     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_contrato_fechas CHECK (fecha_termino >= fecha_inicio)
);
CREATE INDEX ix_contrato_cliente ON contrato (cliente_id);

-- RF-5.2 / RF-5.3: un documento por contrato, guardado en storage interno
CREATE TABLE documento (
    id                  BIGSERIAL PRIMARY KEY,
    contrato_id         BIGINT       NOT NULL UNIQUE REFERENCES contrato (id),
    nombre_original     VARCHAR(255) NOT NULL,
    tipo_contenido      VARCHAR(120) NOT NULL,
    tamano_bytes        BIGINT       NOT NULL,
    ruta_almacenamiento VARCHAR(255) NOT NULL,
    sha256              VARCHAR(64)  NOT NULL,
    subido_por          BIGINT       NOT NULL REFERENCES usuario (id),
    subido_en           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- RF-5.5: enlaces para compartir documentos
CREATE TABLE enlace_compartido (
    id           BIGSERIAL PRIMARY KEY,
    documento_id BIGINT      NOT NULL REFERENCES documento (id) ON DELETE CASCADE,
    token        VARCHAR(64) NOT NULL UNIQUE,
    expira_en    TIMESTAMPTZ NOT NULL,
    revocado     BOOLEAN     NOT NULL DEFAULT FALSE,
    creado_por   BIGINT      NOT NULL REFERENCES usuario (id),
    creado_en    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- RF-1.2: revocacion de tokens (logout)
CREATE TABLE token_revocado (
    jti       VARCHAR(64) PRIMARY KEY,
    expira_en TIMESTAMPTZ NOT NULL
);

-- RF-6.1: auditoria de acciones criticas
CREATE TABLE auditoria (
    id         BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT,
    usuario_id BIGINT,
    accion     VARCHAR(40) NOT NULL,
    entidad    VARCHAR(40) NOT NULL,
    entidad_id BIGINT,
    detalle    VARCHAR(255),
    fecha      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_auditoria_empresa_fecha ON auditoria (empresa_id, fecha DESC);
