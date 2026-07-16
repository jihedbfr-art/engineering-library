#!/usr/bin/env python3
"""Convertisseur de devises avec taux de change STATIQUES codés en dur.

ATTENTION: les taux ci-dessous sont des valeurs d'EXEMPLE figées dans le code,
PAS des taux de change en temps réel. Ne pas utiliser pour de vraies transactions
financières. Pour des taux à jour, utiliser une API de change externe.
"""
import argparse
import sys

# Taux de change vers 1 EUR (référence), figés au moment de l'écriture du script.
RATES_TO_EUR = {
    "EUR": 1.0,
    "USD": 1.08,
    "GBP": 0.85,
    "JPY": 163.50,
    "TND": 3.38,
}


def convert(amount: float, from_currency: str, to_currency: str) -> float:
    from_currency = from_currency.upper()
    to_currency = to_currency.upper()

    if from_currency not in RATES_TO_EUR:
        raise ValueError(f"Devise inconnue: {from_currency}. Disponibles: {', '.join(RATES_TO_EUR)}")
    if to_currency not in RATES_TO_EUR:
        raise ValueError(f"Devise inconnue: {to_currency}. Disponibles: {', '.join(RATES_TO_EUR)}")

    amount_in_eur = amount / RATES_TO_EUR[from_currency]
    return amount_in_eur * RATES_TO_EUR[to_currency]


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Convertit un montant entre devises, taux STATIQUES d'exemple (pas temps réel)."
    )
    parser.add_argument("amount", type=float, help="Montant à convertir")
    parser.add_argument("from_currency", help="Devise source (EUR, USD, GBP, JPY, TND)")
    parser.add_argument("to_currency", help="Devise cible (EUR, USD, GBP, JPY, TND)")
    args = parser.parse_args()

    try:
        result = convert(args.amount, args.from_currency, args.to_currency)
    except ValueError as e:
        print(f"Erreur: {e}", file=sys.stderr)
        sys.exit(1)

    print(f"{args.amount:.2f} {args.from_currency.upper()} = {result:.2f} {args.to_currency.upper()}")
    print("(taux statiques d'exemple, pas temps réel)")


if __name__ == "__main__":
    main()
