# Despliegue en AWS (EC2 + RDS)

```
Internet ──80──▶ EC2 ─┬─ frontend (nginx)  ── sirve la SPA y hace proxy de /api ──┐
                      └─ backend (Spring)  ── solo red interna Docker  ◀──────────┘
                                │
                                └──5432──▶ Amazon RDS (PostgreSQL 16)
```

Solo el puerto 80 de la EC2 queda expuesto. El backend no publica puertos y RDS solo acepta
conexiones desde el security group de la EC2. Las imágenes se construyen en el Mac y se
publican en Docker Hub; la EC2 solo hace `pull`.

---

## 1. Construir y subir las imágenes (desde el Mac)

El Mac es Apple Silicon (arm64) y la EC2 es x86 (amd64): **hay que construir con
`--platform linux/amd64`**, si no el contenedor falla en la EC2 con `exec format error`.

```bash
docker login

export DOCKERHUB_USER=<tu-usuario>
export TAG=1.0.0          # sube la versión en cada despliegue; no uses solo latest

docker buildx build --platform linux/amd64 \
  -t $DOCKERHUB_USER/coworking-backend:$TAG ./backend --push

docker buildx build --platform linux/amd64 \
  -t $DOCKERHUB_USER/coworking-frontend:$TAG ./frontend --push
```

> Para añadir también `latest`, repite `-t $DOCKERHUB_USER/coworking-backend:latest` en el
> mismo comando. El compose de producción usa siempre el tag explícito (`IMAGE_TAG`).

## 2. Crear la base de datos en RDS

1. **RDS → Create database** → PostgreSQL 16, plantilla *Free tier*, `db.t3.micro`.
2. Nombre inicial de la base: `coworking_db` (en *Additional configuration*). Flyway crea las
   tablas al primer arranque del backend.
3. Misma VPC que la EC2, **Public access: No**.
4. Security group de RDS: entrada TCP 5432 **solo desde el security group de la EC2**.

## 3. Crear la instancia EC2

- AMI **Amazon Linux 2023**, tipo `t3.micro`/`t2.micro` (capa gratuita) o `t3.small`.
- Security group: 22 desde *Mi IP* (SSH), 80 desde `0.0.0.0/0`.
- (Opcional) **Elastic IP** para que la IP pública no cambie.

Instalar Docker y el plugin compose:

```bash
ssh -i mi-clave.pem ec2-user@<IP_PUBLICA>

sudo dnf install -y docker
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user

sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-$(uname -m) \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

exit   # vuelve a entrar para que se aplique el grupo docker
```

Como en la EC2 no se compila nada, 1 GB de RAM es suficiente (el backend está limitado a 512 MB).

## 4. Desplegar en la EC2

Solo hacen falta dos ficheros: `docker-compose.prod.yml` y `.env`.

```bash
mkdir ~/coworking && cd ~/coworking
# desde el Mac:  scp -i mi-clave.pem docker-compose.prod.yml .env.example ec2-user@<IP>:~/coworking/

cp .env.example .env
nano .env
```

Valores mínimos en `.env`:

```
DB_URL=jdbc:postgresql://<endpoint-rds>.rds.amazonaws.com:5432/coworking_db?sslmode=require
DB_USERNAME=<usuario RDS>
DB_PASSWORD=<password RDS>
JWT_SECRET=<openssl rand -base64 48>
ADMIN_PASSWORD=<password del admin inicial>
DOCKERHUB_USER=<tu-usuario>
IMAGE_TAG=1.0.0
CORS_ALLOWED_ORIGINS=https://coworking.davidcorrales.dev,https://<id>.cloudfront.net
```

```bash
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
docker compose -f docker-compose.prod.yml ps          # backend y frontend "healthy"
docker compose -f docker-compose.prod.yml logs backend | grep -i flyway
```

Abre `https://coworking.davidcorrales.dev` (o `http://<ELASTIC_IP>` para probar sin CloudFront) e
inicia sesión con `ADMIN_EMAIL` / `ADMIN_PASSWORD`.

> Si falta una variable obligatoria (`DB_URL`, `DB_PASSWORD`, `JWT_SECRET`, `IMAGE_TAG`…),
> `docker compose` se niega a arrancar e indica cuál. Es intencionado.

### Actualizar a una nueva versión

1. En el Mac: construye y sube con un tag nuevo (p. ej. `1.0.1`).
2. En la EC2: cambia `IMAGE_TAG=1.0.1` en `.env` y ejecuta
   `docker compose -f docker-compose.prod.yml pull && docker compose -f docker-compose.prod.yml up -d`.

Para volver atrás basta con restaurar el tag anterior. Las migraciones de Flyway se aplican
solas al arrancar el backend (nunca edites una migración ya aplicada: crea una `V<n+1>__...sql`).

### Diagnóstico

```bash
docker compose -f docker-compose.prod.yml logs -f backend
docker compose -f docker-compose.prod.yml exec frontend wget -qO- http://backend:8080/actuator/health
```

---

## Variables de entorno

| Variable | dev (`docker-compose.yml`) | prod (`docker-compose.prod.yml`) |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` (fijado en el compose) | `prod` (fijado en el compose) |
| `DB_URL` | la fija el compose (contenedor `postgres`) | **Obligatoria** — JDBC de RDS |
| `DB_NAME` | Nombre de la BD local (`coworking_db`) | — |
| `DB_USERNAME` / `DB_PASSWORD` | **Obligatorias** — crean el postgres local | **Obligatorias** — credenciales RDS |
| `JWT_SECRET` | **Obligatoria** (≥ 32 bytes) | **Obligatoria** (≥ 32 bytes) |
| `JWT_EXPIRATION` | `3600000` ms | `3600000` ms |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Admin inicial (vacío = no se crea) | Igual |
| `APP_TIMEZONE` | `America/Bogota` | `America/Bogota` |
| `SWAGGER_ENABLED` | siempre activo en dev | `false` por defecto |
| `FRONTEND_PORT` | Puerto local del frontend (`80`) | — (siempre 80) |
| `DOCKERHUB_USER` / `IMAGE_TAG` | — | **Obligatorias** — imágenes a usar |
| `CORS_ALLOWED_ORIGINS` | no hace falta (mismo origen) | **Necesaria detrás de CloudFront**: dominios HTTPS separados por coma |
| `BACKEND_URL` (contenedor frontend) | `http://backend:8080` | `http://backend:8080` |

## Dominio y HTTPS (configuración actual)

La app se publica en **https://coworking.davidcorrales.dev**:

```
Navegador ──HTTPS──▶ CloudFront (certificado ACM + WAF) ──HTTP:80──▶ EC2 (Elastic IP) ──▶ nginx ──▶ backend
```

1. **Elastic IP** `18.235.22.232` asociada a la EC2, para que la IP no cambie al reiniciar.
2. **Certificado ACM** para `coworking.davidcorrales.dev`, creado en **us-east-1** (obligatorio para
   CloudFront) y validado por DNS con un CNAME `_xxxx.coworking` en Namecheap. Ese CNAME no se
   borra: ACM lo usa para renovar el certificado.
3. **Distribución CloudFront** (plan Free):
   - Origen: tipo *Other*, el *Public IPv4 DNS* de la EC2 (`ec2-18-235-22-232.compute-1.amazonaws.com`),
     protocolo **HTTP only**, puerto 80.
   - *Viewer protocol policy*: Redirect HTTP to HTTPS. Métodos: GET, HEAD, OPTIONS, PUT, POST, PATCH, DELETE.
   - *Cache policy* **CachingDisabled** + *Origin request policy* **AllViewer**, para que el header
     `Authorization` (JWT) llegue a la API y no se cacheen respuestas de un usuario.
   - Dominio alternativo `coworking.davidcorrales.dev` con el certificado de ACM.
4. **DNS en Namecheap**: CNAME `coworking` → `<id>.cloudfront.net`.
5. **CORS**: `CORS_ALLOWED_ORIGINS` en el `.env` de la EC2 con los orígenes HTTPS. Entre CloudFront y
   nginx la petición llega por HTTP, así que Spring ve el `Origin: https://…` como otro sitio y, sin
   esa variable, responde `403 Invalid CORS request` al login.

> Los dominios `.dev` exigen HTTPS en todos los navegadores (HSTS precargado): sin CloudFront no
> se pueden abrir por `http://`.

Mejora opcional: en el security group de la EC2, limitar el puerto 80 a la *prefix list*
`com.amazonaws.global.cloudfront.origin-facing` para que nadie entre directo por la IP saltándose
CloudFront y su WAF.

## Coste y limpieza

Con capa gratuita, EC2 `t3.micro` + RDS `db.t3.micro` cuestan 0 € los primeros 12 meses. CloudFront
está en el plan Free (0 USD/mes, sin cargos por exceso). Toda IPv4 pública, incluida la Elastic IP,
cuesta unos 0,005 USD/hora, esté o no asociada.

Al terminar el curso: **deshabilita y borra la distribución de CloudFront**, **termina la EC2**,
**borra la instancia RDS** (y sus snapshots) y **libera la Elastic IP**. Puedes borrar también el
certificado de ACM y los registros `coworking` en Namecheap.
