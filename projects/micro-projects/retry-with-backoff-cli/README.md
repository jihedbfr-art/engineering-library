# retry-with-backoff-cli

Retry avec exponential backoff + jitter (delai qui double a chaque essai, plus un peu d'aleatoire
pour eviter que plusieurs clients ne retentent tous exactement au meme moment).

## Lancer
```bash
python retry_backoff.py
```
