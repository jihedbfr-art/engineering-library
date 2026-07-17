import argparse
import json
import os

STORE = "flags.json"


def load() -> dict:
    return json.load(open(STORE)) if os.path.exists(STORE) else {}


def save(flags: dict) -> None:
    json.dump(flags, open(STORE, "w"), indent=2)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Feature flags locaux (fichier JSON)")
    sub = parser.add_subparsers(dest="cmd", required=True)
    sub.add_parser("list")
    on = sub.add_parser("on"); on.add_argument("name")
    off = sub.add_parser("off"); off.add_argument("name")

    args = parser.parse_args()
    flags = load()
    if args.cmd == "list":
        for name, value in flags.items():
            print(f"{name}: {'ON' if value else 'OFF'}")
    elif args.cmd == "on":
        flags[args.name] = True
        save(flags)
    elif args.cmd == "off":
        flags[args.name] = False
        save(flags)
