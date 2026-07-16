# todo-list-cli

Gestionnaire de todo-list en ligne de commande avec sous-commandes `add`/`list`/`done`/`remove`,
persistance simple dans un fichier local `todos.json` (créé automatiquement à côté du script).

## Lancer

```bash
python todo.py add "Écrire le rapport"
python todo.py list
python todo.py done 1
python todo.py remove 1
```

## Exemple d'usage

```bash
$ python todo.py add "Acheter du café"
Ajouté #1: Acheter du café

$ python todo.py add "Répondre aux emails"
Ajouté #2: Répondre aux emails

$ python todo.py list
[ ] #1: Acheter du café
[ ] #2: Répondre aux emails

$ python todo.py done 1
Terminé #1: Acheter du café

$ python todo.py list --pending-only
[ ] #2: Répondre aux emails

$ python todo.py remove 2
Supprimé #2
```
