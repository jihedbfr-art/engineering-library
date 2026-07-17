import re


def run_length_decode(s: str) -> str:
    return "".join(char * int(count) for count, char in re.findall(r"(\d+)(\D)", s))


if __name__ == "__main__":
    s = input("Chaine encodee (ex: 3a3b2c1d) : ")
    print(run_length_decode(s))
