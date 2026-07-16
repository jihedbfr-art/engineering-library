# port-scanner-cli

Scanner de ports TCP éducatif, limité à localhost, plage max 1024 ports. Usage strictement autorisé.

## Stack

Python 3, stdlib uniquement (`socket`, `argparse`).

## Lancer / tester

```bash
python port_scanner.py --start-port 1 --end-port 1024
```

## Fichiers clés

- `port_scanner.py` — `ALLOWED_HOSTS` (whitelist localhost only), `MAX_PORT_RANGE = 1024`,
  `scan_port()` fait un `connect_ex` avec timeout, `main()` valide host + plage avant de scanner.

## Points d'attention — GARDE-FOUS SÉCURITÉ (ne pas retirer)

- `ALLOWED_HOSTS` restreint la cible à `127.0.0.1` / `localhost` / `::1` — refus explicite sinon.
- `MAX_PORT_RANGE = 1024` empêche tout scan de masse.
- Avertissement affiché à chaque exécution (usage éducatif, machines autorisées uniquement).
- Ne jamais transformer ce script en scanner de plages IP ou en outil multi-hôtes.
