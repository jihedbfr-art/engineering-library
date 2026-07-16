# age-calculator-cli

Calcule l'âge exact (années, mois, jours) à partir d'une date de naissance, avec gestion
correcte des années bissextiles et des longueurs de mois variables.

## Lancer

```bash
python age_calculator.py 1990-06-15
python age_calculator.py 1990-06-15 --on 2026-07-16
```

## Exemple d'usage

```bash
$ python age_calculator.py 2000-02-29 --on 2026-03-01
26 an(s), 0 mois, 1 jour(s)
(9497 jours au total)
```

`--on` permet de calculer l'âge à une date de référence différente d'aujourd'hui.
