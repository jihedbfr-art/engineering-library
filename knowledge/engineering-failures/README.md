# engineering-failures — Bibliothèque des pannes

Extrêmement rare, extrêmement précieux. Chaque fichier = **une** panne réellement vécue, disséquée
pour qu'on la reconnaisse et la corrige vite la prochaine fois.

## Structure d'une entrée
Cause · Symptômes · Comment diagnostiquer (commandes/logs) · Solution · Prévention. Voir [`_TEMPLATE.md`](_TEMPLATE.md).

## Index

| Panne | Fichier | État |
|---|---|---|
| N+1 (Hibernate) | [hibernate-n-plus-1.md](hibernate-n-plus-1.md) | ✅ |
| Rebalance storm (Kafka) | [kafka-consumer-rebalance-storm.md](kafka-consumer-rebalance-storm.md) | ✅ |
| Cache stampede (Redis) | [redis-cache-stampede.md](redis-cache-stampede.md) | ✅ |
| Pauses GC longues (JVM) | [jvm-gc-pauses.md](jvm-gc-pauses.md) | ✅ |
| Double réservation (race condition) | [race-condition-double-booking.md](race-condition-double-booking.md) | ✅ |
| Requête lente par index manquant | [missing-index-slow-query.md](missing-index-slow-query.md) | ✅ |

## Backlog
Memory leak · Split brain · Circular dependency · OOM ·
Connection leak · Broken deployment · Wrong transaction · Thread starvation.
Ajouter via la skill **ajouter-entree-savoir**.
