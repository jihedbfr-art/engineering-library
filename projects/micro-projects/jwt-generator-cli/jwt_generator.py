import argparse
import base64
import hashlib
import hmac
import json
import time


def b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode()


def generate_jwt(payload: dict, secret: str) -> str:
    header = {"alg": "HS256", "typ": "JWT"}
    segments = [b64url(json.dumps(header).encode()), b64url(json.dumps(payload).encode())]
    signing_input = ".".join(segments).encode()
    signature = hmac.new(secret.encode(), signing_input, hashlib.sha256).digest()
    segments.append(b64url(signature))
    return ".".join(segments)


def main():
    parser = argparse.ArgumentParser(description="Genere un JWT HS256 (complement de jwt-decoder-cli)")
    parser.add_argument("--sub", default="user123")
    parser.add_argument("--secret", default="dev-secret")
    parser.add_argument("--exp-seconds", type=int, default=3600)
    args = parser.parse_args()

    payload = {"sub": args.sub, "iat": int(time.time()), "exp": int(time.time()) + args.exp_seconds}
    print(generate_jwt(payload, args.secret))


if __name__ == "__main__":
    main()
