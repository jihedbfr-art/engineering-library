# Checklist — Avant mise en production

> À exécuter avant tout déploiement en production, au-delà de la checklist
> [before-merge](before-merge.md) déjà validée pour le code lui-même.

## Configuration & secrets
- [ ] Aucun secret en dur dans la configuration déployée (variables d'environnement/vault utilisés)
- [ ] Configuration spécifique à l'environnement (URLs, credentials) vérifiée pour cet environnement précis

## Base de données
- [ ] Migrations (Liquibase/Flyway) testées sur une copie de la base de production ou un environnement équivalent
- [ ] Migration réversible identifiée, ou plan de rollback explicite si elle ne l'est pas

## Observabilité
- [ ] Dashboards de monitoring déjà en place pour les nouvelles métriques introduites
- [ ] Alertes configurées sur les seuils critiques de la fonctionnalité déployée

## Sécurité
- [ ] Scan SAST/dépendances (SonarQube ou équivalent) sans vulnérabilité bloquante
- [ ] Nouveaux endpoints exposés vérifiés contre les règles d'autorisation attendues (Keycloak/IAM)

## 🚫 Bloquant
Migration de base non testée sur un environnement équivalent, ou secret en dur détecté : le
déploiement est bloqué tant que ces points ne sont pas résolus.
