#!/usr/bin/env python3
"""Convertit des longueurs, poids et temperatures via des sous-commandes argparse."""
import argparse
import sys

# --- Longueur : tout passe par le metre comme unite pivot ---
LENGTH_TO_METERS = {
    "m": 1.0,
    "km": 1000.0,
    "mile": 1609.344,
    "ft": 0.3048,
}

# --- Poids : tout passe par le kilogramme comme unite pivot ---
WEIGHT_TO_KG = {
    "kg": 1.0,
    "lb": 0.45359237,
}


def convert_length(value, from_unit, to_unit):
    meters = value * LENGTH_TO_METERS[from_unit]
    return meters / LENGTH_TO_METERS[to_unit]


def convert_weight(value, from_unit, to_unit):
    kg = value * WEIGHT_TO_KG[from_unit]
    return kg / WEIGHT_TO_KG[to_unit]


def convert_temperature(value, from_unit, to_unit):
    # normalise en Celsius d'abord
    if from_unit == "C":
        celsius = value
    elif from_unit == "F":
        celsius = (value - 32) * 5.0 / 9.0
    elif from_unit == "K":
        celsius = value - 273.15

    if to_unit == "C":
        return celsius
    elif to_unit == "F":
        return celsius * 9.0 / 5.0 + 32
    elif to_unit == "K":
        return celsius + 273.15


def main():
    parser = argparse.ArgumentParser(description="Convertisseur d'unites (longueur, poids, temperature).")
    sub = parser.add_subparsers(dest="command", required=True)

    p_len = sub.add_parser("length", help="Convertir une longueur (m, km, mile, ft)")
    p_len.add_argument("value", type=float)
    p_len.add_argument("from_unit", choices=LENGTH_TO_METERS.keys())
    p_len.add_argument("to_unit", choices=LENGTH_TO_METERS.keys())

    p_weight = sub.add_parser("weight", help="Convertir un poids (kg, lb)")
    p_weight.add_argument("value", type=float)
    p_weight.add_argument("from_unit", choices=WEIGHT_TO_KG.keys())
    p_weight.add_argument("to_unit", choices=WEIGHT_TO_KG.keys())

    p_temp = sub.add_parser("temperature", help="Convertir une temperature (C, F, K)")
    p_temp.add_argument("value", type=float)
    p_temp.add_argument("from_unit", choices=["C", "F", "K"])
    p_temp.add_argument("to_unit", choices=["C", "F", "K"])

    args = parser.parse_args()

    if args.command == "length":
        result = convert_length(args.value, args.from_unit, args.to_unit)
    elif args.command == "weight":
        result = convert_weight(args.value, args.from_unit, args.to_unit)
    elif args.command == "temperature":
        result = convert_temperature(args.value, args.from_unit, args.to_unit)
    else:
        print("Commande inconnue", file=sys.stderr)
        sys.exit(1)

    print(f"{args.value} {args.from_unit} = {result:.4f} {args.to_unit}")


if __name__ == "__main__":
    main()
