# uuid-generator-cli

Générateur d'UUID v4 en ligne de commande.

- **Stack** : Python 3 stdlib (`uuid`, `argparse`). Aucune dépendance.
- **Lancer** : `python uuid_gen.py -n 5`
- **Tester rapidement** : `python uuid_gen.py` doit afficher un UUID v4 valide (36 caractères, tirets).
- **Fichier clé** : `uuid_gen.py` (script unique).
- **Points d'attention** : `-n` doit être >= 1, sinon `argparse.error` avec message explicite.
