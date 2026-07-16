# lorem-ipsum-generator-cli

Générateur de faux texte latin pour maquettes/tests.

- **Stack** : Python 3 stdlib (`random`, `argparse`). Aucune dépendance.
- **Lancer** : `python lorem_gen.py -p 2` (paragraphes) ou `python lorem_gen.py -w 20` (mots).
- **Tester rapidement** : `python lorem_gen.py --seed 1 -w 5` doit toujours produire la même sortie.
- **Fichier clé** : `lorem_gen.py`, liste `WORDS` codée en dur en tête de fichier.
- **Points d'attention** : `-p` et `-w` sont mutuellement exclusifs (`add_mutually_exclusive_group`).
  Sans option, mode paragraphes par défaut (3 paragraphes).
