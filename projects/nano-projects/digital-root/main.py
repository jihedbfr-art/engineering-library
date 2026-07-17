def digital_root(n: int) -> int:
    while n >= 10:
        n = sum(int(d) for d in str(n))
    return n


if __name__ == "__main__":
    n = int(input("Nombre : "))
    print(f"Racine numérique de {n} = {digital_root(n)}")
