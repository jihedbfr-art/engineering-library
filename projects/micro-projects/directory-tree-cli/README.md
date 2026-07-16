# directory-tree-cli

Affiche l'arborescence d'un dossier façon commande `tree`, avec option de profondeur
maximale et affichage des fichiers cachés.

## Lancer

```bash
python dir_tree.py
python dir_tree.py ./mon-projet -L 2
python dir_tree.py ./mon-projet -a
```

## Exemple d'usage

```bash
$ python dir_tree.py ./demo
./demo
├── src
│   ├── main.py
│   └── utils.py
└── README.md

1 dossier(s), 3 fichier(s)
```

`-L/--max-depth` limite la profondeur affichée. `-a/--all` inclut les entrées cachées
(préfixées par un point), exclues par défaut.
