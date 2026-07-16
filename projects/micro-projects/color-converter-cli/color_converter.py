#!/usr/bin/env python3
"""Convertit des couleurs entre hex (#RRGGBB), RGB et HSL."""
import argparse
import colorsys
import re

HEX_RE = re.compile(r"^#?([0-9a-fA-F]{6})$")


def parse_hex(value: str) -> tuple[int, int, int]:
    match = HEX_RE.match(value.strip())
    if not match:
        raise ValueError(f"hex invalide : {value!r} (attendu #RRGGBB)")
    h = match.group(1)
    return int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16)


def parse_rgb(value: str) -> tuple[int, int, int]:
    parts = [p.strip() for p in value.split(",")]
    if len(parts) != 3:
        raise ValueError(f"rgb invalide : {value!r} (attendu r,g,b)")
    r, g, b = (int(p) for p in parts)
    for c in (r, g, b):
        if not 0 <= c <= 255:
            raise ValueError(f"composante rgb hors limites (0-255) : {c}")
    return r, g, b


def parse_hsl(value: str) -> tuple[float, float, float]:
    parts = [p.strip().rstrip("%") for p in value.split(",")]
    if len(parts) != 3:
        raise ValueError(f"hsl invalide : {value!r} (attendu h,s%,l%)")
    h, s, l = (float(p) for p in parts)
    return h, s, l


def rgb_to_hex(r: int, g: int, b: int) -> str:
    return f"#{r:02X}{g:02X}{b:02X}"


def rgb_to_hsl(r: int, g: int, b: int) -> tuple[float, float, float]:
    h, l, s = colorsys.rgb_to_hls(r / 255, g / 255, b / 255)
    return h * 360, s * 100, l * 100


def hsl_to_rgb(h: float, s: float, l: float) -> tuple[int, int, int]:
    r, g, b = colorsys.hls_to_rgb(h / 360, l / 100, s / 100)
    return round(r * 255), round(g * 255), round(b * 255)


def print_all(r: int, g: int, b: int) -> None:
    h, s, l = rgb_to_hsl(r, g, b)
    print(f"HEX: {rgb_to_hex(r, g, b)}")
    print(f"RGB: {r}, {g}, {b}")
    print(f"HSL: {h:.0f}, {s:.0f}%, {l:.0f}%")


def main():
    parser = argparse.ArgumentParser(description="Convertit une couleur entre hex, RGB et HSL.")
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("--hex", help="Couleur au format hex, ex: #FF5733")
    group.add_argument("--rgb", help="Couleur au format r,g,b ex: 255,87,51")
    group.add_argument("--hsl", help="Couleur au format h,s%%,l%% ex: 11,100%%,60%%")
    args = parser.parse_args()

    try:
        if args.hex:
            r, g, b = parse_hex(args.hex)
        elif args.rgb:
            r, g, b = parse_rgb(args.rgb)
        else:
            h, s, l = parse_hsl(args.hsl)
            r, g, b = hsl_to_rgb(h, s, l)
    except ValueError as exc:
        parser.error(str(exc))
        return

    print_all(r, g, b)


if __name__ == "__main__":
    main()
