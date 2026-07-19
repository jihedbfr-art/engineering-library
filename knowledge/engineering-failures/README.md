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
| Memory leak (JVM) | [memory-leak.md](memory-leak.md) | ✅ |
| OOM / `OOMKilled` | [oom.md](oom.md) | ✅ |
| Connection leak (pool JDBC) | [connection-leak.md](connection-leak.md) | ✅ |
| Split brain (double leader) | [split-brain.md](split-brain.md) | ✅ |
| Circular dependency (beans Spring) | [circular-dependency.md](circular-dependency.md) | ✅ |
| Broken deployment (migration vs rolling deploy) | [broken-deployment.md](broken-deployment.md) | ✅ |
| Wrong transaction (`@Transactional` ignoré) | [wrong-transaction.md](wrong-transaction.md) | ✅ |
| Thread starvation (pool de threads épuisé) | [thread-starvation.md](thread-starvation.md) | ✅ |

## Backlog
Ajouter via la skill **ajouter-entree-savoir**.
