# palindrome-checker-api

API HTTP Java pur (JDK `HttpServer`) exposant GET /check?text=xxx pour vérifier un palindrome.

- **Stack** : Java (JDK standard, `com.sun.net.httpserver`). Pas de Spring Boot, aucune dépendance externe.
- **Lancer** : `javac PalindromeApi.java && java PalindromeApi` (écoute sur port 8080).
- **Tester rapidement** : `curl "http://localhost:8080/check?text=kayak"` doit renvoyer `isPalindrome":true`.
- **Fichier clé** : `PalindromeApi.java` (fichier unique, classes internes `CheckHandler`).
- **Points d'attention** : comparaison ignore casse/espaces/ponctuation (garde uniquement lettres/chiffres). Réponse 400 si paramètre `text` absent, 405 si méthode != GET.
