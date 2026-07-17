import argparse


def humanize(expr: str) -> str:
    minute, hour, dom, month, dow = expr.split()
    if minute != "*" and hour != "*" and dom == "*" and month == "*" and dow == "*":
        return f"Tous les jours a {hour.zfill(2)}:{minute.zfill(2)}"
    if minute == "0" and hour == "*" and dom == "*" and month == "*" and dow == "*":
        return "Toutes les heures, a l'heure pile"
    if dow != "*" and dom == "*":
        return f"Chaque semaine, jour(s) {dow}, a {hour.zfill(2)}:{minute.zfill(2)}"
    return f"Expression brute: minute={minute} heure={hour} jour-du-mois={dom} mois={month} jour-de-semaine={dow}"


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Traduit une expression cron en francais")
    parser.add_argument("expr", help="ex: '0 9 * * *'")
    args = parser.parse_args()
    print(humanize(args.expr))
