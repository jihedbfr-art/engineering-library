# currency-converter-cli

Convertit un montant entre EUR, USD, GBP, JPY, TND à l'aide de taux de change
**statiques codés en dur** dans le script. Ce sont des taux d'exemple figés au
moment de l'écriture, PAS des taux temps réel — ne pas utiliser pour de vraies décisions financières.

## Lancer

```bash
python currency_converter.py 100 USD EUR
```

## Exemple d'usage

```bash
$ python currency_converter.py 100 USD EUR
100.00 USD = 92.59 EUR
(taux statiques d'exemple, pas temps réel)

$ python currency_converter.py 50 EUR TND
50.00 EUR = 169.00 TND
(taux statiques d'exemple, pas temps réel)
```

Devises disponibles : EUR, USD, GBP, JPY, TND.
