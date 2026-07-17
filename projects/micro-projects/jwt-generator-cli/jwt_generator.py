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
    header_b64 = b64url(json.dumps(header, separators=(",", ":")).encode())
    payload_b64 = b64url(json.dumps(payload, separators=(",", ":")).encode())
    signing_input = f"{header_b64}.{payload_b64}".encode()
    signature = hmac.new(secret.encode(), signing_input, hashlib.sha256).digest()
    return f"{header_b64}.{payload_b64}.{b64url(signature)}"


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Genere un JWT signe HS256 (complement de jwt-decoder-cli)")
    parser.add_argument("subject")
    parser.add_argument("secret")
    parser.add_argument("--expires-in", type=int, default=3600)
    args = parser.parse_args()
    payload = {"sub": args.subject, "iat": int(time.time()), "exp": int(time.time()) + args.expires_in}
    print(generate_jwt(payload, args.secret))
