# Task Management Platform

API REST de gestion de projets et de taches d'equipe, avec deux roles distincts (ADMIN, MEMBER)
et des regles d'autorisation appliquees au niveau service.

## But

Simuler un outil de suivi de taches type Jira simplifie : un ADMIN cree les projets et pilote
l'ensemble des taches, un MEMBER ne peut agir que sur les taches qui lui sont assignees. Le
systeme expose aussi une detection explicite des taches en retard.

## Architecture

Monolithe Spring Boot en couches, module Maven unique :

| Couche | Package | Role |
|---|---|---|
| Controller | `com.jihedapps.taskmanagement.controller` | Endpoints REST, mapping DTO <-> service, codes HTTP |
| Service | `com.jihedapps.taskmanagement.service` | Regles metier et autorisations, transactions |
| Repository | `com.jihedapps.taskmanagement.repository` | Acces donnees via Spring Data JPA |
| Entity | `com.jihedapps.taskmanagement.entity` | Modele de domaine (User, Project, Task, Comment) |
| DTO | `com.jihedapps.taskmanagement.dto` | Contrats d'entree/sortie de l'API (records) |
| Exception | `com.jihedapps.taskmanagement.exception` | Exceptions metier + `@RestControllerAdvice` |
| Config | `com.jihedapps.taskmanagement.config` | Bean `Clock`, jeu de donnees de demonstration |

Modele de domaine : `User(role)` -> `Project` -> `Task(assignee, status, priority, deadline)` -> `Comment`.

### Authentification simplifiee

Pas de Spring Security dans cette demonstration : l'identite de l'appelant est transmise via
l'en-tete HTTP `X-User-Id` (id d'un `User` existant). Chaque service verifie le role et la
relation avec la ressource a partir de cet id. C'est un choix assume pour rester dans le
perimetre "macro-project" sans y consacrer un chantier d'authentification complet — voir
limitations ci-dessous.

## Lancer le projet

```bash
mvn spring-boot:run
```

L'application demarre sur `http://localhost:8081`. Une base H2 fichier est creee dans
`./data/taskdb.mv.db` au premier lancement, avec deux utilisateurs de demonstration
(`admin`, id 1, role ADMIN ; `jihed`, id 2, role MEMBER).

Console H2 : `http://localhost:8081/h2-console` (JDBC URL `jdbc:h2:file:./data/taskdb`, user `sa`,
mot de passe vide).

## Endpoints principaux

| Methode | Endpoint | Regle |
|---|---|---|
| `GET /api/users` | Liste des utilisateurs | libre |
| `POST /api/projects` | Creer un projet | `X-User-Id` doit etre un ADMIN |
| `GET /api/projects` | Lister les projets | libre |
| `POST /api/tasks` | Creer une tache | ADMIN ou MEMBER |
| `PUT /api/tasks/{id}` | Modifier une tache | ADMIN, ou MEMBER si `assignee == X-User-Id` |
| `DELETE /api/tasks/{id}` | Supprimer une tache | ADMIN uniquement |
| `GET /api/tasks/overdue` | Taches en retard (deadline depassee, statut != DONE) | libre |
| `POST /api/tasks/{taskId}/comments` | Commenter une tache | utilisateur existant |

Exemple :

```bash
curl -X POST http://localhost:8081/api/projects \
  -H "X-User-Id: 1" -H "Content-Type: application/json" \
  -d '{"name":"Migration cloud","description":"Refonte infra"}'
```

## Regles metier couvertes par les tests

- `TaskServiceTest` : un ADMIN peut modifier/supprimer n'importe quelle tache ; un MEMBER ne peut
  modifier que les taches qui lui sont assignees (et ne peut jamais supprimer) ; detection des
  taches en retard (`deadline < now && status != DONE`), y compris le cas DONE jamais en retard.
- `ProjectServiceTest` : seul un ADMIN peut creer un projet.

## Limitations connues

- Pas d'authentification reelle (JWT/session) : `X-User-Id` est un en-tete de confiance, a ne
  jamais reproduire tel quel en production.
- Pas de pagination sur les listes (`GET /api/tasks`, `/api/projects`) : acceptable au volume de
  demonstration, a ajouter avant un usage reel.
- Pas de suppression en cascade documentee au-dela de JPA par defaut : supprimer un projet avec
  des taches actives n'est pas gere explicitement.
- Frontend : page statique d'information uniquement (`src/main/resources/static/index.html`),
  pas d'UI de gestion — usage prevu via API/HTTP client, conformement au choix de stack du
  palier "macro-projects".
