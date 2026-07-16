#!/usr/bin/env python3
"""Parse une expression cron à 5 champs et affiche les N prochaines dates d'exécution.

Implémentation manuelle (pas de lib externe) : supporte *, valeurs simples, listes (1,2,3),
plages (1-5) et pas (*/5, 1-10/2).
"""
import argparse
from datetime import datetime, timedelta

FIELD_RANGES = {
    "minute": (0, 59),
    "hour": (0, 23),
    "day": (1, 31),
    "month": (1, 12),
    "weekday": (0, 6),  # 0 = dimanche, 1 = lundi ... 6 = samedi (7 accepté comme alias de dimanche)
}
FIELD_ORDER = ["minute", "hour", "day", "month", "weekday"]


def parse_field(expr: str, low: int, high: int) -> set[int]:
    values: set[int] = set()
    for part in expr.split(","):
        step = 1
        if "/" in part:
            part, step_str = part.split("/", 1)
            step = int(step_str)

        if part == "*":
            range_low, range_high = low, high
        elif "-" in part:
            a, b = part.split("-", 1)
            range_low, range_high = int(a), int(b)
        else:
            range_low = range_high = int(part)

        for v in range(range_low, range_high + 1, step):
            normalized = 0 if (v == 7 and high == 6) else v
            if low <= normalized <= high:
                values.add(normalized)

    return values


def parse_cron(expression: str) -> dict:
    fields = expression.split()
    if len(fields) != 5:
        raise ValueError(f"expression cron invalide, 5 champs attendus, {len(fields)} trouvés")

    parsed = {}
    for name, field in zip(FIELD_ORDER, fields):
        low, high = FIELD_RANGES[name]
        parsed[name] = parse_field(field, low, high)
    return parsed


def matches(dt: datetime, parsed: dict) -> bool:
    return (
        dt.minute in parsed["minute"]
        and dt.hour in parsed["hour"]
        and dt.day in parsed["day"]
        and dt.month in parsed["month"]
        and (dt.isoweekday() % 7) in parsed["weekday"]
    )


def next_runs(expression: str, count: int, start: datetime | None = None) -> list[datetime]:
    parsed = parse_cron(expression)
    current = (start or datetime.now()).replace(second=0, microsecond=0) + timedelta(minutes=1)

    results = []
    max_iterations = 60 * 24 * 366 * 5  # limite de sécurité (~5 ans en minutes)
    iterations = 0

    while len(results) < count and iterations < max_iterations:
        if matches(current, parsed):
            results.append(current)
        current += timedelta(minutes=1)
        iterations += 1

    return results


def main():
    parser = argparse.ArgumentParser(
        description="Parse une expression cron (5 champs) et affiche les prochaines exécutions."
    )
    parser.add_argument("expression", help="Expression cron, ex: '*/15 9-17 * * 1-5'")
    parser.add_argument("-n", "--number", type=int, default=5, help="Nombre de prochaines dates à afficher (défaut: 5)")
    args = parser.parse_args()

    try:
        runs = next_runs(args.expression, args.number)
    except ValueError as exc:
        parser.error(str(exc))
        return

    if not runs:
        print("Aucune exécution trouvée dans l'horizon de recherche.")
        return

    for dt in runs:
        print(dt.strftime("%Y-%m-%d %H:%M (%A)"))


if __name__ == "__main__":
    main()
