# anagram-checker-cli

Vérificateur d'anagrammes avec deux sous-commandes : `check` (deux mots) et `find` (liste de mots).

- **Stack** : Python 3 stdlib (`argparse`). Aucune dépendance.
- **Lancer** : `python anagram_checker.py check "Listen" "Silent"` ou `python anagram_checker.py find words.txt`
- **Tester rapidement** : `python anagram_checker.py check abc bca` doit confirmer une anagramme.
- **Fichier clé** : `anagram_checker.py` (script unique, `normalize` / `is_anagram` / `find_anagram_groups`).
- **Points d'attention** : normalisation = minuscules + suppression de tout ce qui n'est pas alphanumérique. `find` lit un mot par ligne et regroupe par signature triée. Exit code 1 si `check` échoue.
