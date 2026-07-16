# notes-app-microservices (dark mode)

Application complète type Evernote : notes, carnets, étiquettes, recherche, épinglage,
corbeille (soft delete + restauration + suppression définitive), pièces jointes.

## Stack

| Composant | Techno | Port |
|---|---|---|
| Frontend | Angular 17 (dark mode) + keycloak-js | 4300 |
| Backend | Spring Boot 3.2 (Java 17) | 8082 |
| Auth | Keycloak 25 (realm `notesapp`, PKCE) | 8090 |
| Base de données | PostgreSQL 16 | 5433 |
| Messagerie | Kafka 7.5.0 (KRaft) — topic `note-events` | 9094 |
| Fichiers | MinIO (bucket `notesapp-attachments`) | 9100 / console 9101 |
| Discovery | Eureka (Spring Cloud) | 8762 |

> Tous les ports et noms de conteneurs (`notesapp-*`) sont décalés pour cohabiter
> avec d'autres stacks locales sans aucun conflit.

## Démarrage

```bash
cd notes-app-microservices
docker compose up -d --build
```

Puis ouvrir http://localhost:4300 — connexion Keycloak :

- utilisateur : `user` / mot de passe : `user123`
- admin Keycloak : `admin` / `admin` → http://localhost:8090
- console MinIO : `notesapp` / `notesapp123` → http://localhost:9101
- dashboard Eureka : http://localhost:8762

## Développement local (sans Docker pour le front/back)

```bash
# Infra seulement
docker compose up -d postgres keycloak kafka minio discovery

# Backend
cd backend && mvn spring-boot:run

# Frontend (proxy /api -> 8082 déjà configuré)
cd frontend && npm install && npm start
```

## Architecture

- Chaque requête est authentifiée par un JWT Keycloak (resource server côté backend,
  rôles realm mappés en `ROLE_*`).
- Chaque action sur une note (création, modification, corbeille, restauration, purge)
  publie un événement `NoteEvent` sur Kafka ; un consumer l'historise dans
  `activity_logs` (exposé via `GET /api/activity`).
- Les pièces jointes sont stockées dans MinIO ; la purge d'une note nettoie ses objets.
- Le backend s'enregistre auprès d'Eureka (`notesapp-backend`).

## API principale

| Méthode | Endpoint | Description |
|---|---|---|
| GET | `/api/notes?notebookId=&tagId=&q=` | Liste / filtre / recherche |
| GET | `/api/notes/trash` | Corbeille |
| POST | `/api/notes` | Créer |
| PUT | `/api/notes/{id}` | Modifier |
| DELETE | `/api/notes/{id}` | Envoyer à la corbeille (soft delete) |
| POST | `/api/notes/{id}/restore` | Restaurer |
| DELETE | `/api/notes/{id}/purge` | Suppression définitive |
| GET/POST/PUT/DELETE | `/api/notebooks…` | Carnets |
| GET/DELETE | `/api/tags…` | Étiquettes |
| POST | `/api/notes/{id}/attachments` | Upload fichier |
| GET/DELETE | `/api/attachments/{id}` | Télécharger / supprimer |
| GET | `/api/activity` | Historique (via Kafka) |
