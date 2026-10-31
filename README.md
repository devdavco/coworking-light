# Coworking Booking System

Sistema de reserva de espacios de coworking: backend Spring Boot, frontend Angular y PostgreSQL.

- **Clientes**: se registran, consultan espacios y su disponibilidad, reservan y cancelan sus reservas.
- **Administradores**: gestionan espacios (CRUD) y ven / cancelan todas las reservas.

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 21, Spring Boot 3.3, Spring Security (JWT HS256), Spring Data JPA, Flyway |
| Frontend | Angular 17 |
| Base de datos | PostgreSQL 16 |
| Infraestructura | Docker, Docker Compose, nginx, GitHub Actions |

## Estructura

```
coworking-booking-kiro/
├── backend/                 # API REST Spring Boot
├── frontend/                # SPA Angular (servida por nginx, que hace proxy de /api)
├── docker-compose.yml       # Entorno local
├── docker-compose.prod.yml  # Producción (EC2)
├── .env.example             # Variables para producción
├── .github/workflows/ci.yml # CI: tests + build
├── ARCHITECTURE.md          # Diseño
├── DEPLOYMENT.md            # Despliegue en AWS
└── SPEC_AUTH.md             # Especificación de autenticación
```

## Arrancar en local

### Opción A: todo con Docker (recomendada)

Requisito: Docker.

```bash
docker compose up --build
```

- App: http://localhost
- API: http://localhost:8080/api · Swagger: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

Usuario admin de desarrollo: `admin@coworking.com` / `admin123`
(los clientes se crean desde la pantalla de registro).

Para empezar con la base de datos vacía: `docker compose down -v`.

### Opción B: desarrollo con recarga en caliente

Requisitos: Java 21, Maven 3.9+, Node 20.

```bash
docker compose up -d postgres            # solo la base de datos

cd backend && mvn spring-boot:run        # API en :8080 (perfil dev)
cd frontend && npm ci && npm start       # SPA en http://localhost:4200
```

## Tests

```bash
cd backend && mvn test                   # JUnit 5 + Mockito + MockMvc
cd frontend && npm run test:ci           # Karma (Chrome headless)
```

## Despliegue

Ver [DEPLOYMENT.md](DEPLOYMENT.md): EC2 + Docker Compose, con opciones para RDS, HTTPS y ECS.

---

## API

Todas las respuestas siguen el mismo formato:

```json
{ "success": true, "message": "…", "data": { … }, "timestamp": "…" }
```

En caso de error `success` es `false`; los errores de validación detallan cada campo en `data`.
Salvo `/api/auth/**`, todos los endpoints requieren `Authorization: Bearer <token>`.

### Autenticación — ver [SPEC_AUTH.md](SPEC_AUTH.md)

| Método | Ruta | Descripción | Códigos |
|---|---|---|---|
| POST | `/api/auth/register` | Crea un cliente y devuelve token | 201, 400, 409 |
| POST | `/api/auth/login` | Devuelve token + usuario | 200, 400, 401 |

### Espacios

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| GET | `/api/espacios?page=0&size=10&tipo=OFICINA` | Cualquiera | Listado paginado (`size` máx. 100) |
| GET | `/api/espacios/{id}` | Cualquiera | Detalle |
| GET | `/api/espacios/{id}/disponibilidad?fecha=YYYY-MM-DD` | Cualquiera | Franjas libres del día |
| POST | `/api/espacios` | ADMIN | Crear |
| PUT | `/api/espacios/{id}` | ADMIN | Actualizar |
| DELETE | `/api/espacios/{id}` | ADMIN | Eliminar (solo si no tiene reservas) |

`tipo`: `OFICINA`, `SALA_REUNIONES`, `COWORKING_ABIERTO`.

```json
{ "nombre": "Sala Azul", "descripcion": "…", "tipo": "SALA_REUNIONES", "capacidad": 8,
  "horarioApertura": "07:00", "horarioCierre": "22:00" }
```

### Reservas

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/reservas?page=0&size=10` | CLIENT: sus reservas · ADMIN: todas |
| POST | `/api/reservas` | Crear reserva para el usuario autenticado |
| DELETE | `/api/reservas/{id}` | Cancelar (propia, o cualquiera si ADMIN) |

```json
{ "espacioId": 1, "fecha": "2026-11-05", "horaInicio": "09:00", "horaFin": "11:00" }
```

Reglas de negocio:
- Duración entre **1 y 8 horas**, dentro del horario del espacio.
- No se puede reservar en el pasado (según `APP_TIMEZONE`).
- No puede solaparse con otra reserva confirmada del mismo espacio (protegido también
  frente a peticiones concurrentes con un bloqueo de fila).
- Cancelar marca la reserva como `CANCELADA` (no se borra).

### Códigos de error

| Código | Cuándo |
|---|---|
| 400 | Validación, regla de negocio o cuerpo/parámetro mal formado |
| 401 | Sin token, token inválido/expirado o credenciales incorrectas |
| 403 | Autenticado sin permisos (p.ej. CLIENT intentando crear espacios) |
| 404 | Recurso inexistente |
| 409 | Email o nombre de espacio duplicado |
