def is_valid_iccid(iccid: str) -> bool:
    return iccid.isdigit() and 19 <= len(iccid) <= 20 and iccid.startswith("89")


if __name__ == "__main__":
    iccid = input("ICCID (carte SIM) : ")
    print("ICCID plausible" if is_valid_iccid(iccid) else "Format invalide")
