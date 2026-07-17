def binary_to_decimal(b: str) -> int:
    return int(b, 2)


if __name__ == "__main__":
    b = input("Binaire : ")
    print(f"{b} = {binary_to_decimal(b)}")
