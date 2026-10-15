# Recettes de diagnostic

[English version](README.md)

Partir du symptôme observé, puis suivre une procédure ciblée pour trouver et corriger sa cause.

## Structure d'une entrée

Symptôme exact, causes probables par ordre de fréquence, commandes de diagnostic et correctif. Voir [`_TEMPLATE.md`](_TEMPLATE.md).

## Cas vérifiés

- [Claims de rôles Keycloak malformés](keycloak-role-claims/README.fr.md) : entrées synthétiques, tests de régression et portée du correctif combiné.
- [Disponibilité Docker de Keycloak](keycloak-readiness/README.fr.md) : un realm répond HTTP 200 alors que la sonde échoue en 404 ; diagnostiquer le endpoint de gestion avant de redémarrer.
- [Image de test introuvable](image-testconteneurs-introuvable.md) : distinguer erreur de registre et échec applicatif, puis vérifier le comportement du substitut.

## Backlog

Spring Boot ne démarre pas, `Bean not found`, dépendance circulaire, `LazyInitializationException`, JWT invalide, consumer Kafka bloqué, conteneur Docker qui s'arrête, Kubernetes `CrashLoopBackOff`, timeout Oracle et deadlock Postgres. Ajouter les entrées via **ajouter-entree-savoir**.

---

<div align="center">
  <img src="../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>Un projet <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a></sub>
</div>
