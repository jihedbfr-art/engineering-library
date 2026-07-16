# Standard — Sécurité

Auth de référence : **Keycloak (OIDC)** + backend en **OAuth2 Resource Server** (ADR 0001).

## Règles
- **Secure by default** : tout endpoint est `authenticated()` sauf exception explicitement documentée
  (`/actuator/health`, endpoints publics listés).
- **Aucun secret en dur** dans le code ou le repo : mots de passe, clés, tokens → variables d'environnement.
- Valider `iss` et l'expiration des JWT (fait par `issuer-uri`) ; mapper les rôles depuis `realm_access.roles`.
- Entrées utilisateur toujours validées et échappées ; requêtes paramétrées uniquement.
- CORS restreint aux origines connues ; CSRF selon le type de client (stateless API → souvent désactivé, documenté).
- HTTPS de bout en bout en prod (terminaison nginx).

## Avant de pousser
- `grep` des secrets potentiels (`password`, `secret`, `token`, clés privées).
- Passer [engineering-checklists/before-merge.md](../../knowledge/engineering-checklists/before-merge.md).

## Références
- [security-patterns](../../knowledge/security-patterns/) · OWASP Top 10 (backlog).
