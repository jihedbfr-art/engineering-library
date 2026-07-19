# security-patterns

Patterns de sécurité appliqués, pas de la théorie OWASP recopiée. Un fichier par pattern/menace.

## Structure d'une entrée
Menace/objectif · Principe · Mise en œuvre (code/config) · Erreurs classiques · Vérification. Voir [`_TEMPLATE.md`](_TEMPLATE.md).

## Index

| Pattern | Fichier | État |
|---|---|---|
| OAuth2 + Keycloak (resource server) | [oauth2-keycloak.md](oauth2-keycloak.md) | ✅ |
| Anti-bruteforce (Keycloak) | [anti-bruteforce-keycloak.md](anti-bruteforce-keycloak.md) | ✅ |
| MFA / step-up (Keycloak) | [mfa-step-up-keycloak.md](mfa-step-up-keycloak.md) | ✅ |
| CORS | [cors.md](cors.md) | ✅ |
| JWT | [jwt.md](jwt.md) | ✅ |
| CSRF | [csrf.md](csrf.md) | ✅ |

## Backlog
RBAC · ABAC · OWASP Top 10 · SQL injection · XSS · XXE · SSRF.
Ajouter via la skill **ajouter-entree-savoir**.
