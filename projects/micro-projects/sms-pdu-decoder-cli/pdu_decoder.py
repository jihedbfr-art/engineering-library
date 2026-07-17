import argparse


def decode_smsc(pdu: str) -> tuple[str, str]:
    """Decode juste l'en-tete SMSC (Service Center Address) d'un PDU SMS - format simplifie."""
    smsc_len_bytes = int(pdu[0:2], 16)
    smsc_len_hex_chars = smsc_len_bytes * 2
    smsc_field = pdu[2:2 + smsc_len_hex_chars]
    rest = pdu[2 + smsc_len_hex_chars:]
    return smsc_field, rest


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Decode l'en-tete SMSC d'un PDU SMS (format simplifie)")
    parser.add_argument("pdu", help="chaine hexadecimale du PDU")
    args = parser.parse_args()
    smsc, rest = decode_smsc(args.pdu)
    print(f"SMSC (brut hex): {smsc or '(absent)'}")
    print(f"Reste du PDU   : {rest}")
