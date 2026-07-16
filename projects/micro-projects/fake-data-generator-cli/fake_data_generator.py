#!/usr/bin/env python3
"""Genere N faux utilisateurs (nom, email, age, ville) a partir de listes internes codees en dur.
Sortie au format JSON ou CSV."""
import argparse
import csv
import io
import json
import random
import sys

FIRST_NAMES = [
    "Jihed", "Amine", "Sarah", "Youssef", "Nour", "Karim", "Lina", "Mehdi",
    "Rania", "Bilel", "Emna", "Sami", "Ines", "Wassim", "Yasmine", "Adel",
    "Malek", "Salma", "Firas", "Dorra",
]

LAST_NAMES = [
    "Ben Ali", "Trabelsi", "Gharbi", "Jlassi", "Bouzid", "Khemiri", "Sassi",
    "Cherif", "Mansour", "Hamdi", "Ayari", "Karoui", "Bouaziz", "Ferjani",
    "Guesmi", "Naili", "Rekik", "Sfaxi", "Tlili", "Zribi",
]

CITIES = [
    "Tunis", "Sfax", "Sousse", "Kairouan", "Bizerte", "Gabes", "Ariana",
    "Gafsa", "Monastir", "Nabeul",
]

EMAIL_DOMAINS = ["example.com", "mail.test", "webmail.dev"]


def generate_user(user_id):
    first = random.choice(FIRST_NAMES)
    last = random.choice(LAST_NAMES)
    email_local = f"{first.lower()}.{last.lower().replace(' ', '')}{user_id}"
    email = f"{email_local}@{random.choice(EMAIL_DOMAINS)}"
    return {
        "id": user_id,
        "nom": f"{first} {last}",
        "email": email,
        "age": random.randint(18, 75),
        "ville": random.choice(CITIES),
    }


def to_json(users):
    return json.dumps(users, ensure_ascii=False, indent=2)


def to_csv(users):
    output = io.StringIO()
    fieldnames = ["id", "nom", "email", "age", "ville"]
    writer = csv.DictWriter(output, fieldnames=fieldnames)
    writer.writeheader()
    for user in users:
        writer.writerow(user)
    return output.getvalue()


def main():
    parser = argparse.ArgumentParser(description="Genere N faux utilisateurs (donnees fictives).")
    parser.add_argument("-n", "--count", type=int, default=10, help="Nombre d'utilisateurs (defaut: 10)")
    parser.add_argument("-f", "--format", choices=["json", "csv"], default="json", help="Format de sortie")
    parser.add_argument("-s", "--seed", type=int, default=None, help="Graine aleatoire (pour resultats reproductibles)")
    args = parser.parse_args()

    if args.count < 1:
        print("Erreur: --count doit etre >= 1", file=sys.stderr)
        sys.exit(1)

    if args.seed is not None:
        random.seed(args.seed)

    users = [generate_user(i) for i in range(1, args.count + 1)]

    if args.format == "json":
        print(to_json(users))
    else:
        print(to_csv(users), end="")


if __name__ == "__main__":
    main()
