# random-quote-api

API HTTP minimaliste en Java pur (`com.sun.net.httpserver.HttpServer`, aucune dépendance,
pas de Spring Boot) qui renvoie une citation aléatoire parmi une liste de vingt citations
codées en dur (texte + auteur).

## Compiler et lancer

```bash
javac QuoteApi.java
java QuoteApi
```

Le serveur écoute sur `http://localhost:8080`.

## Exemple d'usage

```bash
$ curl "http://localhost:8080/quote"
{"quote":"Premature optimization is the root of all evil.","author":"Donald Knuth"}

$ curl "http://localhost:8080/quote"
{"quote":"La simplicité est la sophistication suprême.","author":"Léonard de Vinci"}
```
