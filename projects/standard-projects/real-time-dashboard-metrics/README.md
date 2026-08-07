# Real-Time System Metrics Dashboard

> Dashboard de métriques en temps réel sans dépendance externe (Java 17 `HttpServer` natif, Canvas API natif & Polling REST).

## Architecture & Choix Techniques

Ce projet illustre le monitoring applicatif fluide et léger en temps réel.

- **Backend** : Java 17 natif. Générateur de métriques planifié en tâche de fond (`ScheduledExecutorService`) alimentant un buffer circulaire de 60 points en mémoire (`ConcurrentLinkedQueue`).
- **Métriques simulées** : Charge CPU (%), Consommation mémoire (Mo), Requêtes/sec (RPS) et Latence moyenne (ms).
- **Frontend** : Dashboard réactif HTML5 / Canvas API traçant une courbe fluide des métriques rafraîchie par polling toutes les 2 secondes.

## Structure du Projet

```text
real-time-dashboard-metrics/
├── src/
│   ├── Server.java      # Serveur HTTP, générateur de métriques & endpoints REST
│   ├── Json.java        # Serializer JSON natif
│   └── HttpUtil.java    # Utilitaires HTTP (Headers & MIME)
└── public/              # Application Web Dashboard (HTML, CSS, JS Canvas)
```

## Démarrage Rapide

### Compiler et lancer

```bash
# Compilation des sources
javac -d bin src/*.java

# Exécution du serveur
java -cp bin Server
```

Le tableau de bord est accessible sur **`http://localhost:8080`**.

## Endpoints API

| Méthode | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/metrics` | Retourne la dernière mesure de métrique |
| `GET` | `/api/metrics/history` | Retourne l'historique des 60 derniers points de mesure |
