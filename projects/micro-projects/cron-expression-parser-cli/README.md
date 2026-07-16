# cron-expression-parser-cli

Parse une expression cron classique à 5 champs (minute heure jour mois jour-de-semaine)
et affiche les N prochaines dates d'exécution. Implémentation manuelle, sans lib externe.

## Lancer

```bash
python cron_parser.py "0 9 * * 1-5"
python cron_parser.py "*/15 9-17 * * *" -n 10
```

## Exemple d'usage

```bash
$ python cron_parser.py "0 9 * * 1-5" -n 3
2026-07-17 09:00 (Friday)
2026-07-20 09:00 (Monday)
2026-07-21 09:00 (Tuesday)
```

Supporte `*`, valeurs simples, listes (`1,2,3`), plages (`1-5`) et pas (`*/15`, `1-10/2`).
Jour de semaine au format cron standard : 0 ou 7 = dimanche, 1 = lundi, ..., 6 = samedi.
