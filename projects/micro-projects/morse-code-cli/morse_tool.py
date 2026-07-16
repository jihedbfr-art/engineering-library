#!/usr/bin/env python3
"""Encode/décode du texte en code Morse."""
import argparse

MORSE_TABLE = {
    "A": ".-", "B": "-...", "C": "-.-.", "D": "-..", "E": ".", "F": "..-.",
    "G": "--.", "H": "....", "I": "..", "J": ".---", "K": "-.-", "L": ".-..",
    "M": "--", "N": "-.", "O": "---", "P": ".--.", "Q": "--.-", "R": ".-.",
    "S": "...", "T": "-", "U": "..-", "V": "...-", "W": ".--", "X": "-..-",
    "Y": "-.--", "Z": "--..",
    "0": "-----", "1": ".----", "2": "..---", "3": "...--", "4": "....-",
    "5": ".....", "6": "-....", "7": "--...", "8": "---..", "9": "----.",
    ".": ".-.-.-", ",": "--..--", "?": "..--..", "'": ".----.", "!": "-.-.--",
    "/": "-..-.", "(": "-.--.", ")": "-.--.-", "&": ".-...", ":": "---...",
    ";": "-.-.-.", "=": "-...-", "+": ".-.-.", "-": "-....-", "_": "..--.-",
    "\"": ".-..-.", "$": "...-..-", "@": ".--.-.",
}
REVERSE_TABLE = {v: k for k, v in MORSE_TABLE.items()}


def encode(text: str) -> str:
    words = text.upper().split(" ")
    encoded_words = []
    for word in words:
        letters = [MORSE_TABLE[c] for c in word if c in MORSE_TABLE]
        encoded_words.append(" ".join(letters))
    return " / ".join(encoded_words)


def decode(morse: str) -> str:
    words = morse.strip().split(" / ")
    decoded_words = []
    for word in words:
        letters = [REVERSE_TABLE.get(code, "") for code in word.split(" ") if code]
        decoded_words.append("".join(letters))
    return " ".join(decoded_words)


def main():
    parser = argparse.ArgumentParser(description="Encode/décode du texte en code Morse.")
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("-e", "--encode", metavar="TEXT", help="Texte à encoder en morse")
    group.add_argument("-d", "--decode", metavar="MORSE", help="Morse à décoder en texte")
    args = parser.parse_args()

    if args.encode is not None:
        print(encode(args.encode))
    else:
        print(decode(args.decode))


if __name__ == "__main__":
    main()
