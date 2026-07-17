import argparse
import configparser
import json


def main():
    parser = argparse.ArgumentParser(description="Parse un fichier .ini et l'affiche en JSON")
    parser.add_argument("file")
    args = parser.parse_args()

    config = configparser.ConfigParser()
    config.read(args.file)
    data = {section: dict(config.items(section)) for section in config.sections()}
    print(json.dumps(data, indent=2))


if __name__ == "__main__":
    main()
