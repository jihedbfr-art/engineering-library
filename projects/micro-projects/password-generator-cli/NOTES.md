# password-generator-cli

Générateur de mot de passe aléatoire sécurisé en ligne de commande.

- **Stack** : Python 3 stdlib (`argparse`, `secrets`, `string`). Aucune dépendance.
- **Lancer** : `python password_generator.py --length 20`
- **Tester rapidement** : `python password_generator.py -l 8 -n 5` doit imprimer 5 lignes de 8 caractères.
- **Fichier clé** : `password_generator.py` (script unique).
- **Points d'attention** : `secrets.choice` (pas `random`) pour la sécurité cryptographique. Longueur minimale forcée à 4.
