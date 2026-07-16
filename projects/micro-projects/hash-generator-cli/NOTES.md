# hash-generator-cli

Calcule un hash (MD5/SHA1/SHA256/SHA512) d'un fichier ou d'une chaîne de texte.

## Stack

Python 3, stdlib uniquement (`hashlib`, `argparse`).

## Lancer / tester

```bash
python hash_generator.py --text "hello world" --algo sha256
python hash_generator.py --file ./README.md --algo sha1
```

## Fichiers clés

- `hash_generator.py` — `hash_file()` (lecture par chunks) et `hash_text()`, dispatch via `--algo`.

## Points d'attention

- `--file` et `--text` sont mutuellement exclusifs et l'un des deux est requis (argparse le gère).
- Lecture fichier par chunks de 64 Ko pour supporter les gros fichiers sans saturer la RAM.
