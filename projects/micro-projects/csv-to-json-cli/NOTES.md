# csv-to-json-cli

Convertisseur CSV → JSON (liste d'objets), en-têtes automatiques via `csv.DictReader`.

- **Stack** : Python 3 stdlib (`csv`, `json`, `argparse`). Aucune dépendance.
- **Lancer** : `python csv_to_json.py fichier.csv` ou `cat fichier.csv | python csv_to_json.py`
- **Tester rapidement** : `printf 'a,b\n1,2\n' | python csv_to_json.py`
- **Fichier clé** : `csv_to_json.py` (script unique, fonction `convert`).
- **Points d'attention** : toutes les valeurs sortent en string (comportement standard `csv.DictReader`, pas de typage auto).
