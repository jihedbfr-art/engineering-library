# word-counter-cli

Compteur mots/lignes/caractères + top 10 mots fréquents pour un fichier texte.

- **Stack** : Python 3 stdlib (`re`, `collections.Counter`, `argparse`). Aucune dépendance.
- **Lancer** : `python word_counter.py fichier.txt` ou `cat fichier.txt | python word_counter.py`
- **Tester rapidement** : `printf 'a a b\n' | python word_counter.py`
- **Fichier clé** : `word_counter.py` (fonction `analyze`).
- **Points d'attention** : découpage des mots via regex incluant accents (À-ÖØ-öø-ÿ) et apostrophes ; tout en minuscule pour le comptage.
