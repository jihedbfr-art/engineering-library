# Le conteneur Docker démarre puis `Exited (1)` immédiatement

> `docker ps -a` montre le conteneur en état `Exited` quelques secondes après `docker run`/`docker-compose up`, parfois sans aucun log exploitable à l'écran.

## Causes probables (fréquentes → rares)

1. Le process principal (PID 1 du conteneur, celui défini par `CMD`/`ENTRYPOINT`) plante ou se termine normalement dès le démarrage — un conteneur Docker s'arrête dès que son PID 1 se termine, peu importe la raison. C'est de loin la cause la plus fréquente : une exception au démarrage de l'application, un fichier de config manquant, une variable d'environnement requise absente.
2. Le `CMD` lance un process qui se termine tout de suite parce qu'il n'a rien à faire en tâche de fond (typiquement un script qui fait son travail puis `exit 0` — normal pour un job, anormal pour un serveur censé rester up).
3. Le conteneur dépend d'un autre service (base de données, broker) pas encore prêt, et l'application quitte au lieu de retenter la connexion — fréquent avec `docker-compose up` sans `depends_on`/`healthcheck` correctement configuré, la base démarre encore pendant que l'app essaie déjà de s'y connecter.
4. Mauvaise architecture d'image (image buildée pour `amd64` exécutée sur une machine `arm64` ou l'inverse) — le binaire ne peut littéralement pas s'exécuter, message d'erreur souvent cryptique type `exec format error`.

## Diagnostic pas-à-pas

```bash
# 1. Voir le code de sortie exact — 0 = arrêt normal, autre chose = crash
docker ps -a --filter "name=mon-service" --format "{{.Status}}"

# 2. Les logs du conteneur arrêté restent consultables même après Exited
docker logs mon-service

# 3. Si les logs sont vides ou inexploitables, relancer en interactif pour voir
# ce qui se passe avant le crash, sans le comportement "restart automatique"
# d'un docker-compose qui masquerait la fenêtre utile
docker run -it --rm mon-image:tag

# 4. Vérifier que le CMD/ENTRYPOINT effectif est bien celui attendu
docker inspect mon-image:tag --format '{{.Config.Cmd}} {{.Config.Entrypoint}}'

# 5. Cas dépendance pas prête : vérifier l'ordre de démarrage réel
docker-compose logs -f db app   # observer les deux logs entrelacés dans le temps
```

## Correctif

- **Process qui plante au démarrage** : le correctif est dans le code/config de l'application, pas dans Docker — le message d'erreur de `docker logs` (ou du run interactif) pointe généralement directement la cause (variable d'env manquante, connexion refusée, fichier absent).
- **Dépendance pas prête** : ne pas se fier à `depends_on` seul (il attend que le conteneur démarre, pas que le service à l'intérieur soit prêt à répondre) — ajouter un vrai `healthcheck` sur le service dépendant et `depends_on: condition: service_healthy` :
```yaml
services:
  db:
    image: postgres:16
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 2s
      retries: 10
  app:
    depends_on:
      db:
        condition: service_healthy
```
  Ou, en complément côté application : une logique de retry avec backoff sur la connexion initiale plutôt qu'un crash immédiat si la base n'est pas encore là.
- **Mauvaise architecture d'image** : rebuild avec `docker buildx build --platform linux/amd64,linux/arm64` pour produire une image multi-arch, ou explicitement la bonne plateforme pour la machine cible.

## Si ça ne suffit pas

Si le conteneur redémarre en boucle plutôt que de rester `Exited` (typiquement avec `restart: always` ou `unless-stopped`), voir [kubernetes-crashloopbackoff.md](kubernetes-crashloopbackoff.md) — c'est le même mode de défaillance transposé à Kubernetes, la méthode de diagnostic (logs du dernier crash, code de sortie, vérifier les dépendances) est identique.
