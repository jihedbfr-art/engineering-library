# task-management-platform — CLAUDE.md

Plateforme de gestion de projets/taches avec deux roles (ADMIN, MEMBER) et regles
d'autorisation appliquees en couche service. Voir `README.md` pour le detail fonctionnel.

## Modules

| Repertoire | Role |
|---|---|
| `src/main/java/.../entity` | User, Role, Project, Task, TaskStatus, TaskPriority, Comment |
| `src/main/java/.../repository` | Spring Data JPA (UserRepository, ProjectRepository, TaskRepository, CommentRepository) |
| `src/main/java/.../service` | Logique metier + autorisations (UserService, ProjectService, TaskService, CommentService) |
| `src/main/java/.../controller` | Endpoints REST (`/api/users`, `/api/projects`, `/api/tasks`, `/api/tasks/{id}/comments`) |
| `src/main/java/.../dto` | Records de requete/reponse (isolent l'entite JPA de l'API) |
| `src/main/java/.../exception` | `ResourceNotFoundException`, `ForbiddenOperationException`, `GlobalExceptionHandler` |
| `src/main/java/.../config` | Bean `Clock` (testabilite du temps), `DemoDataInitializer` |
| `src/test/java/.../service` | Tests JUnit 5 + Mockito sur `TaskService` et `ProjectService` |

## Stack verifiee

- Spring Boot 3.2.5 (starters : web, data-jpa, validation, thymeleaf), Java 17, Maven.
- `groupId com.jihedapps`, `artifactId task-management-platform`.
- H2 fichier embarque (`jdbc:h2:file:./data/taskdb`), `ddl-auto=update`.
- Tests : JUnit 5 + Mockito (via `spring-boot-starter-test`).

## Commandes

```bash
mvn compile          # compilation
mvn test             # tests unitaires (12 tests, TaskServiceTest + ProjectServiceTest)
mvn spring-boot:run  # lancement sur le port 8081
```

PowerShell : memes commandes, `mvn` fonctionne identiquement.

## Regles specifiques au projet

- Pas de Spring Security : l'identite de l'appelant est l'en-tete `X-User-Id`, resolu dans les
  controllers puis verifie dans les services (`checkCanModify`, controles ADMIN explicites).
  Ne jamais faire confiance a cet en-tete dans un contexte reel — c'est une simplification
  documentee, pas un pattern a reproduire ailleurs dans la bibliotheque.
- Toute nouvelle regle d'autorisation doit vivre dans la couche service (jamais dans le
  controller) pour rester testable unitairement sans contexte Spring.
- `TaskService` recoit un `Clock` injecte (pas `LocalDateTime.now()` en dur) pour permettre de
  fixer le temps dans les tests de detection de retard.
- Le fichier H2 (`./data/taskdb.mv.db`) et `target/` ne doivent pas etre committes (aucun git sur
  ce PC de toute facon, mais applicable si le projet est copie ailleurs).
