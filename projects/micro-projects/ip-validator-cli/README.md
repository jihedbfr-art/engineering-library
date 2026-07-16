# ip-validator-cli

Valide une ou plusieurs adresses IPv4/IPv6 et détecte leur type (privée, publique,
loopback, multicast, link-local...) via le module stdlib `ipaddress`.

## Lancer

```bash
python ip_validator.py 192.168.1.1 8.8.8.8 ::1 not-an-ip
```

## Exemple d'usage

```bash
$ python ip_validator.py 192.168.1.1 8.8.8.8 127.0.0.1 ::1 999.1.1.1
192.168.1.1 -> VALIDE (IPv4, privée)
8.8.8.8 -> VALIDE (IPv4, publique)
127.0.0.1 -> VALIDE (IPv4, loopback)
::1 -> VALIDE (IPv6, loopback)
999.1.1.1 -> INVALIDE
```

Code de sortie non-nul si au moins une adresse est invalide.
