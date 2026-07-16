# Docker Cheatsheet

## Images

```bash
docker build -t app:1.0 .              # build
docker images                          # list
docker rmi app:1.0                     # remove
docker history app:1.0                 # layers & sizes
docker tag app:1.0 ghcr.io/me/app:1.0  # retag for a registry
docker push ghcr.io/me/app:1.0
```

## Containers

```bash
docker run -d --name api -p 8080:8080 --env-file .env app:1.0
docker ps -a                           # all containers
docker logs -f api                     # follow logs
docker exec -it api sh                 # shell inside
docker stop api && docker rm api
docker stats                           # live CPU/mem
docker inspect api                     # full config JSON
```

## Compose

```bash
docker compose up -d          # start stack
docker compose up -d --build  # rebuild changed services
docker compose logs -f api    # logs of one service
docker compose ps
docker compose down           # stop & remove
docker compose down -v        # ...and volumes (⚠️ data)
```

## Volumes & networks

```bash
docker volume ls / inspect / rm
docker network ls
docker network inspect bridge
# containers on the same compose network reach each other by SERVICE NAME
```

## Cleanup

```bash
docker system df              # what's eating disk
docker system prune           # dangling stuff
docker system prune -af       # everything unused (⚠️)
docker builder prune          # build cache
```

## Debug reflexes

| Symptom | First move |
|---|---|
| Container exits instantly | `docker logs <c>` — the app crashed, read why |
| "port is already allocated" | `ss -tulpn \| grep <port>` or change host port |
| Can't reach service from another container | use service name not `localhost`; same network? |
| Changes to code not visible | image is stale → rebuild, or use a bind mount in dev |
| Works locally, not in container | env vars? file paths? user permissions? |
