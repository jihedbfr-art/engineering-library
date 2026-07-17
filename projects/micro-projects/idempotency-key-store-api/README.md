# idempotency-key-store-api

Store d'idempotence minimal (computeIfAbsent) : un client qui retente un POST paiement avec la meme
`Idempotency-Key` ne declenche l'action qu'une seule fois.

## Lancer
```bash
javac IdempotencyStore.java && java IdempotencyStore
```
