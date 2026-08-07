# 0001 — Pourquoi Keycloak plutôt qu'un JWT maison ?

- **Statut** : Accepté
- **Date** : 2026-07-12
- **Contexte projet** : projects/notes-app-microservices

## Contexte
Le backend Spring Boot doit authentifier les utilisateurs et sécuriser ses endpoints. En
microservices, plusieurs services devront valider la même identité. Il faut aussi gérer inscription,
reset de mot de passe, rôles, et à terme du SSO — sans réinventer une usine à sécurité.

## Options envisagées
| Option | Avantages | Inconvénients |
|---|---|---|
| JWT maison (Spring Security + signature interne) | Zéro dépendance externe, contrôle total | On réimplémente refresh tokens, révocation, MFA, reset password, rotation de clés — surface de bugs sécurité énorme |
| Keycloak (OIDC) | Serveur d'identité prêt : OIDC, rôles, MFA, console admin, rotation JWKS | Une brique de plus à opérer, courbe d'apprentissage |
| IdP managé (Auth0/Cognito) | Zéro ops | Coût récurrent, dépendance cloud, moins de contrôle en dev local |

## Décision
**Keycloak**, avec le backend en **OAuth2 Resource Server** (`spring-boot-starter-oauth2-resource-server`).
Facteur décisif : la sécurité d'identité est un domaine où « fait maison » = dette et risque. Keycloak
émet les tokens, gère les clés (JWKS), et chaque service se contente de **valider** — pas de secret partagé.

## Conséquences
- ✅ Les services restent stateless : ils valident la signature via l'endpoint JWKS de Keycloak.
- ✅ Inscription, reset, MFA, rôles → délégués à Keycloak, pas de code métier de sécurité.
- ⚠️ Keycloak devient une dépendance critique de démarrage (présent dans docker-compose).
- 🔁 Engage à : externaliser la config realm (`keycloak/realm-export.json`) et versionner les rôles.

## Références
- [security-patterns/oauth2-keycloak.md](../security-patterns/oauth2-keycloak.md)
- [engineering-cookbook/jwt-resource-server-spring.md](../engineering-cookbook/jwt-resource-server-spring.md)
