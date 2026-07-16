# Docker — Hardening & Best Practices

## The secure Dockerfile pattern

```dockerfile
# 1. Multi-stage: build tools never reach production
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline          # cached layer
COPY src ./src
RUN mvn -B package -DskipTests

# 2. Minimal runtime image
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app   # non-root user
USER app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java","-jar","app.jar"]
```

## Hardening checklist

- [ ] **Non-root user** (`USER app`) — container breakout is much harder
- [ ] **Minimal base image** — alpine, distroless, or chainguard images
- [ ] **Pin versions** — `alpine:3.20`, never `latest`
- [ ] **Multi-stage builds** — no compilers/secrets in the final image
- [ ] **`.dockerignore`** — exclude `.git`, `.env`, `node_modules`, secrets
- [ ] **Read-only filesystem**: `docker run --read-only --tmpfs /tmp`
- [ ] **Drop capabilities**: `--cap-drop=ALL --cap-add=NET_BIND_SERVICE`
- [ ] **No secrets in ENV/ARG** — they persist in image layers; use runtime secrets
- [ ] **Scan every image** — `trivy image myapp:1.0`

## docker-compose for local dev (secure defaults)

```yaml
services:
  api:
    build: ./backend
    read_only: true
    security_opt: [no-new-privileges:true]
    cap_drop: [ALL]
    tmpfs: [/tmp]
    environment:
      DB_PASSWORD_FILE: /run/secrets/db_password
    secrets: [db_password]

secrets:
  db_password:
    file: ./secrets/db_password.txt   # git-ignored!
```

## Layer & size optimization

1. Order instructions from least → most frequently changed (cache hits).
2. Combine `RUN` commands; clean package caches in the same layer.
3. `docker history myimage` and `dive myimage` to inspect what bloats you.

## Debug commands

```bash
docker logs -f <ctr>              # follow logs
docker exec -it <ctr> sh          # shell inside
docker inspect <ctr> | jq '.[0].NetworkSettings'
docker stats                      # live resource usage
docker system prune -af           # reclaim space (careful)
```
