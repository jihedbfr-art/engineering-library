import math


def gcd_lcm(a: int, b: int) -> tuple[int, int]:
    g = math.gcd(a, b)
    l = abs(a * b) // g if g else 0
    return g, l


if __name__ == "__main__":
    a = int(input("Premier nombre : "))
    b = int(input("Deuxième nombre : "))
    g, l = gcd_lcm(a, b)
    print(f"PGCD = {g}, PPCM = {l}")
