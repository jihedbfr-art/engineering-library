# --- build stage ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
# Copy the reactor and build only the app plus what it needs.
COPY pom.xml .
COPY core core
COPY connector-api connector-api
COPY persistence persistence
COPY media media
COPY connector-linkedin connector-linkedin
COPY connector-x connector-x
COPY connector-facebook connector-facebook
COPY connector-instagram connector-instagram
COPY connector-youtube connector-youtube
COPY connector-tiktok connector-tiktok
COPY app app
RUN mvn -q -B -DskipTests -pl app -am package

# --- runtime stage ---
FROM eclipse-temurin:17-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg curl \
    && rm -rf /var/lib/apt/lists/*
RUN useradd -r -u 1001 -m appuser
WORKDIR /app
COPY --from=build /workspace/app/target/social-publisher-mcp.jar app.jar
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
