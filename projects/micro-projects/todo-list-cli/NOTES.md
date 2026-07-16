# todo-list-cli

Gestionnaire de todo-list en CLI avec sous-commandes add/list/done/remove, persistance JSON locale.

- **Stack** : Python 3 stdlib (`json`, `argparse`, `os`). Aucune dépendance.
- **Lancer** : `python todo.py add "texte"`, `python todo.py list`, `python todo.py done <id>`, `python todo.py remove <id>`
- **Tester rapidement** : `python todo.py add "test" && python todo.py list`
- **Fichier clé** : `todo.py` (persistance dans `todos.json`, créé automatiquement à côté du script).
- **Points d'attention** : `todos.json` est ignoré s'il est corrompu (retourne liste vide). IDs auto-incrémentés (max+1), pas de réutilisation après suppression.
