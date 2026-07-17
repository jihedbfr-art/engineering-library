import datetime


def day_of_week(year: int, month: int, day: int) -> str:
    jours = ["Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"]
    return jours[datetime.date(year, month, day).weekday()]


if __name__ == "__main__":
    y, m, d = (int(x) for x in input("Date (AAAA MM JJ) : ").split())
    print(day_of_week(y, m, d))
