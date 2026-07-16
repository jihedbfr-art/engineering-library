# url-shortener-app

## Vue d'ensemble
Raccourcisseur d'URL avec encodage base62, redirection 302, stockage en mémoire (non persistant).

## Stack
Java 17+ pur, `com.sun.net.httpserver.HttpServer`. Aucune dépendance externe.

## Commandes
```
javac -d out src/ShortenerServer.java
java -cp out ShortenerServer
```
Lancer depuis la racine du projet (`url-shortener-app/`) pour que `public/` soit trouvé.

## Fichiers clés
- `src/ShortenerServer.java` — serveur HTTP, route `/shorten` (POST) et `/{code}` (redirection 302).
- `public/index.html` — formulaire de saisie d'URL et affichage du lien court.

## Note
Stockage en mémoire : redémarrer le serveur efface tous les codes.
