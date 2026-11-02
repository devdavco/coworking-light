# syntax=docker/dockerfile:1
# Build multiplataforma desde Mac Apple Silicon para EC2 x86:
#   docker buildx build --platform linux/amd64 -t <usuario>/coworking-frontend:<version> .

# ---- Build stage: Node 22 + Angular CLI ----
# Los estáticos no dependen de la arquitectura: se compilan nativos (sin emulación).
FROM --platform=$BUILDPLATFORM node:22-alpine AS builder
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY . .
RUN npm run build -- --configuration production

# ---- Runtime stage: nginx ----
FROM nginx:1.27-alpine

# Host del backend, configurable al arrancar el contenedor
ENV BACKEND_URL=http://backend:8080

# Angular 17 con builder "browser": la salida es dist/<proyecto>/ (sin subcarpeta browser/)
COPY --from=builder /app/dist/coworking-frontend /usr/share/nginx/html
# La imagen oficial procesa /etc/nginx/templates/*.template -> /etc/nginx/conf.d/
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://127.0.0.1/ >/dev/null || exit 1
