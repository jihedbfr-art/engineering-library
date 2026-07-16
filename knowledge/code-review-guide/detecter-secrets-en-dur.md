# Détecter un secret en dur en revue de code

## 🔎 Quoi chercher
Une chaîne qui ressemble à une clé API, un mot de passe, un token ou une chaîne de connexion
directement écrite dans le code source ou un fichier de configuration versionné — au lieu d'être
lue depuis une variable d'environnement ou un gestionnaire de secrets.

## 💥 Pourquoi ça compte
Un secret commité, même supprimé dans un commit ultérieur, reste visible dans l'historique git —
sur un dépôt public, c'est immédiatement exploitable ; sur un dépôt privé, ça reste un risque en
cas de fuite ou de changement de visibilité du dépôt plus tard.

## ❌ À rejeter
```yaml
spring:
  datasource:
    password: SuperSecret123!
  ai:
    anthropic:
      api-key: sk-ant-xxxxxxxxxxxx
```

## ✅ Accepté
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD}
  ai:
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
```
Avec les valeurs réelles injectées via variables d'environnement, un vault (Docker secrets,
Kubernetes Secrets) ou un gestionnaire dédié — jamais dans un fichier suivi par git (cf. le
`.gitignore` de ce dépôt qui exclut déjà `.env`, `*.local`, `application-local.properties`).
