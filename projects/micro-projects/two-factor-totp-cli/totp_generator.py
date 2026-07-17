import base64
import hashlib
import hmac
import struct
import time


def generate_totp(secret_b32: str, digits: int = 6, period: int = 30) -> str:
    key = base64.b32decode(secret_b32.upper())
    counter = int(time.time()) // period
    msg = struct.pack(">Q", counter)
    h = hmac.new(key, msg, hashlib.sha1).digest()
    offset = h[-1] & 0x0F
    code = (struct.unpack(">I", h[offset:offset + 4])[0] & 0x7FFFFFFF) % (10 ** digits)
    return str(code).zfill(digits)


if __name__ == "__main__":
    secret = "JBSWY3DPEHPK3PXP"
    print(f"Code TOTP actuel : {generate_totp(secret)} (valide ~30s)")
