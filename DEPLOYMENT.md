# Despliegue en AWS

Arquitectura recomendada para un proyecto académico: **una instancia EC2 con Docker Compose**.
Es la opción más simple y barata (entra en la capa gratuita) y usa exactamente los mismos
contenedores que en local.

```
Internet ──80──▶ EC2 ─┬─ frontend (nginx)  ── sirve la SPA y hace proxy de /api
                      ├─ backend (Spring)  ── solo red interna Docker
                      └─ postgres          ── solo red interna Docker (volumen persistente)
```

Solo el puerto 80 queda expuesto. El backend y la base de datos no son accesibles desde fuera.

---

## 1. Crear la instancia EC2

1. Consola AWS → **EC2 → Launch instance**
   - AMI: **Amazon Linux 2023**
   - Tipo: `t3.small` (recomendado) o `t3.micro` / `t2.micro` (capa gratuita, ver paso 3: swap)
   - Key pair: crea o selecciona una para conectarte por SSH
   - Almacenamiento: 20 GB gp3
2. **Security group** (reglas de entrada):
   | Puerto | Origen | Uso |
   |---|---|---|
   | 22 | *Mi IP* | SSH |
   | 80 | 0.0.0.0/0 | Aplicación web |
3. (Opcional) Asigna una **Elastic IP** para que la IP pública no cambie al reiniciar.

## 2. Instalar Docker

```bash
ssh -i mi-clave.pem ec2-user@<IP_PUBLICA>

sudo dnf install -y docker git
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user

# Plugin docker compose
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-$(uname -m) \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

exit   # vuelve a entrar para que se aplique el grupo docker
```

> Si `docker compose build` pide una versión más reciente de **buildx**, descarga el binario
> desde https://github.com/docker/buildx/releases a `/usr/local/lib/docker/cli-plugins/docker-buildx`
> y dale permisos de ejecución.

## 3. (Solo instancias de 1 GB) Añadir swap

Compilar Maven y Angular en una máquina de 1 GB puede quedarse sin memoria:

```bash
sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab
```

## 4. Desplegar

```bash
git clone <URL_DE_TU_REPO> coworking && cd coworking

cp .env.example .env
# Genera secretos y edítalos en .env:
openssl rand -base64 48   # -> JWT_SECRET
openssl rand -hex 16      # -> POSTGRES_PASSWORD
nano .env

docker compose -f docker-compose.prod.yml --env-file .env up -d --build
docker compose -f docker-compose.prod.yml ps        # los 3 servicios deben quedar "healthy"
```

Abre `http://<IP_PUBLICA>` e inicia sesión con `ADMIN_EMAIL` / `ADMIN_PASSWORD` del `.env`.

> Si falta alguna variable obligatoria (`JWT_SECRET`, `POSTGRES_PASSWORD`, `ADMIN_PASSWORD`…),
> `docker compose` se niega a arrancar y muestra cuál falta. Es intencionado.

### Actualizar a una nueva versión

```bash
git pull
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

Las migraciones de base de datos (Flyway) se aplican solas al arrancar el backend.

### Logs y diagnóstico

```bash
docker compose -f docker-compose.prod.yml logs -f backend
docker compose -f docker-compose.prod.yml exec postgres psql -U <POSTGRES_USER> coworking_db
```

### Copia de seguridad de la base de datos

```bash
docker compose -f docker-compose.prod.yml exec -T postgres \
  pg_dump -U <POSTGRES_USER> coworking_db > backup_$(date +%F).sql
```

---

## Mejoras opcionales

### Base de datos gestionada (Amazon RDS)

1. Crea una instancia **RDS PostgreSQL 16** (`db.t3.micro`, capa gratuita) en la misma VPC.
2. En el security group de RDS, permite el puerto 5432 **solo** desde el security group de la EC2.
3. En `.env`:
   ```
   DB_URL=jdbc:postgresql://<endpoint-rds>:5432/coworking_db
   POSTGRES_USER=<usuario RDS>
   POSTGRES_PASSWORD=<password RDS>
   ```
4. En `docker-compose.prod.yml` elimina el servicio `postgres`, su volumen y el
   `depends_on: postgres` del backend.

### HTTPS

La forma más sencilla sin dominio propio: crea una distribución de **CloudFront** con la EC2 como
origen (HTTP, puerto 80), *Viewer protocol policy* "Redirect HTTP to HTTPS" y caché desactivada
para `/api/*`. Obtendrás una URL `https://xxxx.cloudfront.net`.
Con dominio propio: **Application Load Balancer** + certificado gratuito de **ACM**.

### Contenedores gestionados (ECS Fargate)

Para una arquitectura más "cloud native": sube las imágenes a **ECR**, crea un servicio **ECS
Fargate** por contenedor detrás de un **ALB** (`/api/*` → backend, resto → frontend) y usa
**RDS**. Pasa los secretos con **Secrets Manager / SSM Parameter Store** en lugar de `.env`.
El health check del ALB para el backend es `GET /actuator/health`.

---

## Variables de entorno (perfil `prod`)

| Variable | Obligatoria | Descripción |
|---|---|---|
| `POSTGRES_USER` / `POSTGRES_PASSWORD` | Sí | Credenciales de la BD |
| `POSTGRES_DB` | No (`coworking_db`) | Nombre de la BD |
| `DB_URL` | No | JDBC URL; por defecto el contenedor `postgres` |
| `JWT_SECRET` | Sí | ≥ 32 bytes. `openssl rand -base64 48` |
| `ADMIN_EMAIL` | No (`admin@coworking.com`) | Admin creado en el primer arranque |
| `ADMIN_PASSWORD` | Sí | Contraseña de ese admin |
| `APP_TIMEZONE` | No (`America/Bogota`) | Zona horaria del coworking |
| `JWT_EXPIRATION` | No (`3600000`) | Duración del token en ms |

## Coste y limpieza

Con capa gratuita, una EC2 `t3.micro` + 20 GB de EBS cuesta 0 € los primeros 12 meses.
Al terminar el curso: **termina la instancia**, libera la Elastic IP (cobra si no está asociada)
y borra RDS/CloudFront si los creaste.
