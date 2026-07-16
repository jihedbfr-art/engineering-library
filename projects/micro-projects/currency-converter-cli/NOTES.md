# currency-converter-cli

Convertisseur de devises avec taux de change STATIQUES codés en dur (EUR/USD/GBP/JPY/TND).

- **Stack** : Python 3 stdlib (`argparse`). Aucune dépendance.
- **Lancer** : `python currency_converter.py 100 USD EUR`
- **Tester rapidement** : `python currency_converter.py 1 EUR EUR` doit donner `1.00 EUR = 1.00 EUR`.
- **Fichier clé** : `currency_converter.py` (dict `RATES_TO_EUR`, fonction `convert`).
- **Points d'attention** : les taux sont des valeurs d'EXEMPLE figées dans le code, pas temps réel — clairement documenté dans le script et le README, jamais utiliser pour de vraies transactions.
