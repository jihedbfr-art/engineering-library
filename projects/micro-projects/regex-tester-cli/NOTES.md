# regex-tester-cli

Testeur de regex en ligne de commande : affiche matches, positions et groupes capturés.

- **Stack** : Python 3 stdlib (`re`, `argparse`). Aucune dépendance.
- **Lancer** : `python regex_tester.py "\d+" -t "texte à tester"`
- **Tester rapidement** : `python regex_tester.py "a+" -t "aaa bbb aa"` doit donner 2 matches.
- **Fichier clé** : `regex_tester.py` (script unique, utilise `re.finditer`).
- **Points d'attention** : erreur de pattern regex gérée proprement (`re.error`), exit code 1 si aucun match ou pattern invalide.
