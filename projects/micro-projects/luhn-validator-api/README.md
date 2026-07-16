# luhn-validator-api

Micro-API HTTP (sans framework, `com.sun.net.httpserver.HttpServer`) exposant
`GET /validate?number=xxx` qui applique l'algorithme de Luhn et renvoie
`{"number":"...","valid":true/false}`.

> **Important** : ceci est un **validateur de FORMAT uniquement** (checksum de Luhn), à but
> **éducatif**. Ce n'est **pas** un outil de vérification de cartes bancaires réelles — la
> validité de Luhn ne prouve ni l'existence ni la légitimité d'un numéro de carte.
> Le serveur ne stocke ni ne logge aucun numéro reçu.

## Lancer

```bash
cd src
javac LuhnAlgorithm.java Main.java
java Main
```

Le serveur démarre sur `http://localhost:8080`.

## Exemple

```bash
$ curl "http://localhost:8080/validate?number=79927398713"
{"number":"79927398713","valid":true}

$ curl "http://localhost:8080/validate?number=1234567890123456"
{"number":"1234567890123456","valid":false}
```
