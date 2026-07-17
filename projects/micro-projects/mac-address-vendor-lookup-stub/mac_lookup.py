OUI_TABLE = {
    "00:1A:2B": "Cisco Systems",
    "3C:5A:B4": "Google Inc.",
    "F4:5C:89": "Apple Inc.",
    "00:0C:29": "VMware Inc.",
    "B8:27:EB": "Raspberry Pi Foundation",
}


def vendor_lookup(mac: str) -> str:
    prefix = mac.upper()[:8]
    return OUI_TABLE.get(prefix, "Constructeur inconnu (annuaire local limite)")


if __name__ == "__main__":
    mac = input("Adresse MAC (ex: 3C:5A:B4:11:22:33) : ")
    print(vendor_lookup(mac))
