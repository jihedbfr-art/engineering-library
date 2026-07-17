# dependency-license-checker-cli

Extrait les champs `license` d'un `package.json` et signale les licences copyleft (GPL/AGPL/LGPL)
qui meritent une verification de compatibilite avant usage en entreprise.

## Lancer

```bash
javac LicenseChecker.java && java LicenseChecker package.json
```
