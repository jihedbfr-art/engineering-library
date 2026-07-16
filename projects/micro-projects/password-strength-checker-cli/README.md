# password-strength-checker-cli

Évalue la force d'un mot de passe sur un score de 0 à 6 (longueur, présence de
minuscules/majuscules/chiffres/symboles, motifs faibles courants et répétitions), et
affiche des suggestions concrètes d'amélioration.

## Lancer

```bash
python password_strength.py "MonMotDePasse123!"
```

## Exemple d'usage

```bash
$ python password_strength.py "password123"
Score: 1/6 (Très faible)
Suggestions:
  - Ajouter des lettres majuscules.
  - Ajouter des symboles (!@#$...).
  - Éviter les motifs courants (123456, password, azerty...).

$ python password_strength.py "Xk9#mQ2$vL7pR"
Score: 6/6 (Très fort)
Aucune suggestion, ce mot de passe est robuste.
```
