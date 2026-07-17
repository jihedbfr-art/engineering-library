import argparse


def xor_bytes(data: bytes, key: bytes) -> bytes:
    return bytes(b ^ key[i % len(key)] for i, b in enumerate(data))


def main():
    parser = argparse.ArgumentParser(description="Chiffrement XOR simple (sa propre fonction inverse)")
    parser.add_argument("text")
    parser.add_argument("key")
    args = parser.parse_args()

    result = xor_bytes(args.text.encode(), args.key.encode())
    print(result.hex())


if __name__ == "__main__":
    main()
