# roman-numeral-converter-cli

Convertisseur bidirectionnel entier <-> chiffres romains en ligne de commande.

- **Stack** : Python 3 stdlib (`argparse`). Aucune dépendance.
- **Lancer** : `python roman_numeral.py 1994` ou `python roman_numeral.py MCMXCIV`
- **Tester rapidement** : `python roman_numeral.py 9` doit imprimer `IX`.
- **Fichier clé** : `roman_numeral.py` (script unique, `int_to_roman` / `roman_to_int`).
- **Points d'attention** : bornes strictes 1-3999. Le sens de conversion est détecté automatiquement (argument numérique vs. lettres). `roman_to_int` revalide via `int_to_roman` pour rejeter les formes non canoniques (ex: IIII).
