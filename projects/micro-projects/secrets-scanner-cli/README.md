# secrets-scanner-cli

Scanne recursivement un dossier a la recherche de patterns de secrets probables (cles AWS,
API keys, cles privees PEM) - une version minimale du principe derriere Gitleaks/TruffleHog.

## Lancer

```bash
javac SecretsScanner.java && java SecretsScanner ./src
```
