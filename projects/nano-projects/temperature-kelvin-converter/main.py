def celsius_to_kelvin(c: float) -> float:
    return c + 273.15


def kelvin_to_celsius(k: float) -> float:
    return k - 273.15


if __name__ == "__main__":
    c = float(input("Celsius : "))
    print(f"{c} C = {celsius_to_kelvin(c):.2f} K")
