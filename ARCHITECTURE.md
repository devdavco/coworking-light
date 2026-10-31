# Arquitectura - Coworking Booking System

## Visión General

Sistema modular para reserva de espacios en coworking con autenticación JWT, roles diferenciados y validaciones en tiempo real.

## Componentes Principales

### Backend (Spring Boot)

**Capas:**
- **Entity**: Usuario, Espacio, Reserva (JPA)
- **Repository**: Acceso a datos con queries personalizadas
- **Service**: Lógica de negocio centralizada
- **Controller**: APIs REST
- **Config**: Seguridad JWT, CORS, Swagger, DataLoader
- **Exception**: Manejo centralizado de errores
- **DTO**: Validaciones de entrada/salida

**Flujo Autenticación:**
1. Usuario se registra/loguea en `/api/auth/register` o `/api/auth/login`
2. Backend valida credenciales y retorna JWT
3. Frontend almacena JWT en localStorage
4. JwtInterceptor agrega token a cada request
5. JwtAuthenticationFilter en backend valida token
6. SecurityConfig controla acceso según roles

**Validaciones Críticas:**
- Email único (constraint en BD), normalizado a minúsculas
- Password hasheado con BCrypt
- Duración reserva: 1-8 horas (calculada en minutos)
- Dentro del horario del espacio (por defecto 07:00-22:00) y nunca en el pasado
- No sobreposición de reservas (consulta de solapamiento + bloqueo `SELECT ... FOR UPDATE` del espacio)
- Solo admin puede crear/editar/eliminar espacios (`@PreAuthorize` + `@EnableMethodSecurity`)

### Frontend (Angular)

**Estructura Modular:**
- **Core**: Services (Auth, HTTP), Guards, Interceptors
- **Auth Module**: Login, Register
- **Admin Module**: CRUD Espacios, Ver todas las reservas
- **Client Module**: Ver espacios, Crear/cancelar reservas

**Flujo de Routing:**
```
/auth/login       → Público
/auth/register    → Público
/admin/*          → AuthGuard + RoleGuard (ADMIN)
/client/*         → AuthGuard
**                → /auth/login
```

**Interceptores:**
- JwtInterceptor: Agrega token a headers
- ErrorInterceptor: en un 401 (fuera de /auth) cierra sesión y redirige al login

### Base de Datos (PostgreSQL)

**Tablas:**
- `users` (id, email, password_hash, first_name, last_name, role)
- `espacios` (id, nombre, tipo, capacidad, horario_apertura, horario_cierre)
- `reservas` (id, usuario_id, espacio_id, fecha, hora_inicio, hora_fin, estado)

**Índices:**
- email en users (unique)
- (fecha, espacio_id) en reservas (búsqueda de disponibilidad)

**Migraciones (Flyway):** `V1__init_schema.sql` (esquema), `V2__ids_to_bigint.sql` (ids a BIGINT
para coincidir con las entidades JPA). Hibernate solo valida (`ddl-auto: validate`).

## Flujos de Negocio

### 1. Registro/Login
```
User Input → AuthController → AuthService → JwtUtil → Token Response
```

### 2. Crear Reserva
```
Client Request → ReservaController → ReservaService 
  → Validar (horarios, sobreposición) 
  → ReservaRepository.save()
  → Response
```

### 3. Ver Disponibilidad
```
GET /espacios/{id}/disponibilidad?fecha=...
  → EspacioService.obtenerDisponibilidad()
  → ReservaRepository.findReservasByEspacioAndFecha()
  → Calcular gaps → Response
```

### 4. Cancelar Reserva
```
DELETE /reservas/{id}
  → Verificar propiedad (si es client) o admin
  → Cambiar estado a CANCELADA
  → Response
```

## Decisiones de Diseño

| Decisión | Razón |
|----------|-------|
| JWT en localStorage | Stateless, sencillo. Contrapartida: expuesto a XSS (alternativa: cookie HttpOnly) |
| BCrypt para passwords | Standard, adaptive, seguro |
| Flyway para migrations | Versionado, reproducible |
| DTOs con @Valid | Validación centralizada, claro |
| GlobalExceptionHandler | Respuestas uniformes |
| Monorepo | Desarrollo integrado, fácil |
| Docker desde inicio | Deployment consistente |

## Seguridad

- En producción el frontend y la API comparten origen (nginx hace proxy de `/api`), así que no hace falta CORS.
  Para `ng serve` se permite `CORS_ALLOWED_ORIGINS` (por defecto `http://localhost:4200`).
- CSRF deshabilitado (API stateless con JWT en cabecera, sin cookies)
- SessionCreationPolicy.STATELESS
- 401 JSON si falta el token o no es válido; 403 JSON si el rol no basta
- Secretos (`JWT_SECRET`, credenciales BD, `ADMIN_PASSWORD`) solo por variables de entorno;
  el perfil `prod` no tiene valores por defecto y no arranca si faltan
- El secreto JWT debe tener ≥ 32 bytes (se valida al arrancar)
- Login con el mismo error para email inexistente y password incorrecto
- nginx: cabeceras `X-Frame-Options`, `X-Content-Type-Options`, `Referrer-Policy`
- Contenedor del backend ejecutado con usuario no root

## Performance

- Paginación (default 10 items)
- Índices en búsquedas frecuentes
- Lazy loading en Angular
- `@EntityGraph` en listados de reservas (evita N+1)
- `open-in-view: false`
- Connection pooling (HikariCP)

## Testing

- **Backend**: JUnit + Mockito (servicios, reglas de reserva, JWT) y MockMvc (cadena de seguridad real)
- **Frontend**: Jasmine/Karma (AuthService)
- CI en GitHub Actions: tests de ambos lados + build de imágenes Docker

## Deployment

- Docker: Backend (Java 21), Frontend (Nginx), PostgreSQL, con health checks
- `docker-compose.yml` para local, `docker-compose.prod.yml` para producción
- AWS: EC2 + Docker Compose (opcional RDS / CloudFront / ECS). Ver `DEPLOYMENT.md`
