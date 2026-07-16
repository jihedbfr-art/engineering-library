# Helpdesk Ticket System

API REST de gestion de tickets de support, avec un workflow de statuts strict et un
reporting du temps moyen de resolution par agent.

## But

Simuler un outil de support type Zendesk simplifie : des agents traitent des tickets qui
suivent un cycle de vie impose (pas de raccourci de statut), un superviseur peut
reassigner un ticket a un autre agent, et on peut mesurer la performance de resolution
par agent.

## Architecture

Monolithe Spring Boot en couches, module Maven unique :

| Couche | Package | Role |
|---|---|---|
| Controller | `com.jihedapps.helpdesk.controller` | Endpoints REST, mapping DTO <-> service, codes HTTP |
| Service | `com.jihedapps.helpdesk.service` | Regles metier (workflow, autorisation, reporting), transactions |
| Repository | `com.jihedapps.helpdesk.repository` | Acces donnees via Spring Data JPA |
| Entity | `com.jihedapps.helpdesk.entity` | Modele de domaine (Agent, Ticket, TicketHistory) |
| DTO | `com.jihedapps.helpdesk.dto` | Contrats d'entree/sortie de l'API (records) |
| Exception | `com.jihedapps.helpdesk.exception` | Exceptions metier + `@RestControllerAdvice` |
| Config | `com.jihedapps.helpdesk.config` | Bean `Clock`, jeu de donnees de demonstration |

Modele de domaine : `Agent(role)` <- assigne - `Ticket(priority, status)` -> historise dans
-> `TicketHistory(fromStatus, toStatus, changedAt)`.

### Workflow de statuts

Un ticket suit obligatoirement l'ordre `OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED`. Chaque
changement de statut ne peut avancer que d'un cran (pas de saut, ex. `OPEN -> CLOSED`
direct est refuse, et pas de retour en arriere). La regle est appliquee dans
`TicketService.checkTransition` et chaque transition reussie cree une ligne immuable dans
`TicketHistory` (jamais modifiee ni supprimee).

### Reassignation

Seul un agent avec le role `SUPERVISOR` peut reassigner un ticket a un autre agent
(`TicketService.reassign`). Un `AGENT` qui tente l'operation recoit une 403.
L'identite de l'appelant est transmise via l'en-tete HTTP `X-Agent-Id` (id d'un `Agent`
existant) — simplification assumee, voir limitations.

### Reporting

`GET /api/tickets/reports/average-resolution-time` calcule, pour chaque agent, le temps
moyen (en minutes) entre la creation d'un ticket et sa premiere transition vers
`RESOLVED`, sur les tickets qui lui sont actuellement assignes. Les tickets jamais
resolus ne comptent pas dans la moyenne.

## Lancer le projet

```bash
mvn spring-boot:run
```

L'application demarre sur `http://localhost:8082`. Une base H2 fichier est creee dans
`./data/helpdesk-db.mv.db` au premier lancement, avec trois agents de demonstration
(`sophie` id 1 SUPERVISOR, `karim` id 2 AGENT, `lea` id 3 AGENT).

Console H2 : `http://localhost:8082/h2-console` (JDBC URL `jdbc:h2:file:./data/helpdesk-db`,
user `sa`, mot de passe vide).

## Endpoints principaux

| Methode | Endpoint | Regle |
|---|---|---|
| `POST /api/agents` | Creer un agent | libre |
| `GET /api/agents` | Lister les agents | libre |
| `POST /api/tickets` | Creer un ticket (statut initial OPEN) | libre |
| `GET /api/tickets` | Lister les tickets (filtrable par `agentId`) | libre |
| `GET /api/tickets/{id}` | Detail d'un ticket | libre |
| `PUT /api/tickets/{id}/status` | Changer le statut | doit respecter le workflow sequentiel |
| `PUT /api/tickets/{id}/reassign` | Reassigner a un autre agent | `X-Agent-Id` doit etre un SUPERVISOR |
| `GET /api/tickets/reports/average-resolution-time` | Temps moyen de resolution par agent | libre |

## Limitations

- Pas de Spring Security : l'identite de l'appelant pour la reassignation repose sur
  l'en-tete `X-Agent-Id`, non verifie par un mecanisme d'authentification. C'est une
  simplification documentee pour rester dans le perimetre "macro-project", pas un pattern
  a reproduire dans un contexte reel.
- Pas de pagination sur les listes (`GET /api/tickets`, `GET /api/agents`) : acceptable au
  volume d'un demonstrateur, a revoir si le jeu de donnees grandit.
- Le reporting recalcule tout a la volee a chaque appel (pas de vue materialisee ni de
  cache) : suffisant pour le volume vise ici.
