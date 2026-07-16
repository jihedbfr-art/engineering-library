# file-renamer-batch-cli

Renomme en masse les fichiers d'un dossier via préfixe, suffixe ou remplacement regex.
**Mode dry-run par défaut** : le script n'affiche que le plan de renommage tant que
`--apply` n'est pas passé explicitement, pour éviter tout dégât accidentel.

## Lancer

```bash
python file_renamer.py ./mes_fichiers --prefix "2026_"
python file_renamer.py ./mes_fichiers --pattern "IMG_" --replace "photo_" --apply
```

## Exemple d'usage

```bash
$ python file_renamer.py ./photos --suffix "_backup"
Mode : DRY-RUN (aucune modification, utilisez --apply pour renommer)
  IMG_0001.jpg  ->  IMG_0001_backup.jpg
  IMG_0002.jpg  ->  IMG_0002_backup.jpg

$ python file_renamer.py ./photos --suffix "_backup" --apply
Mode : APPLICATION
  IMG_0001.jpg  ->  IMG_0001_backup.jpg
  IMG_0002.jpg  ->  IMG_0002_backup.jpg

2 fichier(s) renommé(s).
```

`--pattern` (regex) + `--replace` permet un remplacement libre. `--recursive` parcourt
les sous-dossiers. Sans `--apply`, aucune modification n'est faite sur le disque.
