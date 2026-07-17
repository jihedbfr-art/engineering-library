# iban-validator-cli

Valide un IBAN via l'algorithme modulo 97 (norme ISO 13616) : rearrangement, conversion
lettres->chiffres, verification du reste.

## Lancer

```bash
javac IbanValidator.java && java IbanValidator "FR14 2004 1010 0505 0001 3M02 606"
```
