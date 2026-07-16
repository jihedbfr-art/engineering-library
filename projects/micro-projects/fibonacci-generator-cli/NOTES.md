# fibonacci-generator-cli

Générateur de suite de Fibonacci, avec calcul itératif direct d'un terme précis.

- **Stack** : Python 3 stdlib (`argparse`). Aucune dépendance.
- **Lancer** : `python fibonacci.py -n 10` (séquence) ou `python fibonacci.py --term 30` (terme unique).
- **Tester rapidement** : `python fibonacci.py --term 10` doit imprimer `55`.
- **Fichier clé** : `fibonacci.py` (script unique, `fibonacci_sequence` / `fibonacci_nth`).
- **Points d'attention** : `fibonacci_nth` est itératif en O(n), pas de récursion (évite l'explosion exponentielle pour n élevé). Indexation à partir de 0 (F(0)=0, F(1)=1).
