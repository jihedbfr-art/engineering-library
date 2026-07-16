# unit-converter-cli

Convertit longueur (m/km/mile/ft), poids (kg/lb) et température (C/F/K) via sous-commandes.

## Stack

Python 3, stdlib uniquement (`argparse`).

## Lancer / tester

```bash
python unit_converter.py length 1 mile ft
python unit_converter.py weight 1 lb kg
python unit_converter.py temperature 273.15 K C
```

## Fichiers clés

- `unit_converter.py` — 3 sous-commandes (`length`, `weight`, `temperature`), conversion via
  unité pivot (mètre / kg) sauf température (pivot = Celsius, formules directes).

## Points d'attention

- Chaque sous-commande valide ses unités via `choices=` argparse (erreur claire si unité invalide).
