import argparse

FONT = {
    "A": [" # ", "# #", "###", "# #", "# #"],
    "B": ["## ", "# #", "## ", "# #", "## "],
    "C": [" ##", "#  ", "#  ", "#  ", " ##"],
}


def render(text: str) -> str:
    letters = [FONT.get(c.upper(), ["   "] * 5) for c in text]
    return "\n".join(" ".join(letter[row] for letter in letters) for row in range(5))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Banniere ASCII minimale (A/B/C geres, le reste = espace)")
    parser.add_argument("text")
    args = parser.parse_args()
    print(render(args.text))
