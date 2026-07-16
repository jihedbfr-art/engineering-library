import sys

sys.stdout.reconfigure(encoding="utf-8")


def temperature_emoji(celsius: float) -> str:
    if celsius <= 0:
        return "❄️"
    if celsius < 15:
        return "🧥"
    if celsius < 25:
        return "🌤️"
    if celsius < 35:
        return "☀️"
    return "🔥"


if __name__ == "__main__":
    c = float(input("Température (°C) : "))
    print(f"{c}°C {temperature_emoji(c)}")
