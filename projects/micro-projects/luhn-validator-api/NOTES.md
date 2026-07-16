# luhn-validator-api

Micro-API HTTP GET /validate?number=xxx appliquant l'algorithme de Luhn (validation de format).

## Stack

Java pur avec `com.sun.net.httpserver.HttpServer` (pas de Spring Boot, cohérent avec le lot A).

## Lancer / tester

```bash
cd src
javac LuhnAlgorithm.java Main.java
java Main
curl "http://localhost:8080/validate?number=79927398713"
```

## Fichiers clés

- `src/LuhnAlgorithm.java` — `isValid(String)` : checksum de Luhn pur, sans I/O.
- `src/Main.java` — serveur HTTP sur le port 8080, `ValidateHandler` parse `?number=`, renvoie
  du JSON, gère les méthodes non-GET (405) et le paramètre manquant (400).

## Points d'attention — SÉCURITÉ (ne pas retirer)

- Validateur de **FORMAT uniquement** : ne prouve pas qu'une carte est réelle/active.
- Le serveur ne stocke ni ne logge jamais le paramètre `number` (aucun `println`/fichier).
- Usage éducatif — ne pas brancher sur un vrai flux de paiement.
