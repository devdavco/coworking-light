-- Las entidades JPA usan Long (BIGINT); V1 creó las columnas como INTEGER (SERIAL),
-- lo que hace fallar la validación de Hibernate (ddl-auto: validate).
ALTER TABLE users    ALTER COLUMN id TYPE BIGINT;
ALTER TABLE espacios ALTER COLUMN id TYPE BIGINT;
ALTER TABLE reservas ALTER COLUMN id TYPE BIGINT;
ALTER TABLE reservas ALTER COLUMN usuario_id TYPE BIGINT;
ALTER TABLE reservas ALTER COLUMN espacio_id TYPE BIGINT;

ALTER SEQUENCE users_id_seq    AS BIGINT;
ALTER SEQUENCE espacios_id_seq AS BIGINT;
ALTER SEQUENCE reservas_id_seq AS BIGINT;

-- idx_users_email es redundante: la constraint UNIQUE ya crea un índice
DROP INDEX IF EXISTS idx_users_email;
