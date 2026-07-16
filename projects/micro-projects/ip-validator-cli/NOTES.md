# ip-validator-cli

Valide des adresses IPv4/IPv6 et détecte leur type (privée/publique/loopback/...).

- **Stack** : Python 3 stdlib (`ipaddress`, `argparse`). Aucune dépendance.
- **Lancer** : `python ip_validator.py 192.168.1.1 8.8.8.8 ::1`
- **Tester rapidement** : `python ip_validator.py 127.0.0.1` doit afficher `loopback`.
- **Fichier clé** : `ip_validator.py` (fonction `classify` s'appuie sur `ipaddress.ip_address`).
- **Points d'attention** : accepte plusieurs adresses en une commande ; exit code 1 si au moins une est invalide.
