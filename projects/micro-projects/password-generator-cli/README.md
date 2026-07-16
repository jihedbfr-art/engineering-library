# password-generator-cli

Génère un mot de passe aléatoire cryptographiquement sûr (module `secrets`) en ligne de commande,
avec contrôle de la longueur et des jeux de caractères inclus.

## Lancer

```bash
python password_generator.py --length 20 --count 3
```

## Exemple d'usage

```bash
$ python password_generator.py -l 16
Xk9$mQ2!vLpR7@wZ

$ python password_generator.py -l 12 --no-symbols --no-digits
QjrEfxNkPzTa

$ python password_generator.py -n 3 -l 10
h3K!qT9@rX
```

Options : `--length/-l`, `--no-symbols`, `--no-digits`, `--no-upper`, `--count/-n`.
