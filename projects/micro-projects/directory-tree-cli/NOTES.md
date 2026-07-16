# directory-tree-cli

Affichage d'arborescence de dossier façon `tree`.

- **Stack** : Python 3 stdlib (`os`, `argparse`). Aucune dépendance.
- **Lancer** : `python dir_tree.py ./dossier -L 2`
- **Tester rapidement** : `python dir_tree.py .` doit lister le contenu du dossier courant
  avec les connecteurs `├──`/`└──`.
- **Fichier clé** : `dir_tree.py`, fonction récursive `walk`.
- **Points d'attention** : tri alphabétique insensible à la casse. Fichiers/dossiers cachés
  (préfixe `.`) exclus par défaut, activables avec `-a`. `PermissionError` sur un sous-dossier
  ignoré silencieusement (dossier vide affiché).
