# tip-calculator-cli

Calculateur de pourboire, total et répartition entre convives.

- **Stack** : Python 3 stdlib (`argparse`). Aucune dépendance.
- **Lancer** : `python tip_calculator.py 100 -p 20 -n 4`
- **Tester rapidement** : `python tip_calculator.py 50 -p 10` doit afficher un total de `55.00`.
- **Fichier clé** : `tip_calculator.py` (script unique, `compute_tip`).
- **Points d'attention** : `-p` par défaut à 15%, `-n` par défaut à 1 (pas de ligne "par personne" si n=1). Validation : montant/pourcentage positifs, au moins 1 personne.
