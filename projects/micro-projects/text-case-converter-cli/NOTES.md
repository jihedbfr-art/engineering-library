# text-case-converter-cli

Convertit un texte entre camelCase, snake_case, kebab-case, PascalCase avec détection auto du format source.

## Stack

Python 3, stdlib uniquement (`re`, `argparse`).

## Lancer / tester

```bash
python case_converter.py "some-kebab-text" --to pascal
python case_converter.py "PascalCaseExample" --to snake
```

## Fichiers clés

- `case_converter.py` — `detect_format()` détecte snake/kebab/Pascal/camel/lowercase,
  `split_words()` décompose en mots minuscules (regex de frontière pour camel/Pascal),
  `CONVERTERS` mappe `--to` vers la fonction de reconstruction.

## Points d'attention

- Gère les acronymes en Pascal/camel (ex: `HTTPServerName` -> `http`, `server`, `name`).
