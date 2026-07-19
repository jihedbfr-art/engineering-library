# debugging-recipes

« Ça ne marche pas » → une checklist de diagnostic ciblée par symptôme. On ne cherche pas la théorie,
on cherche **la prochaine commande à taper**.

## Structure d'une entrée
Symptôme exact · Causes probables (les plus fréquentes d'abord) · Diagnostic pas-à-pas · Correctif. Voir [`_TEMPLATE.md`](_TEMPLATE.md).

## Index

| Symptôme | Fichier |
|---|---|
| Kubernetes `CrashLoopBackOff` | [kubernetes-crashloopbackoff.md](kubernetes-crashloopbackoff.md) |
| `401`/token invalide (Keycloak) | [keycloak-token-invalide.md](keycloak-token-invalide.md) |
| `LazyInitializationException` | [hibernate-lazyinitializationexception.md](hibernate-lazyinitializationexception.md) |
| Pool de connexions épuisé | [connexion-pool-epuise.md](connexion-pool-epuise.md) |
| `deadlock detected` (PostgreSQL) | [deadlock-postgres.md](deadlock-postgres.md) |

## Backlog
Spring Boot ne démarre pas · `Bean not found` · dépendance circulaire · Kafka consumer bloqué ·
conteneur Docker qui exit · timeout Oracle. Ajouter via la skill **ajouter-entree-savoir**.
