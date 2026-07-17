def is_armstrong(n: int) -> bool:
    digits = str(n)
    power = len(digits)
    return n == sum(int(d) ** power for d in digits)


if __name__ == "__main__":
    n = int(input("Nombre : "))
    verdict = "est" if is_armstrong(n) else "n'est pas"
    print(f"{n} {verdict} un nombre d'Armstrong")
