# duplicate-file-finder-cli

Trouve les fichiers en doublon (contenu identique) dans un dossier via hash SHA-256.

## Stack

Python 3, stdlib uniquement (`os`, `hashlib`, `argparse`, `collections`).

## Lancer / tester

```bash
python duplicate_finder.py <dossier>
python duplicate_finder.py .   # test rapide sur le dossier courant
```

## Fichiers clés

- `duplicate_finder.py` — script unique : `hash_file()` hash un fichier par chunks,
  `find_duplicates()` groupe d'abord par taille (optimisation) puis par hash sha256.

## Points d'attention

- Optimisation : le hash n'est calculé que si au moins 2 fichiers ont la même taille.
- Gestion des erreurs de lecture (permissions, fichiers verrouillés) sans crasher le scan.
