# retry-backoff-simulator-cli

Affiche la progression d'un exponential backoff (avec jitter) pour N tentatives - utile pour
visualiser le comportement d'un client HTTP resilient avant de l'implementer en vrai.

## Lancer

```bash
javac BackoffSimulator.java && java BackoffSimulator 6 200
```
