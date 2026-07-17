# openapi-lint-cli

Lint basique d'un fichier OpenAPI YAML : signale les operations (get/post/...) sans
`description` a proximite - la regle la plus utile a automatiser cote revue de PR.

## Lancer

```bash
javac OpenApiLint.java && java OpenApiLint openapi.yaml
```
