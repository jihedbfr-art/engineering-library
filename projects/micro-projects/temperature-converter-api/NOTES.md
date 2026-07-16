# temperature-converter-api

API HTTP Java pur (JDK `HttpServer`) exposant GET /convert?value=100&from=C&to=F.

- **Stack** : Java (JDK standard, `com.sun.net.httpserver`). Pas de Spring Boot, aucune dépendance externe.
- **Lancer** : `javac TemperatureApi.java && java TemperatureApi` (écoute sur port 8080).
- **Tester rapidement** : `curl "http://localhost:8080/convert?value=100&from=C&to=F"` doit renvoyer `"result":212.0`.
- **Fichier clé** : `TemperatureApi.java` (fichier unique, classe interne `ConvertHandler`).
- **Points d'attention** : unités acceptées C/F/K, insensibles à la casse. Conversion passe toujours par Celsius comme pivot. Réponse 400 si paramètres manquants/unité invalide, 405 si méthode != GET.
