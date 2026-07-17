import argparse
import hashlib
import hmac


def verify(payload: bytes, signature: str, secret: str) -> bool:
    expected = hmac.new(secret.encode(), payload, hashlib.sha256).hexdigest()
    return hmac.compare_digest(expected, signature)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Verifie une signature HMAC de webhook")
    parser.add_argument("payload_file")
    parser.add_argument("signature", help="hexdigest attendu")
    parser.add_argument("secret")
    args = parser.parse_args()
    with open(args.payload_file, "rb") as f:
        payload = f.read()
    ok = verify(payload, args.signature, args.secret)
    print("Signature valide" if ok else "Signature invalide - payload possiblement altere")
