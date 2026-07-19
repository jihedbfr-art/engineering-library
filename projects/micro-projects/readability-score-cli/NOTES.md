# readability-score-cli

Score Flesch Reading Ease + Flesch-Kincaid Grade Level, texte anglais.

## Stack

Python 3, stdlib uniquement (`re`, `argparse`).

## Lancer / tester

```bash
echo "Simple text here." | python readability.py -
```

## Fichiers clés

- `readability.py` — `count_syllables()` heuristique (groupes de voyelles + correction `e`
  muet final), `analyze()` applique les formules Flesch standard, `interpret()` mappe le
  score a une tranche de lisibilite lisible par un humain.

## Points d'attention

- Formules calibrees pour l'anglais (comptage de syllabes different en francais) — a ne
  pas utiliser tel quel sur un texte francais, le score serait trompeur.
