# text-diff-cli

Compare deux fichiers texte ligne par ligne et affiche les différences façon `diff`,
en s'appuyant sur `difflib` de la stdlib. Supporte un mode unifié (`-u`) avec contexte.

## Lancer

```bash
python text_diff.py fichier1.txt fichier2.txt
python text_diff.py fichier1.txt fichier2.txt -u
```

## Exemple d'usage

```bash
$ python text_diff.py f1.txt f2.txt
  a
- b
+ x
  c
```

Code de sortie 0 si les fichiers sont identiques, 1 sinon (utile en script).
