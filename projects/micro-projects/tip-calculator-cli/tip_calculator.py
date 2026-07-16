#!/usr/bin/env python3
"""Calcule le pourboire, le total et la part par personne."""
import argparse
import sys


def compute_tip(amount: float, percent: float, people: int) -> tuple[float, float, float]:
    tip = amount * percent / 100
    total = amount + tip
    per_person = total / people
    return tip, total, per_person


def main() -> None:
    parser = argparse.ArgumentParser(description="Calcule pourboire, total et répartition entre convives.")
    parser.add_argument("amount", type=float, help="Montant de l'addition")
    parser.add_argument("-p", "--percent", type=float, default=15.0, help="Pourcentage de pourboire (défaut: 15)")
    parser.add_argument("-n", "--people", type=int, default=1, help="Nombre de personnes pour le partage (défaut: 1)")
    args = parser.parse_args()

    if args.amount < 0:
        print("Erreur: le montant doit être positif.", file=sys.stderr)
        sys.exit(1)
    if args.people < 1:
        print("Erreur: le nombre de personnes doit être au moins 1.", file=sys.stderr)
        sys.exit(1)
    if args.percent < 0:
        print("Erreur: le pourcentage doit être positif.", file=sys.stderr)
        sys.exit(1)

    tip, total, per_person = compute_tip(args.amount, args.percent, args.people)

    print(f"Addition:        {args.amount:.2f}")
    print(f"Pourboire ({args.percent:.1f}%): {tip:.2f}")
    print(f"Total:           {total:.2f}")
    if args.people > 1:
        print(f"Par personne ({args.people}): {per_person:.2f}")


if __name__ == "__main__":
    main()
