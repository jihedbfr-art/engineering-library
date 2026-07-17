# webhook-signature-verifier-cli

Verifie qu'un payload de webhook correspond bien a sa signature HMAC-SHA256 (le mecanisme
utilise par Stripe, GitHub, etc. pour prouver qu'un webhook n'a pas ete falsifie).

## Lancer

```bash
python verify_webhook.py payload.json abc123... monSecret
```
