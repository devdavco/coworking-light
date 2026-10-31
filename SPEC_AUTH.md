# AUTH API SPEC

Todas las respuestas van envueltas en `{ success, message, data, timestamp }`.
Abajo se describe el contenido de `data`.

## POST /api/auth/register
- Auth: `none`
- Payload: `{ email, password, firstName, lastName }`
- Response `data`: `{ token, refreshToken, user: { id, email, firstName, lastName, role: "CLIENT" } }`
- Status: `201 / 400 / 409`
- Validaciones: email válido, password 8–72 chars, email único (sin distinguir mayúsculas),
  firstName/lastName no vacíos (máx. 100)
- El rol siempre es `CLIENT`; los administradores no se pueden registrar por la API.

## POST /api/auth/login
- Auth: `none`
- Payload: `{ email, password }`
- Response `data`: `{ token, refreshToken, user: { id, email, firstName, lastName, role } }`
- Status: `200 / 400 / 401`
- Token: JWT HS256, `sub` = email, claim `role`, expira según `JWT_EXPIRATION` (1 h por defecto)

## Uso del token
`Authorization: Bearer <token>` en el resto de endpoints.
El `refreshToken` no tiene rol y **no** sirve para acceder a la API (no hay endpoint de refresh todavía).

## Error Cases
- `409 Conflict`: email ya registrado
- `401 Unauthorized`: credenciales inválidas (mismo mensaje si el email no existe, para no revelar qué cuentas existen)
- `400 Bad Request`: formato inválido, validación fallida
