# palindrome-checker-api

API HTTP minimaliste en Java pur (`com.sun.net.httpserver.HttpServer`, aucune dépendance,
pas de Spring Boot) qui vérifie si un texte est un palindrome, en ignorant casse,
espaces et ponctuation.

## Compiler et lancer

```bash
javac PalindromeApi.java
java PalindromeApi
```

Le serveur écoute sur `http://localhost:8080`.

## Exemple d'usage

```bash
$ curl "http://localhost:8080/check?text=Engage%20le%20jeu%20que%20je%20le%20gagne"
{"input":"Engage le jeu que je le gagne","isPalindrome":true}

$ curl "http://localhost:8080/check?text=bonjour"
{"input":"bonjour","isPalindrome":false}

$ curl "http://localhost:8080/check"
{"error":"Paramètre 'text' manquant"}
```
