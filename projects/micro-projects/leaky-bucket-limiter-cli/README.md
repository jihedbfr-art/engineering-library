# leaky-bucket-limiter-cli

Rate limiting par seau percé (leaky bucket) : lisse le trafic a un debit de sortie constant,
contrairement au token bucket qui tolere des rafales. Le choix classique pour du shaping reseau.

## Lancer
```bash
javac LeakyBucket.java && java LeakyBucket
```
