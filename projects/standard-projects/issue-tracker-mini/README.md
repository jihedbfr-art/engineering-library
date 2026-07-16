# issue-tracker-mini

Suivi de tickets par projet, avec statut et priorite. Pense comme un mini Jira :
un projet regroupe des tickets, chaque ticket a un statut (TODO / IN_PROGRESS / DONE)
et une priorite (LOW / MEDIUM / HIGH / CRITICAL).

## Architecture

Application Spring Boot monolithique classique, en couches :

- `entity` : `Project`, `Ticket` (avec `TicketStatus` et `TicketPriority` en enum)
- `repository` : `ProjectRepository`, `TicketRepository` (Spring Data JPA, requetes derivees pour le filtrage)
- `service` : `ProjectService`, `TicketService` (logique metier, transactions)
- `controller` : `ProjectController` et `TicketController` (API REST JSON), `TicketBoardController` (pages HTML Thymeleaf)
- `exception` : `ResourceNotFoundException` + `GlobalExceptionHandler` (reponses d'erreur JSON uniformes)

Persistance H2 en fichier (`./data/issuetrackerdb`), donc les donnees survivent aux redemarrages.

## Lancement

```
mvn spring-boot:run
```

L'application demarre sur `http://localhost:8082`.

- Page d'accueil (liste des projets) : `http://localhost:8082/`
- Console H2 : `http://localhost:8082/h2-console` (JDBC URL `jdbc:h2:file:./data/issuetrackerdb`, user `sa`, pas de mot de passe)

Aucun projet n'est cree par defaut : passez par l'API REST pour en creer un, puis
naviguez vers `/projects/{id}/tickets` pour voir la page HTML.

## Endpoints REST

### Projets

| Methode | URL | Description |
|---|---|---|
| GET | `/api/projects` | Liste tous les projets |
| GET | `/api/projects/{id}` | Detail d'un projet |
| POST | `/api/projects` | Cree un projet (`{"name": "...", "description": "..."}`) |
| PUT | `/api/projects/{id}` | Met a jour un projet |
| DELETE | `/api/projects/{id}` | Supprime un projet (et ses tickets, cascade) |

### Tickets

| Methode | URL | Description |
|---|---|---|
| GET | `/api/tickets` | Liste les tickets, filtrable par `projectId`, `status`, `priority` (query params, combinables) |
| GET | `/api/tickets/{id}` | Detail d'un ticket |
| POST | `/api/tickets?projectId={id}` | Cree un ticket rattache au projet `{id}` |
| PUT | `/api/tickets/{id}` | Met a jour titre/description/statut/priorite |
| PATCH | `/api/tickets/{id}/status` | Change uniquement le statut (`{"status": "IN_PROGRESS"}`) |
| DELETE | `/api/tickets/{id}` | Supprime un ticket |

### Pages HTML

| URL | Description |
|---|---|
| `/` | Liste des projets |
| `/projects/{id}/tickets` | Tickets du projet, filtrables par statut/priorite, changement de statut via menu deroulant |

## Exemple rapide

```
curl -X POST http://localhost:8082/api/projects -H "Content-Type: application/json" -d "{\"name\":\"Site vitrine\",\"description\":\"Refonte du site public\"}"

curl -X POST "http://localhost:8082/api/tickets?projectId=1" -H "Content-Type: application/json" -d "{\"title\":\"Corriger le footer\",\"description\":\"Lien casse\",\"status\":\"TODO\",\"priority\":\"HIGH\"}"

curl "http://localhost:8082/api/tickets?projectId=1&status=TODO"
```
