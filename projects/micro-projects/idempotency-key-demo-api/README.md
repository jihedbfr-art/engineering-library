# idempotency-key-demo-api

API minimale demontrant l'idempotence via header `Idempotency-Key` : la premiere requete
avec une cle cree une ressource, les suivantes avec la meme cle renvoient le meme resultat
sans doublon - le pattern standard pour securiser les retries de paiement/commande.

## Lancer

```bash
javac IdempotencyServer.java && java IdempotencyServer
curl -H "Idempotency-Key: abc123" -X POST http://localhost:8080/orders
```
