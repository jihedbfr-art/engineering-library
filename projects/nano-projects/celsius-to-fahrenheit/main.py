def celsius_to_fahrenheit(c: float) -> float:
    return c * 9 / 5 + 32


def fahrenheit_to_celsius(f: float) -> float:
    return (f - 32) * 5 / 9


if __name__ == "__main__":
    c = float(input("Degrés Celsius : "))
    f = celsius_to_fahrenheit(c)
    print(f"{c}°C = {f}°F")
