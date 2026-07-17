MORSE = {
    ".-": "A", "-...": "B", "-.-.": "C", "-..": "D", ".": "E", "..-.": "F",
    "--.": "G", "....": "H", "..": "I", ".---": "J", "-.-": "K", ".-..": "L",
    "--": "M", "-.": "N", "---": "O", ".--.": "P", "--.-": "Q", ".-.": "R",
    "...": "S", "-": "T", "..-": "U", "...-": "V", ".--": "W", "-..-": "X",
    "-.--": "Y", "--..": "Z",
}


def morse_to_text(code: str) -> str:
    return "".join(MORSE.get(sym, "?") for sym in code.split())


if __name__ == "__main__":
    code = input("Morse (espaces entre lettres) : ")
    print(morse_to_text(code))
