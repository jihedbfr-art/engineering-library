# binary-hex-converter-cli

Convertisseur de nombres entre binaire, hexadécimal, décimal et octal.

- **Stack** : Python 3 stdlib (`argparse`). Aucune dépendance.
- **Lancer** : `python base_converter.py 255` (affiche les 4 bases) ou avec `-t <base>` pour une seule.
- **Tester rapidement** : `python base_converter.py 0xFF -t dec` doit imprimer `255`.
- **Fichier clé** : `base_converter.py` (script unique).
- **Points d'attention** : base source détectée via préfixe `0x`/`0b`/`0o`, sinon décimal par défaut ; peut être forcée avec `-f`. Bases : `bin`, `hex`, `dec`, `oct`.
