# slug-generator-cli

Convertit un texte en slug URL-friendly (minuscules, tirets, sans accents/caractères spéciaux).

## Stack

Python 3, stdlib uniquement (`re`, `unicodedata`, `argparse`).

## Lancer / tester

```bash
python slug_generator.py "Mon Article : Guide Complet !"
```

## Fichiers clés

- `slug_generator.py` — `slugify()` : normalisation NFKD + suppression des diacritiques,
  minuscules, remplacement du non-alphanumérique par `-`, fusion/trim des tirets.

## Points d'attention

- Gère correctement les accents français (é, à, ç...) via `unicodedata.normalize("NFKD", ...)`.
