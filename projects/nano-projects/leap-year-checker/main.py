def is_leap_year(year: int) -> bool:
    return year % 4 == 0 and (year % 100 != 0 or year % 400 == 0)


if __name__ == "__main__":
    year = int(input("Année : "))
    print(f"{year} est {'bissextile' if is_leap_year(year) else 'non bissextile'}")
