# http-status-lookup-cli

Table de recherche des codes de statut HTTP.

- **Stack** : Python 3 stdlib (`argparse`). Aucune dépendance.
- **Lancer** : `python http_status.py -c 404` ou `python http_status.py -k "timeout"`
- **Tester rapidement** : `python http_status.py -c 200` doit afficher `200 OK`.
- **Fichier clé** : `http_status.py`, dictionnaire `STATUS_CODES` codé en dur (~50 codes usuels).
- **Points d'attention** : `-c` et `-k` mutuellement exclusifs. Recherche par mot-clé
  insensible à la casse, sous-chaîne de la description (pas de correspondance exacte).
