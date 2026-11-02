# syntax=docker/dockerfile:1
# Build multiplataforma desde Mac Apple Silicon para EC2 x86:
#   docker buildx build --platform linux/amd64 -t <usuario>/coworking-backend:<version> .

# ---- Build stage: Maven + JDK 21 ----
# El jar no depende de la arquitectura: se compila nativo en la máquina de build (rápido en
# Apple Silicon, sin emulación) y solo la imagen final es linux/amd64.
FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Dependencias en una capa propia (se cachea mientras pom.xml no cambie)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests \
 && cp target/coworking-backend-*.jar /app/app.jar

# ---- Runtime stage: solo JRE 21 + el jar ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
COPY --from=builder --chown=app:app /app/app.jar app.jar
USER app

EXPOSE 8080

# Ajusta el heap al límite de memoria del contenedor
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
