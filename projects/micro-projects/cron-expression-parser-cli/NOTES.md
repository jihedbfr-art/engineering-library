# cron-expression-parser-cli

Parseur cron manuel (5 champs) avec calcul des prochaines exécutions.

- **Stack** : Python 3 stdlib (`datetime`, `argparse`). Aucune dépendance (pas de `croniter`).
- **Lancer** : `python cron_parser.py "0 9 * * 1-5" -n 5`
- **Tester rapidement** : `python cron_parser.py "* * * * *" -n 1` doit donner la minute suivante.
- **Fichier clé** : `cron_parser.py`, fonctions `parse_field` (parsing d'un champ) et
  `next_runs` (recherche par avancement minute par minute, limite de sécurité ~5 ans).
- **Points d'attention** : jour de semaine cron (0/7=dimanche) converti depuis
  `datetime.isoweekday() % 7`, différent de `datetime.weekday()` (0=lundi). Recherche naïve
  minute par minute — correct mais pas optimisé pour de très longs horizons.
