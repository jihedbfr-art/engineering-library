COMPOSE = docker compose -f docker/docker-compose.yml

.PHONY: build build-fast test up down logs verify fmt demo

# Local visual demo, no Docker: in-memory H2 + MOCK connector + web UI at http://localhost:8080
demo:
	mvn -B -pl app -am -DskipTests package
	java -jar app/target/social-publisher-mcp.jar --spring.profiles.active=demo

build:
	mvn -B clean package

build-fast:
	mvn -B -DskipTests clean package

test:
	mvn -B test

verify:
	mvn -B verify

fmt:
	mvn -B spotless:apply

up:
	$(COMPOSE) up --build

down:
	$(COMPOSE) down -v

logs:
	$(COMPOSE) logs -f app
