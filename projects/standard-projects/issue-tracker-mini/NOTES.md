# issue-tracker-mini

Mini suivi de tickets par projet (statut + priorite). Projet de la bibliotheque
`standard-projects`, autonome, sans lien avec les autres projets du repertoire.

## Stack (verifiee dans pom.xml)

- Spring Boot 3.2.5, Java 17, groupId `com.jihedapps`
- spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation
- spring-boot-starter-thymeleaf (page HTML des tickets)
- H2 en fichier (`./data/issuetrackerdb`), pas de H2 en memoire
- Pas de frontend JS : Thymeleaf cote serveur uniquement

## Commandes

```
mvn compile          # verifier la compilation
mvn spring-boot:run  # lancer l'app sur le port 8082
mvn package           # produire le jar (target/issue-tracker-mini-0.1.0.jar)
```

## Fichiers cles

- `src/main/java/.../entity/Project.java`, `Ticket.java` : entites JPA, `Project` a une
  liste de `Ticket` en cascade (suppression d'un projet -> suppression de ses tickets)
- `src/main/java/.../entity/TicketStatus.java` (`TODO`, `IN_PROGRESS`, `DONE`)
  et `TicketPriority.java` (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`)
- `src/main/java/.../repository/TicketRepository.java` : requetes derivees pour le
  filtrage combine par projet/statut/priorite
- `src/main/java/.../service/ProjectService.java`, `TicketService.java` : logique
  metier et transactions ; `ResourceNotFoundException` levee si id inconnu
- `src/main/java/.../controller/ProjectController.java`, `TicketController.java` :
  API REST JSON (CRUD + filtrage)
- `src/main/java/.../controller/TicketBoardController.java` : pages HTML
  (`/` liste des projets, `/projects/{id}/tickets` tickets + changement de statut)
- `src/main/resources/templates/projects.html`, `tickets.html` : vues Thymeleaf
- `src/main/resources/application.properties` : datasource H2, port 8082

## Points d'attention

- `Project.tickets` est annote `@JsonIgnore` pour eviter les reponses JSON demesurees ;
  la relation inverse (`Ticket.project`) reste serialisee normalement.
- Le changement de statut a deux points d'entree : `PATCH /api/tickets/{id}/status`
  (API) et le formulaire POST de la page HTML (`TicketBoardController.changeStatus`).
- Voir `README.md` pour la liste complete des endpoints et des exemples curl.
