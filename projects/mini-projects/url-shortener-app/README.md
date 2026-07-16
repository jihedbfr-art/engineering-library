# url-shortener-app

## But
Raccourcir une URL (`POST /shorten {url}` -> `{code}`) et rediriger vers l'URL d'origine
(`GET /{code}` -> 302), avec une page d'accueil pour saisir une URL.

## Choix technique
Même approche que les autres mini-projets serveur : **`com.sun.net.httpserver.HttpServer`**, pas de
framework. Réutilise la logique d'encodage base62 d'un compteur incrémental (même principe que
`url-shortener-core` s'il existe dans la bibliothèque), mais ici exposée via un vrai serveur HTTP.

## Architecture
- `src/ShortenerServer.java` : serveur HTTP. Route `/shorten` (POST, JSON), route `/` (page d'accueil
  statique + redirection 302 sur tout code inconnu de la racine).
- `public/index.html` : formulaire de saisie d'URL, affiche le lien court obtenu.
- Stockage en mémoire (`Map<String,String>`) — redémarrage du serveur = tous les codes sont perdus.

## Lancer
Depuis le dossier `url-shortener-app/` :
```
javac -d out src/ShortenerServer.java
java -cp out ShortenerServer
```
Puis ouvrir http://localhost:8082
