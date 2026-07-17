import datetime


def days_until(year: int, month: int, day: int) -> int:
    target = datetime.date(year, month, day)
    return (target - datetime.date.today()).days


if __name__ == "__main__":
    y, m, d = (int(x) for x in input("Date cible (AAAA MM JJ) : ").split())
    n = days_until(y, m, d)
    print(f"{n} jour(s)" if n >= 0 else f"C'etait il y a {-n} jour(s)")
