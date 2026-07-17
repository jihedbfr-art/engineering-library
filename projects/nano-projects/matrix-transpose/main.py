def transpose(matrix: list[list[int]]) -> list[list[int]]:
    return [list(row) for row in zip(*matrix)]


if __name__ == "__main__":
    m = [[1, 2, 3], [4, 5, 6]]
    print("Original :", m)
    print("Transposee :", transpose(m))
