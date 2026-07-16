# text-diff-cli

Comparateur de fichiers texte ligne par ligne, façon `diff`.

- **Stack** : Python 3 stdlib (`difflib`). Aucune dépendance.
- **Lancer** : `python text_diff.py fichier1.txt fichier2.txt` (ajouter `-u` pour le format unifié).
- **Tester rapidement** : sur deux fichiers différents, le script doit lister les lignes `-`/`+` et sortir avec le code 1.
- **Fichier clé** : `text_diff.py` (script unique).
- **Points d'attention** : mode par défaut = `ndiff` (marqueurs `-`/`+`/espace), mode `-u` = diff unifié avec en-têtes `---`/`+++`. Exit code 1 si différences détectées (pratique en pipeline).
