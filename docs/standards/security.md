# Standard — Sécurité & Protection des Données

Auth de référence : **Keycloak (OIDC)** + backend en **OAuth2 Resource Server** (ADR 0001).

## 1. Protection des Données Personnelles & Anti-Fuite (Dépôts Publics)

- **Placeholders anonymes obligatoires dans les exemples et docs** :
  - Emails dans les exemples Markdown/Code : `user@example.com` ou `your.email@example.com` (jamais d'emails réels personnels/professionnels en clair).
  - Noms dans les exemples : `John Doe` ou `Your Name`.
  - Téléphones : `+1 555-0199` ou `00000000`.
- **Anonymat strict des employeurs & clients** :
  - Aucun nom commercial d'entreprise, d'employeur ou de client (Numeryx, Ooredoo, TTN, Huawei, Nokia, etc.) dans les dépôts publics.
  - Utilisation exclusive de termes génériques (*opérateur Télécom 5G*, *plateforme BSS*, *architecture microservices Spring Cloud*).
- **Isolation des artefacts de build & secrets** :
  - Nettoyage et exclusion stricte des dossiers `target/`, `bin/`, `build/` et fichiers de secrets (`.env`, `application-local.yml`).

## 2. Règles d'Architecture Sécurité

- **Secure by default** : tout endpoint est `authenticated()` sauf exception explicitement documentée (`/actuator/health`, endpoints publics listés).
- **Aucun secret en dur** dans le code ou le repo : mots de passe, clés, tokens → variables d'environnement.
- Valider `iss` et l'expiration des JWT (fait par `issuer-uri`) ; mapper les rôles depuis `realm_access.roles`.
- Entrées utilisateur toujours validées et échappées ; requêtes paramétrées uniquement.
- CORS restreint aux origines connues ; CSRF selon le type de client (stateless API → souvent désactivé, documenté).
- HTTPS de bout en bout en prod (terminaison nginx).

## 3. Avant de Pousser (Checklist)

- `grep` des secrets et emails réels (`password`, `secret`, `token`, `*.gmail.com`, `*@numeryx.fr`).
- Vérifier que `target/` et `bin/` sont absents de `git status`.
- Passer [engineering-checklists/before-merge.md](../../knowledge/engineering-checklists/before-merge.md).

## 4. Références

- [security-patterns](../../knowledge/security-patterns/) · OWASP Top 10.
