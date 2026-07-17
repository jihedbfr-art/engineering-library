import argparse
import json
from datetime import date


def generate_invoice(client: str, items: list[dict], invoice_number: str) -> str:
    subtotal = sum(item["qty"] * item["price"] for item in items)
    tax = subtotal * 0.19
    total = subtotal + tax

    lines = [
        f"FACTURE #{invoice_number}",
        f"Date : {date.today().isoformat()}",
        f"Client : {client}",
        "-" * 50,
    ]
    for item in items:
        lines.append(f"{item['name']:<25} {item['qty']:>3} x {item['price']:>8.2f} = {item['qty'] * item['price']:>10.2f}")
    lines += [
        "-" * 50,
        f"{'Sous-total':<38}{subtotal:>10.2f}",
        f"{'TVA (19%)':<38}{tax:>10.2f}",
        f"{'TOTAL':<38}{total:>10.2f}",
    ]
    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(description="Genere une facture texte a partir d'un JSON d'articles")
    parser.add_argument("client")
    parser.add_argument("items_json", help='ex: \'[{"name":"Conseil","qty":5,"price":100}]\'')
    parser.add_argument("-n", "--number", default="2026-001")
    args = parser.parse_args()

    items = json.loads(args.items_json)
    print(generate_invoice(args.client, items, args.number))


if __name__ == "__main__":
    main()
