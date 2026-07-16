# password-strength-checker-cli

Analyseur de force de mot de passe avec score sur 6 et suggestions.

- **Stack** : Python 3 stdlib (`re`). Aucune dépendance.
- **Lancer** : `python password_strength.py "MonMotDePasse123!"`
- **Tester rapidement** : `python password_strength.py "password123"` doit renvoyer un score faible (exit 1).
- **Fichier clé** : `password_strength.py` (script unique, `evaluate` / `label`).
- **Points d'attention** : score basé sur longueur + diversité de caractères, pénalisé si motif faible connu (`COMMON_PATTERNS`) ou répétition (regex `(.)\1{2,}`). Exit code 1 si score <= 2 (pratique en CI).
