# json-pretty-printer-cli

Réindenteur JSON en ligne de commande (fichier ou stdin).

- **Stack** : Python 3 stdlib (`json`, `argparse`). Aucune dépendance.
- **Lancer** : `python json_pretty.py fichier.json` ou `cat fichier.json | python json_pretty.py`
- **Tester rapidement** : `echo '{"b":1,"a":2}' | python json_pretty.py --sort-keys`
- **Fichier clé** : `json_pretty.py` (script unique).
- **Points d'attention** : erreurs JSON invalide et fichier introuvable gérées proprement (exit code 1, message sur stderr).
