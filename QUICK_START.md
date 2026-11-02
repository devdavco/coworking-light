# Quick Start

```bash
cp .env.example .env
# Rellena DB_PASSWORD, JWT_SECRET (openssl rand -base64 48) y ADMIN_PASSWORD
docker compose up --build
```

| Qué | Dónde |
|---|---|
| Aplicación | http://localhost |
| API (vía nginx) | http://localhost/api |
| API (directa) | http://localhost:8080/api |
| Swagger | http://localhost:8080/swagger-ui.html |
| Health | http://localhost:8080/actuator/health |

Admin de desarrollo: `admin@coworking.com` / el `ADMIN_PASSWORD` de tu `.env`.
Los clientes se registran desde `/auth/register`.

### Desarrollo sin Docker para el frontend

```bash
docker compose up -d postgres backend
cd frontend && npm start        # ng serve en :4200; /api se redirige a :8080 (proxy.conf.json)
```

Para arrancar el backend desde el IDE: `SPRING_PROFILES_ACTIVE=dev`, `DB_PASSWORD` y `JWT_SECRET`
como variables de entorno (los mismos valores del `.env`).

### Probar la API con curl

```bash
source .env
TOKEN=$(curl -s -X POST http://localhost/api/auth/login \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"admin@coworking.com\",\"password\":\"$ADMIN_PASSWORD\"}" | sed -E 's/.*"token":"([^"]+)".*/\1/')

curl -s http://localhost/api/espacios -H "Authorization: Bearer $TOKEN"
```

### Problemas frecuentes

- **Puerto ocupado (80, 8080, 5432)**: para el proceso que lo usa o cambia `FRONTEND_PORT` en `.env`.
- **Error de checksum de Flyway** tras editar una migración ya aplicada: `docker compose down -v` (borra los datos locales).
- **Cambiaste `DB_PASSWORD` y el backend no conecta**: el volumen conserva la contraseña antigua; `docker compose down -v`.
- **Reconstruir tras cambios**: `docker compose up --build`.

Despliegue en AWS: [DEPLOYMENT.md](DEPLOYMENT.md).
