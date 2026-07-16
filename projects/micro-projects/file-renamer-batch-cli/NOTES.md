# file-renamer-batch-cli

Renommage en masse de fichiers (préfixe/suffixe/regex), dry-run par défaut.

- **Stack** : Python 3 stdlib (`os`, `re`, `argparse`). Aucune dépendance.
- **Lancer** : `python file_renamer.py ./dossier --prefix "x_"` (dry-run) puis rajouter
  `--apply` pour appliquer réellement.
- **Tester rapidement** : créer un dossier avec 1 fichier, lancer sans `--apply` -> doit
  seulement afficher le plan, fichier inchangé sur le disque.
- **Fichier clé** : `file_renamer.py` (script unique), fonction `compute_new_name`.
- **Points d'attention** : **`--apply` est requis pour toute modification réelle**, comportement
  par défaut = dry-run pur (aucun `os.rename` appelé). Les collisions de nom cible existant
  sont ignorées avec un message d'erreur, pas d'écrasement silencieux.
