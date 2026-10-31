-- Users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CLIENT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_role CHECK (role IN ('ADMIN', 'CLIENT'))
);

-- Espacios table
CREATE TABLE espacios (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL UNIQUE,
    descripcion TEXT,
    tipo VARCHAR(50) NOT NULL,
    capacidad INTEGER NOT NULL,
    horario_apertura TIME NOT NULL DEFAULT '07:00',
    horario_cierre TIME NOT NULL DEFAULT '22:00',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_tipo CHECK (tipo IN ('OFICINA', 'SALA_REUNIONES', 'COWORKING_ABIERTO')),
    CONSTRAINT check_capacidad CHECK (capacidad > 0)
);

-- Reservas table
CREATE TABLE reservas (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL REFERENCES users(id),
    espacio_id INTEGER NOT NULL REFERENCES espacios(id),
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'CONFIRMADA',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_estado CHECK (estado IN ('CONFIRMADA', 'CANCELADA')),
    CONSTRAINT check_horarios CHECK (hora_inicio < hora_fin)
);

-- Indexes
CREATE INDEX idx_reservas_usuario ON reservas(usuario_id);
CREATE INDEX idx_reservas_espacio ON reservas(espacio_id);
CREATE INDEX idx_reservas_fecha_espacio ON reservas(fecha, espacio_id);
CREATE INDEX idx_users_email ON users(email);
