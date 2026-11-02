# Estructura del Proyecto - Coworking Booking

```
coworking-booking-kiro/                          [Raíz del Proyecto]
├── backend/                                     [Spring Boot Application]
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/coworking/
│   │   │   │   ├── CoworkingBackendApplication.java    [Main app]
│   │   │   │   ├── config/
│   │   │   │   │   ├── ClockConfig.java                [Zona horaria / Clock]
│   │   │   │   │   ├── CorsConfig.java                 [CORS]
│   │   │   │   │   ├── DataLoader.java                 [Seed data]
│   │   │   │   │   ├── JwtAuthenticationFilter.java    [JWT filter]
│   │   │   │   │   ├── JwtProperties.java              [JWT config]
│   │   │   │   │   └── SecurityConfig.java             [Spring Security]
│   │   │   │   ├── controller/
│   │   │   │   │   ├── AuthController.java             [POST /auth/*]
│   │   │   │   │   ├── EspacioController.java          [GET/POST/PUT/DELETE /espacios]
│   │   │   │   │   └── ReservaController.java          [GET/POST/DELETE /reservas]
│   │   │   │   ├── service/
│   │   │   │   │   ├── AuthService.java                [Auth logic]
│   │   │   │   │   ├── EspacioService.java             [Space logic]
│   │   │   │   │   ├── ReservaService.java             [Reservation logic]
│   │   │   │   │   └── UsuarioService.java             [User logic]
│   │   │   │   ├── repository/
│   │   │   │   │   ├── EspacioRepository.java          [Space queries]
│   │   │   │   │   ├── ReservaRepository.java          [Reservation queries]
│   │   │   │   │   └── UsuarioRepository.java          [User queries]
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Espacio.java                    [Space entity]
│   │   │   │   │   ├── Reserva.java                    [Reservation entity]
│   │   │   │   │   └── Usuario.java                    [User entity]
│   │   │   │   ├── dto/
│   │   │   │   │   ├── ApiResponse.java                [Generic response]
│   │   │   │   │   ├── AuthLoginRequest.java           [Login DTO]
│   │   │   │   │   ├── AuthRegisterRequest.java        [Register DTO]
│   │   │   │   │   ├── AuthResponse.java               [Auth response]
│   │   │   │   │   ├── DisponibilidadDTO.java          [Availability DTO]
│   │   │   │   │   ├── EspacioDTO.java                 [Space DTO]
│   │   │   │   │   ├── PaginatedResponse.java          [Pagination DTO]
│   │   │   │   │   ├── ReservaDTO.java                 [Reservation DTO]
│   │   │   │   │   └── UsuarioDTO.java                 [User DTO]
│   │   │   │   ├── exception/
│   │   │   │   │   ├── DuplicateResourceException.java [409]
│   │   │   │   │   ├── GlobalExceptionHandler.java     [Exception handler]
│   │   │   │   │   ├── ResourceNotFoundException.java  [404]
│   │   │   │   │   ├── UnauthorizedException.java      [401]
│   │   │   │   │   └── ValidationException.java        [400]
│   │   │   │   └── util/
│   │   │   │       ├── JwtUtil.java                    [JWT utilities]
│   │   │   │       └── PageUtils.java                  [Paginación segura]
│   │   │   └── resources/
│   │   │       ├── application.yml                     [Base config]
│   │   │       ├── application-dev.yml                 [Dev config]
│   │   │       ├── application-prod.yml                [Prod config]
│   │   │       └── db/migration/
│   │   │           ├── V1__init_schema.sql             [Flyway migration]
│   │   │           └── V2__ids_to_bigint.sql           [Ids a BIGINT]
│   │   └── test/
│   │       └── java/com/coworking/
│   │           ├── controller/EspacioControllerSecurityTest.java [Seguridad]
│   │           ├── service/AuthServiceTest.java
│   │           ├── service/ReservaServiceTest.java     [Reglas de reserva]
│   │           ├── service/UsuarioServiceTest.java
│   │           └── util/JwtUtilTest.java
│   ├── pom.xml                                  [Maven dependencies]
│   ├── Dockerfile                               [Backend container]
│   └── .gitignore
│
├── frontend/                                    [Angular Application]
│   ├── src/
│   │   ├── app/
│   │   │   ├── app.component.ts                 [Root component]
│   │   │   ├── app.module.ts                    [App module]
│   │   │   ├── app-routing.module.ts            [Main routing]
│   │   │   ├── core/
│   │   │   │   ├── services/
│   │   │   │   │   ├── auth.service.ts          [Auth service]
│   │   │   │   │   ├── auth.service.spec.ts
│   │   │   │   │   ├── espacio.service.ts
│   │   │   │   │   └── reserva.service.ts
│   │   │   │   ├── guards/
│   │   │   │   │   ├── auth.guard.ts            [Auth guard]
│   │   │   │   │   └── role.guard.ts            [Role guard]
│   │   │   │   ├── interceptors/
│   │   │   │   │   ├── error.interceptor.ts     [Error interceptor]
│   │   │   │   │   └── jwt.interceptor.ts       [JWT interceptor]
│   │   │   │   └── models/
│   │   │   │       └── api.models.ts            [Tipos de la API]
│   │   │   ├── modules/
│   │   │   │   ├── auth/
│   │   │   │   │   ├── auth.module.ts
│   │   │   │   │   ├── auth-routing.module.ts
│   │   │   │   │   └── components/
│   │   │   │   │       ├── login/
│   │   │   │   │       │   ├── login.component.ts
│   │   │   │   │       │   ├── login.component.html
│   │   │   │   │       │   └── login.component.scss
│   │   │   │   │       └── register/
│   │   │   │   │           ├── register.component.ts
│   │   │   │   │           ├── register.component.html
│   │   │   │   │           └── register.component.scss
│   │   │   │   ├── admin/
│   │   │   │   │   ├── admin.module.ts
│   │   │   │   │   ├── admin-routing.module.ts
│   │   │   │   │   └── components/
│   │   │   │   │       └── admin-dashboard/
│   │   │   │   │           ├── admin-dashboard.component.ts
│   │   │   │   │           └── admin-dashboard.component.html  [CRUD espacios + reservas]
│   │   │   │   └── client/
│   │   │   │       ├── client.module.ts
│   │   │   │       ├── client-routing.module.ts
│   │   │   │       └── components/
│   │   │   │           └── client-dashboard/
│   │   │   │               ├── client-dashboard.component.ts
│   │   │   │               └── client-dashboard.component.html [Reservar + mis reservas]
│   │   │   ├── environments/
│   │   │   │   ├── environment.ts                [Dev config]
│   │   │   │   └── environment.prod.ts           [Prod config]
│   │   │   └── shared/
│   │   │       ├── shared.module.ts
│   │   │       ├── navbar/navbar.component.ts
│   │   │       ├── hora.pipe.ts
│   │   │       └── fechas.ts
│   │   ├── index.html                            [Main HTML]
│   │   ├── main.ts                               [Entry point]
│   │   └── styles.scss                           [Global styles]
│   ├── package.json                              [NPM dependencies]
│   ├── angular.json                              [Angular config]
│   ├── tsconfig.json                             [TypeScript config]
│   ├── tsconfig.app.json
│   ├── tsconfig.spec.json
│   ├── karma.conf.js                             [Karma config]
│   ├── .browserslistrc
│   ├── nginx/default.conf.template               [Nginx config (BACKEND_URL)]
│   ├── Dockerfile                                [Frontend container]
│   └── .gitignore
│
├── docker-compose.yml                           [Entorno local]
├── docker-compose.prod.yml                      [Producción / EC2]
├── .env.example                                 [Variables de producción]
├── .github/workflows/ci.yml                     [CI]
├── DEPLOYMENT.md                                [Despliegue AWS]
├── .gitignore                                    [Git ignore]
├── README.md                                     [Setup instructions]
├── ARCHITECTURE.md                               [Architecture doc]
├── IMPLEMENTATION_STATUS.md                      [Implementation status]
├── PROJECT_STRUCTURE.md                          [Este archivo]
└── SPEC_AUTH.md                                  [Auth spec; resto de la API en README.md]
```

## Resumen por Capa

### Backend - Config Layer
- **Seguridad**: JWT filter, SecurityConfig, CORS
- **Datos**: DataLoader para seed, JwtProperties
- **Excepciones**: GlobalExceptionHandler

### Backend - Control Layer
- **AuthController**: /api/auth/register, /api/auth/login
- **EspacioController**: GET/POST/PUT/DELETE /api/espacios, GET /disponibilidad
- **ReservaController**: GET/POST/DELETE /api/reservas

### Backend - Business Layer
- **AuthService**: Lógica de registro y login
- **EspacioService**: CRUD espacios con validaciones
- **ReservaService**: Crear, cancelar, listar, calcular disponibilidad
- **UsuarioService**: Operaciones de usuario

### Backend - Persistence Layer
- **Repositories**: Queries especializadas
- **Entities**: Usuario, Espacio, Reserva con relaciones
- **DTOs**: Validaciones y transferencia de datos

### Frontend - Core Layer
- **AuthService**: Gestiona autenticación, token, usuario
- **JwtInterceptor**: Agrega token a headers
- **ErrorInterceptor**: Maneja errores HTTP
- **AuthGuard**: Protege rutas autenticadas
- **RoleGuard**: Protege rutas por rol

### Frontend - Modules
- **AuthModule**: Login y registro
- **AdminModule**: Dashboard admin (CRUD espacios, reservas)
- **ClientModule**: Dashboard cliente (espacios, reservas)

---

## Convenciones

### Backend
- Controllers en `com.coworking.controller`
- Services en `com.coworking.service`
- Entities en `com.coworking.entity`
- DTOs en `com.coworking.dto`
- Repositories en `com.coworking.repository`
- Exceptions en `com.coworking.exception`

### Frontend
- Components en módulos
- Services en `core/services`
- Guards en `core/guards`
- Interceptors en `core/interceptors`
- Models en `core/models`

---

## Archivos Clave

| Archivo | Propósito |
|---------|----------|
| `pom.xml` | Todas las dependencias Maven |
| `application.yml` | Configuración base Spring Boot |
| `application-dev.yml` | Configuración development |
| `V1__init_schema.sql` | Schema inicial BD |
| `SecurityConfig.java` | Configuración Spring Security |
| `JwtUtil.java` | Utilidades JWT |
| `GlobalExceptionHandler.java` | Manejo centralizado de errores |
| `auth.service.ts` | Servicio de autenticación Angular |
| `auth.guard.ts` | Protección de rutas |
| `docker-compose.yml` | Orquestación de contenedores |

---

## Próximas Adiciones

Ver la sección *Pendiente* de `IMPLEMENTATION_STATUS.md`.
