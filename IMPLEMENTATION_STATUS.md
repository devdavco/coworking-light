# Estado de Implementación

Última revisión: 2026-10-31.

## Funcionalidad

| Componente | Estado |
|---|---|
| Backend: auth (registro, login, JWT) | ✅ |
| Backend: espacios (CRUD admin, listado, filtro, disponibilidad) | ✅ |
| Backend: reservas (crear, listar, cancelar, reglas de negocio) | ✅ |
| Frontend: login / registro / guards / interceptores | ✅ |
| Frontend: cliente (espacios, disponibilidad, reservar, mis reservas) | ✅ |
| Frontend: admin (CRUD espacios, todas las reservas) | ✅ |
| Docker local + producción, health checks | ✅ |
| CI (GitHub Actions) | ✅ |
| Guía de despliegue AWS | ✅ `DEPLOYMENT.md` |

## Verificación realizada

- `mvn test`: 27 tests OK (servicios, reglas de reserva, JWT, seguridad con MockMvc).
- `npm run test:ci`: 3 tests OK; `ng build --configuration production` sin errores.
- `docker compose up --build`: los 3 contenedores healthy; Flyway aplica V1+V2 y Hibernate valida el esquema.
- Prueba de humo de 25 llamadas a la API (directa y a través de nginx): códigos esperados
  (201/200/400/401/403/409) en registro, login, CRUD, reservas, solapes y cancelaciones.
- `docker-compose.prod.yml`: no arranca sin secretos; con `.env` arranca con perfil `prod`,
  solo expone el puerto 80 y `admin123` no es válido.

## Problemas corregidos en la revisión

**Bloqueantes (la aplicación no compilaba o no arrancaba)**
- `JwtUtil` usaba `Jwts.parserBuilder()`, eliminado en jjwt 0.12 → no compilaba.
- Faltaba `flyway-database-postgresql` (obligatorio con Flyway 10) → no arrancaba.
- Ids `SERIAL` (INTEGER) frente a `Long` en JPA → fallaba `ddl-auto: validate`. Migración `V2`.
- Faltaba `spring-boot-starter-actuator`, pero se usaba `/actuator/health`.
- Frontend: el backend envuelve las respuestas en `ApiResponse` y el frontend leía `response.user`
  directamente → el login fallaba con error de JavaScript.
- Frontend: faltaba `fileReplacements` → el build de producción llamaba a `http://localhost:8080`.

**Seguridad**
- Sin `@EnableMethodSecurity` los `@PreAuthorize` no tenían efecto: **cualquier cliente podía
  crear, editar y borrar espacios**.
- CORS no estaba integrado en Spring Security (las peticiones preflight fallaban).
- Peticiones sin token devolvían 403 en lugar de 401.
- Login con email inexistente devolvía 404 (permitía enumerar usuarios).
- El refresh token servía como token de acceso.
- Secretos y `admin123` fijos también en producción; ahora vienen del entorno y `prod` no arranca sin ellos.
- Credenciales de prueba visibles en la pantalla de login.

**Lógica de negocio**
- Duración calculada en horas enteras (8h59m pasaba la validación de máx. 8 h).
- Se podía reservar una hora ya pasada del día actual.
- Condición de carrera: dos reservas simultáneas podían solaparse → bloqueo de fila.
- `findByTipo(String)` sobre un campo enum fallaba en tiempo de ejecución.
- `tipo` inválido, JSON mal formado, parámetros incorrectos o rutas inexistentes devolvían 500.
- Borrar un espacio con reservas canceladas fallaba con 500 (FK).
- Al actualizar un espacio se ignoraba el horario.
- Se podía cancelar dos veces la misma reserva.

## Pendiente / mejoras sugeridas

- Endpoint de refresh token (ahora, al expirar el token a la hora, hay que volver a iniciar sesión).
- Tests de integración con Testcontainers (PostgreSQL real) y tests de componentes Angular.
- Paginación en las tablas del frontend (ahora carga hasta 50-100 elementos).
- Calendario visual de disponibilidad.
- Rate limiting en `/api/auth/login`.
- Quitar `@angular/material` de `package.json` (no se usa).
