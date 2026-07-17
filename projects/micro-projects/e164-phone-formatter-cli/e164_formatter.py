import argparse
import re

COUNTRY_CODES = {"TN": "216", "FR": "33", "US": "1", "GB": "44"}


def to_e164(local_number: str, country: str) -> str:
    digits = re.sub(r"\D", "", local_number).lstrip("0")
    code = COUNTRY_CODES[country]
    return f"+{code}{digits}"


def main():
    parser = argparse.ArgumentParser(description="Formate un numero local en E.164 (norme telecom internationale)")
    parser.add_argument("number")
    parser.add_argument("-c", "--country", default="TN", choices=COUNTRY_CODES.keys())
    args = parser.parse_args()
    print(to_e164(args.number, args.country))


if __name__ == "__main__":
    main()
