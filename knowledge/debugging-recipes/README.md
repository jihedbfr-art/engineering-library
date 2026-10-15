# debugging-recipes

« Ça ne marche pas » → une checklist de diagnostic ciblée par symptôme. On ne cherche pas la théorie,
on cherche **la prochaine commande à taper**.

## Structure d'une entrée
Symptôme exact · Causes probables (les plus fréquentes d'abord) · Diagnostic pas-à-pas · Correctif. Voir [`_TEMPLATE.md`](_TEMPLATE.md).

## Cas documentés récemment

- [Image de test introuvable avant les assertions](image-testconteneurs-introuvable.md) · [English](test-container-image-unavailable.md) : distinguer erreur de registre et échec applicatif, puis vérifier le comportement du substitut.

## Backlog
Spring Boot ne démarre pas · `Bean not found` · dépendance circulaire · `LazyInitializationException` ·
JWT invalide · Kafka consumer bloqué · conteneur Docker qui exit · Kubernetes `CrashLoopBackOff` ·
timeout Oracle · deadlock Postgres. Ajouter via la skill **ajouter-entree-savoir**.
