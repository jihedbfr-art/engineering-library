# fake-data-generator-cli

Génère N faux utilisateurs (nom, email, âge, ville) en JSON ou CSV, sans lib Faker externe.

## Stack

Python 3, stdlib uniquement (`random`, `json`, `csv`, `argparse`).

## Lancer / tester

```bash
python fake_data_generator.py -n 10 -f json
python fake_data_generator.py -n 10 -f csv --seed 1
```

## Fichiers clés

- `fake_data_generator.py` — listes `FIRST_NAMES`/`LAST_NAMES`/`CITIES` codées en dur,
  `generate_user()` construit un utilisateur, `to_json()`/`to_csv()` formatent la sortie.

## Points d'attention

- `--seed` permet des résultats reproductibles (utile pour tests).
- Emails générés = `prenom.nom<id>@domaine` pour garantir l'unicité même avec les mêmes noms.
