# rate-limiter-demo-api

Rate limiter fixed-window en memoire (5 requetes / 10s par IP), renvoie 429 + Retry-After
au-dela - la version pedagogique du principe utilise par les API gateways.

## Lancer

```bash
python rate_limiter.py
# puis, plusieurs fois de suite :
curl -i http://localhost:8080/
```
