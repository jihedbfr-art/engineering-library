#!/usr/bin/env python3
"""Génère un QR code à partir d'un texte.

Utilise le package `qrcode` s'il est installé (pip install qrcode[pil] ou qrcode).
Sinon, fallback hors-ligne : un simple encadré ASCII-art autour du texte
(PAS un vrai QR code scannable, juste un repère visuel pour rester utilisable
sans connexion réseau / sans dépendance).
"""
import argparse
import sys
from typing import Optional

try:
    import qrcode  # type: ignore
    HAS_QRCODE = True
except ImportError:
    HAS_QRCODE = False


def generate_real_qr(text: str, output: Optional[str]) -> None:
    qr = qrcode.QRCode(border=2)
    qr.add_data(text)
    qr.make(fit=True)

    if output:
        img = qr.make_image(fill_color="black", back_color="white")
        img.save(output)
        print(f"QR code enregistré dans {output}")
    else:
        qr.print_ascii(invert=True)


def generate_ascii_placeholder(text: str) -> str:
    """Fallback hors-ligne : encadré ASCII autour du texte (PAS un vrai QR)."""
    lines = [text[i:i + 40] for i in range(0, len(text), 40)] or [""]
    width = max(len(line) for line in lines) + 4
    border = "+" + "-" * width + "+"
    body = [border]
    body.append("|" + " QR PLACEHOLDER (non scannable) ".center(width) + "|")
    body.append("+" + "-" * width + "+")
    for line in lines:
        body.append("| " + line.ljust(width - 2) + " |")
    body.append(border)
    return "\n".join(body)


def main() -> None:
    parser = argparse.ArgumentParser(description="Génère un QR code (ou un placeholder ASCII si `qrcode` absent).")
    parser.add_argument("text", help="Texte ou URL à encoder")
    parser.add_argument("-o", "--output", help="Fichier image de sortie (ex: qr.png), nécessite le package qrcode")
    args = parser.parse_args()

    if HAS_QRCODE:
        generate_real_qr(args.text, args.output)
    else:
        print("Le package 'qrcode' n'est pas installé. Installez-le avec :", file=sys.stderr)
        print("    pip install qrcode", file=sys.stderr)
        print("Affichage d'un placeholder ASCII (PAS un vrai QR code) en attendant :\n", file=sys.stderr)
        print(generate_ascii_placeholder(args.text))


if __name__ == "__main__":
    main()
