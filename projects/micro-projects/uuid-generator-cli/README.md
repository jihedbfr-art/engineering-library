# uuid-generator-cli

Génère des identifiants UUID v4 aléatoires en ligne de commande. Utile pour créer rapidement
des ID de test, des clés primaires ou des noms de fichiers uniques.

## Lancer

```bash
python uuid_gen.py
python uuid_gen.py -n 5
python uuid_gen.py -n 3 -u
```

## Exemple d'usage

```bash
$ python uuid_gen.py -n 3
3f2504e0-4f89-41d3-9a0c-0305e82c3301
c56a4180-65aa-42ec-a945-5fd21dec0538
a1b2c3d4-e5f6-4789-a0b1-c2d3e4f56789
```

Option `-u/--uppercase` pour afficher les UUID en majuscules.
