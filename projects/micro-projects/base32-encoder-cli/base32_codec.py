import argparse
import base64


def main():
    parser = argparse.ArgumentParser(description="Encode/decode en Base32 (RFC 4648)")
    parser.add_argument("mode", choices=["encode", "decode"])
    parser.add_argument("text")
    args = parser.parse_args()

    if args.mode == "encode":
        print(base64.b32encode(args.text.encode()).decode())
    else:
        print(base64.b32decode(args.text).decode())


if __name__ == "__main__":
    main()
