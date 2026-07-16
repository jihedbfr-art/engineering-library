# calculator-rest-api

API HTTP Java pur (JDK `HttpServer`) exposant GET /calculate?op=add|sub|mul|div&a=&b=.

- **Stack** : Java (JDK standard, `com.sun.net.httpserver`). Pas de Spring Boot, aucune dépendance externe.
- **Lancer** : `javac CalculatorApi.java && java CalculatorApi` (écoute sur port 8081).
- **Tester rapidement** : `curl "http://localhost:8081/calculate?op=add&a=3&b=5"` doit renvoyer `"result":8`.
- **Fichier clé** : `CalculatorApi.java` (fichier unique, classe interne `CalculateHandler`).
- **Points d'attention** : division par zéro renvoie HTTP 400 + JSON d'erreur (pas d'exception non gérée). Port 8081 (différent de palindrome-checker-api sur 8080) pour pouvoir lancer les deux en parallèle.
