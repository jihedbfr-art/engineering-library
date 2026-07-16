# bmi-calculator-api

API HTTP Java pur (JDK `HttpServer`) exposant GET /bmi?weight=70&height=1.75 pour calculer un IMC.

- **Stack** : Java (JDK standard, `com.sun.net.httpserver`). Pas de Spring Boot, aucune dépendance externe.
- **Lancer** : `javac BmiApi.java && java BmiApi` (écoute sur port 8080).
- **Tester rapidement** : `curl "http://localhost:8080/bmi?weight=70&height=1.75"` doit renvoyer `"bmi":22.86`.
- **Fichier clé** : `BmiApi.java` (fichier unique, classe interne `BmiHandler`).
- **Points d'attention** : weight en kg, height en mètres. Catégories : <18.5 insuffisance, <25 normale, <30 surpoids, sinon obésité. Réponse 400 si paramètres manquants/non numériques/négatifs, 405 si méthode != GET.
