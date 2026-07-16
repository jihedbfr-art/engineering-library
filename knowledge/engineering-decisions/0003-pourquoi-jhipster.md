# 0003 — Pourquoi JHipster comme base de génération pour les microservices Spring Boot ?

- **Statut** : Accepté
- **Date** : 2026-01-15
- **Contexte projet** : projects/notes-app-microservices

## Contexte
Un projet de plateforme microservices (Spring Boot + Angular + Keycloak + Eureka + Kafka) démarre
avec un ensemble de choix transverses répétitifs à chaque nouveau service : structure Maven,
configuration Spring Cloud (Gateway, Eureka, OpenFeign), intégration JPA/Liquibase, sécurité
OAuth2/OIDC. Écrire ce socle à la main pour chaque microservice coûte du temps et introduit des
incohérences entre services.

## Options envisagées
| Option | Avantages | Inconvénients |
|---|---|---|
| Écrire chaque microservice à la main | Contrôle total, zéro dépendance de génération | Répétition du socle, incohérences entre services, temps de démarrage plus long |
| JHipster | Génère un socle Spring Boot/Spring Cloud cohérent, intégration Keycloak/JPA/Liquibase prête à l'emploi | Courbe d'apprentissage de ses conventions, code généré à comprendre avant de le personnaliser |
| Framework maison interne | Adapté sur mesure à l'équipe | Coût de développement et de maintenance du générateur lui-même |

## Décision
JHipster est retenu comme base de génération. Le facteur décisif : la cohérence immédiate entre
microservices (même structure, mêmes conventions de sécurité et de persistance) l'emporte sur le
coût d'apprentissage de ses conventions, d'autant que Spring Boot/Spring Cloud/Keycloak sont déjà
la stack cible indépendamment de JHipster.

## Conséquences
- ✅ Positives : nouveaux microservices bootstrappés rapidement avec une base homogène (sécurité,
  persistance, API Gateway déjà câblés).
- ⚠️ Négatives / dette acceptée : une partie du code généré (configuration Liquibase initiale,
  structure des tests) doit être comprise avant d'être personnalisée — risque de "code cargo-culté"
  si l'équipe ne comprend pas ce que JHipster a généré.
- 🔁 Ce que ça nous engage à faire ensuite : documenter, pour chaque service, les écarts entre le
  socle généré et le code personnalisé (cf. `CLAUDE.md` de chaque projet).

## Références
- [engineering-decisions/0001-pourquoi-keycloak.md](0001-pourquoi-keycloak.md)
- [architecture-library/microservices.md](../architecture-library/microservices.md)
