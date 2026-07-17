# health-check-aggregator-api

Endpoint `/health` qui agrege l'etat de plusieurs dependances (DB, cache, MQ) en un seul statut
UP/DOWN - le pattern derriere Spring Boot Actuator `/actuator/health`.

## Lancer
```bash
python health_check.py
curl http://localhost:8000/health
```
