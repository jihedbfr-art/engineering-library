# 0002 — Pourquoi PostgreSQL ?

- **Statut** : Accepté
- **Date** : 2026-07-12
- **Contexte projet** : projects/notes-app-microservices

## Contexte
Le backend a besoin d'un stockage relationnel fiable via JPA/Hibernate : entités liées, contraintes
d'intégrité, transactions. Il faut un moteur solide, gratuit, bien supporté par Spring Data et Docker.

## Options envisagées
| Option | Avantages | Inconvénients |
|---|---|---|
| PostgreSQL | ACID solide, JSONB, extensions, gratuit, superbe support Hibernate/Docker | Tuning avancé (VACUUM, autovacuum) à apprendre à l'échelle |
| MySQL/MariaDB | Très répandu, simple | Moins riche (types, JSON, requêtes analytiques) |
| MongoDB | Flexible, sans schéma | Pas adapté à des données fortement relationnelles + transactions |

## Décision
**PostgreSQL**. Facteur décisif : les données du domaine sont relationnelles et transactionnelles, et
Postgres offre le meilleur rapport rigueur/coût/écosystème avec Spring Data JPA. JSONB reste disponible
pour les cas semi-structurés sans changer de moteur.

## Conséquences
- ✅ Contraintes d'intégrité et transactions déléguées à la base.
- ⚠️ Prévoir la stratégie de migrations de schéma (Flyway/Liquibase) — futur ADR.
- 🔁 Engage à documenter index et requêtes lentes dans [database-engineering](../database-engineering/).

## Références
- [database-engineering](../database-engineering/)
- [engineering-failures](../engineering-failures/) — voir l'entrée N+1 Hibernate.
