import argparse
import base64
import hashlib
import hmac
import struct
import time


def generate_totp(secret: str, digits: int = 6, period: int = 30) -> str:
    key = base64.b32decode(secret.upper() + "=" * ((8 - len(secret) % 8) % 8))
    counter = int(time.time() // period)
    msg = struct.pack(">Q", counter)
    h = hmac.new(key, msg, hashlib.sha1).digest()
    offset = h[-1] & 0x0F
    code = (struct.unpack(">I", h[offset:offset + 4])[0] & 0x7FFFFFFF) % (10 ** digits)
    return str(code).zfill(digits)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generateur TOTP (RFC 6238)")
    parser.add_argument("secret", help="secret Base32 (ex: JBSWY3DPEHPK3PXP)")
    args = parser.parse_args()
    print(generate_totp(args.secret))
