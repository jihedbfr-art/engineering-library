# Standard — Base de données

Moteur de référence : **PostgreSQL** (ADR 0002), accès via **JPA/Hibernate**.

## Règles
- **Migrations versionnées** obligatoires (Flyway ou Liquibase) — jamais `ddl-auto=update` en prod
  (`validate` seulement). En dev on tolère `create-drop`.
- Nommage : tables et colonnes en `snake_case`, pluriel pour les tables (`notes`, `note_tags`).
- Clés primaires : `id` (identity/sequence). Clés étrangères indexées explicitement.
- Toute requête listant est **paginée** (`Pageable`) — jamais de `findAll()` non borné exposé.
- Index sur toute colonne utilisée en `WHERE`/`JOIN` fréquent ; documenter les index non triviaux dans
  [knowledge/database-engineering](../../knowledge/database-engineering/).

## Anti-patterns
- N+1 (cf. [failures/hibernate-n-plus-1.md](../../knowledge/engineering-failures/hibernate-n-plus-1.md)).
- Base partagée entre microservices (couplage caché).
- Requête SQL construite par concaténation de chaînes (injection) → requêtes paramétrées / JPQL.
