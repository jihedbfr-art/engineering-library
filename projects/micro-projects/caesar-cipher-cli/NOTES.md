# caesar-cipher-cli

Chiffreur/déchiffreur César avec mode brute-force.

- **Stack** : Python 3 stdlib (`string`, `argparse`). Aucune dépendance.
- **Lancer** : `python caesar.py "texte" -s 3` (chiffrer/déchiffrer) ou `python caesar.py "texte" -b` (brute-force).
- **Tester rapidement** : `python caesar.py "abc" -s 1` doit afficher `bcd`.
- **Fichier clé** : `caesar.py` (script unique).
- **Points d'attention** : `-s` et `-b` mutuellement exclusifs. Casse préservée, caractères
  non alphabétiques inchangés. Décalage négatif = déchiffrement.
