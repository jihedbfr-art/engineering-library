# json-to-csv-cli

Convertisseur JSON (liste d'objets) vers CSV. Inverse de `csv-to-json-cli`.

- **Stack** : Python 3 stdlib (`json`, `csv`, `argparse`). Aucune dépendance.
- **Lancer** : `python json_to_csv.py data.json -o data.csv`
- **Tester rapidement** : un JSON `[{"a":1}]` doit produire un CSV avec en-tête `a` et ligne `1`.
- **Fichier clé** : `json_to_csv.py` (script unique).
- **Points d'attention** : le JSON doit être une liste d'objets (pas un objet racine). Les colonnes
  sont l'union ordonnée des clés vues dans tous les objets ; clé manquante -> cellule vide.
