# engineering-decisions — ADR

Architecture Decision Records. Très peu de développeurs publient ça — c'est pourtant le travail
quotidien d'un architecte. Chaque fichier répond à **une** question : « Pourquoi X ? ».

## Convention

- Nom : `NNNN-titre-court.md` (numéro croissant, ex. `0001-pourquoi-keycloak.md`).
- Un ADR est **immuable** une fois `Accepté`. Pour changer d'avis, on écrit un nouvel ADR qui
  supersède l'ancien (et on met à jour le statut de l'ancien en `Remplacé par NNNN`).
- Statuts : `Proposé` · `Accepté` · `Remplacé` · `Déprécié`.

## Index

| # | Décision | Statut |
|---|---|---|
| 0001 | [Pourquoi Keycloak plutôt qu'un JWT maison](0001-pourquoi-keycloak.md) | Accepté |
| 0002 | [Pourquoi PostgreSQL](0002-pourquoi-postgresql.md) | Accepté |

## Questions à documenter (backlog)
Pourquoi Kafka · Pourquoi Redis · Pourquoi REST vs GraphQL · Pourquoi Docker · Pourquoi Eureka ·
Pourquoi MinIO · Pourquoi Liquibase/Flyway. Utiliser la skill **ajouter-entree-savoir**.
