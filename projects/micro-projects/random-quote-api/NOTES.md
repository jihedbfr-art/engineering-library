# random-quote-api

API HTTP Java pur (JDK `HttpServer`) exposant GET /quote pour tirer une citation au hasard.

- **Stack** : Java (JDK standard, `com.sun.net.httpserver`). Pas de Spring Boot, aucune dépendance externe.
- **Lancer** : `javac QuoteApi.java && java QuoteApi` (écoute sur port 8080).
- **Tester rapidement** : `curl "http://localhost:8080/quote"` doit renvoyer un objet `{"quote":...,"author":...}`.
- **Fichier clé** : `QuoteApi.java` (fichier unique, liste `QUOTES` codée en dur, ~20 entrées).
- **Points d'attention** : tirage via `ThreadLocalRandom`, pas de persistance ni base de données. Réponse 405 si méthode != GET.
