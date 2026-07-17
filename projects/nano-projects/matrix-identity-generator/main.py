def identity(n: int) -> list[list[int]]:
    return [[1 if i == j else 0 for j in range(n)] for i in range(n)]


if __name__ == "__main__":
    n = int(input("Taille de la matrice : "))
    for row in identity(n):
        print(row)
