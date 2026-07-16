#!/usr/bin/env python3
"""Calcule l'âge exact (années, mois, jours) à partir d'une date de naissance."""
import argparse
import calendar
from datetime import date


def parse_date(value: str) -> date:
    try:
        year, month, day = (int(p) for p in value.split("-"))
        return date(year, month, day)
    except ValueError as exc:
        raise ValueError(f"date invalide : {value!r} (attendu AAAA-MM-JJ)") from exc


def compute_age(birth: date, reference: date) -> tuple[int, int, int]:
    if birth > reference:
        raise ValueError("la date de naissance est dans le futur par rapport à la date de référence")

    years = reference.year - birth.year
    months = reference.month - birth.month
    days = reference.day - birth.day

    if days < 0:
        months -= 1
        prev_month = reference.month - 1 or 12
        prev_month_year = reference.year if reference.month > 1 else reference.year - 1
        days_in_prev_month = calendar.monthrange(prev_month_year, prev_month)[1]
        days += days_in_prev_month

    if months < 0:
        years -= 1
        months += 12

    return years, months, days


def main():
    parser = argparse.ArgumentParser(description="Calcule l'âge exact à partir d'une date de naissance.")
    parser.add_argument("birthdate", help="Date de naissance au format AAAA-MM-JJ")
    parser.add_argument("--on", help="Date de référence AAAA-MM-JJ (défaut: aujourd'hui)")
    args = parser.parse_args()

    try:
        birth = parse_date(args.birthdate)
        reference = parse_date(args.on) if args.on else date.today()
        years, months, days = compute_age(birth, reference)
    except ValueError as exc:
        parser.error(str(exc))
        return

    total_days = (reference - birth).days
    print(f"{years} an(s), {months} mois, {days} jour(s)")
    print(f"({total_days} jours au total)")


if __name__ == "__main__":
    main()
