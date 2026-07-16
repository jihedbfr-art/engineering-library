def factorial(n: int) -> int:
    if n < 0:
        raise ValueError("n doit être positif")
    result = 1
    for i in range(2, n + 1):
        result *= i
    return result


if __name__ == "__main__":
    n = int(input("Nombre : "))
    print(f"{n}! = {factorial(n)}")
