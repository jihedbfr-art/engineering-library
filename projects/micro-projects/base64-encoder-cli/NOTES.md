# base64-encoder-cli

Encodeur/décodeur base64 pour texte ou fichier, en ligne de commande.

- **Stack** : Python 3 stdlib (`base64`, `argparse`). Aucune dépendance.
- **Lancer** : `python base64_tool.py --encode -t "texte"` ou `python base64_tool.py --decode -f data.b64`
- **Tester rapidement** : `python base64_tool.py --encode -t "abc"` doit afficher `YWJj`.
- **Fichier clé** : `base64_tool.py` (script unique).
- **Points d'attention** : décodage de contenu binaire nécessite `-o` (écriture en mode `wb`), sinon erreur explicite car impossible d'afficher du binaire sur stdout proprement.
