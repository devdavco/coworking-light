# Quick Start

```bash
docker compose up --build
```

| Qué | Dónde |
|---|---|
| Aplicación | http://localhost |
| API | http://localhost:8080/api |
| Swagger | http://localhost:8080/swagger-ui.html |
| Health | http://localhost:8080/actuator/health |

Admin de desarrollo: `admin@coworking.com` / `admin123`. Los clientes se registran desde `/auth/register`.

### Probar la API con curl

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@coworking.com","password":"admin123"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')

curl -s http://localhost:8080/api/espacios -H "Authorization: Bearer $TOKEN"
```

### Problemas frecuentes

- **Puerto ocupado (80, 8080, 5432)**: para el proceso que lo usa o cambia el puerto en `docker-compose.yml`.
- **Error de checksum de Flyway** tras editar una migración ya aplicada: `docker compose down -v` (borra los datos locales).
- **Reconstruir tras cambios**: `docker compose up --build`.

Más detalles en [README.md](README.md) y despliegue en [DEPLOYMENT.md](DEPLOYMENT.md).
