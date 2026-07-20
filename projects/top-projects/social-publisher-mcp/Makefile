COMPOSE = docker compose -f docker/docker-compose.yml

.PHONY: build build-fast test up down logs verify fmt

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
