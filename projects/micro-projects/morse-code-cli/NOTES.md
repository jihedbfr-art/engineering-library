# morse-code-cli

Encodeur/décodeur de code Morse.

- **Stack** : Python 3 stdlib (`argparse`). Aucune dépendance.
- **Lancer** : `python morse_tool.py -e "SOS"` ou `python morse_tool.py -d "... --- ..."`
- **Tester rapidement** : `python morse_tool.py -e "SOS"` doit afficher `... --- ...`.
- **Fichier clé** : `morse_tool.py`, dictionnaire `MORSE_TABLE` codé en dur + `REVERSE_TABLE` dérivé.
- **Points d'attention** : séparateur de mots `" / "` en Morse, espace simple entre lettres.
  Les caractères non reconnus à l'encodage sont ignorés silencieusement.
