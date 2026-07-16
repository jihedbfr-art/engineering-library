# fake-data-generator-cli

Génère N faux utilisateurs (nom, email, âge, ville) à partir de listes de noms/villes codées
en dur (aucune lib externe type Faker), en sortie JSON ou CSV au choix.

## Lancer

```bash
python fake_data_generator.py -n 5 -f json
python fake_data_generator.py -n 5 -f csv
```

## Exemple

```bash
$ python fake_data_generator.py -n 2 -f json --seed 42
[
  {
    "id": 1,
    "nom": "Sami Zribi",
    "email": "sami.zribi1@example.com",
    "age": 34,
    "ville": "Sfax"
  },
  ...
]
```
