# api-mock-server-cli

Sert des reponses JSON pre-enregistrees depuis un dossier : `GET /users` cherche `users.json`.
Utile pour developper un frontend sans backend pret, ou stabiliser des tests d'integration.

## Lancer

```bash
mkdir mocks && echo '{"id":1,"name":"Alice"}' > mocks/users.json
python mock_server.py --dir mocks
curl http://localhost:8080/users
```
