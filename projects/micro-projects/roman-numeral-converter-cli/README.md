# roman-numeral-converter-cli

Convertit un entier en chiffres romains et inversement, en ligne de commande. Détecte
automatiquement le sens de la conversion selon que l'argument est numérique ou non, et
valide les bornes standard (1 à 3999).

## Lancer

```bash
python roman_numeral.py 1994
python roman_numeral.py MCMXCIV
```

## Exemple d'usage

```bash
$ python roman_numeral.py 1994
MCMXCIV

$ python roman_numeral.py MCMXCIV
1994

$ python roman_numeral.py 4000
Erreur: le nombre doit être compris entre 1 et 3999
```
