# helpdesk-ticket-system — CLAUDE.md

Systeme de tickets de support avec workflow de statuts sequentiel strict, reassignation
reservee aux superviseurs et reporting du temps moyen de resolution. Voir `README.md` pour
le detail fonctionnel.

## Modules

| Repertoire | Role |
|---|---|
| `src/main/java/.../entity` | Agent, AgentRole, Ticket, TicketStatus, TicketPriority, TicketHistory |
| `src/main/java/.../repository` | Spring Data JPA (AgentRepository, TicketRepository, TicketHistoryRepository) |
| `src/main/java/.../service` | Logique metier (AgentService, TicketService : workflow, autorisation, reporting) |
| `src/main/java/.../controller` | Endpoints REST (`/api/agents`, `/api/tickets`) |
| `src/main/java/.../dto` | Records de requete/reponse + `ResolutionReport` |
| `src/main/java/.../exception` | `ResourceNotFoundException`, `ForbiddenOperationException`, `InvalidStatusTransitionException`, `GlobalExceptionHandler` |
| `src/main/java/.../config` | Bean `Clock` (testabilite du temps), `DemoDataInitializer` |
| `src/test/java/.../service` | Tests JUnit 5 + Mockito sur `TicketServiceTest` |

## Stack verifiee

- Spring Boot 3.2.5 (starters : web, data-jpa, validation, thymeleaf), Java 17, Maven.
- `groupId com.jihedapps`, `artifactId helpdesk-ticket-system`.
- H2 fichier embarque (`jdbc:h2:file:./data/helpdesk-db`), `ddl-auto=update`.
- Tests : JUnit 5 + Mockito (via `spring-boot-starter-test`).

## Commandes

```bash
mvn compile          # compilation
mvn test             # tests unitaires (TicketServiceTest)
mvn spring-boot:run  # lancement sur le port 8082
```

PowerShell : memes commandes, `mvn` fonctionne identiquement.

## Regles specifiques au projet

- Le workflow de statuts (`TicketStatus` : `OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED`) est
  verrouille sur l'ordre des constantes de l'enum via `TicketService.checkTransition` :
  seule la transition `ordinal + 1` est acceptee. Toute modification de l'ordre des valeurs
  de l'enum change directement le workflow autorise — a traiter avec prudence.
- Chaque transition de statut cree une ligne `TicketHistory` immuable (jamais update ni
  delete en code) : c'est la source de verite du reporting de temps de resolution.
- Pas de Spring Security : l'identite de l'appelant pour `PUT /api/tickets/{id}/reassign`
  est l'en-tete `X-Agent-Id`, resolu dans le controller puis verifie dans
  `TicketService.reassign`. Simplification documentee, pas un pattern a reproduire ailleurs
  dans la bibliotheque.
- `TicketService` recoit un `Clock` injecte (pas `LocalDateTime.now()` en dur) pour figer le
  temps dans les tests de workflow et de reporting.
- Le fichier H2 (`./data/helpdesk-db.mv.db`) et `target/` ne doivent pas etre committes
  (aucun git sur ce PC de toute facon, mais applicable si le projet est copie ailleurs).
