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

## Backlog
Memory leak · Deadlock · Race condition · Split brain · Cache stampede · Circular dependency · OOM ·
Slow query · Connection leak · Broken deployment · Wrong transaction · Thread starvation ·
Redis eviction · GC pauses. Ajouter via la skill **ajouter-entree-savoir**.
