# pastebin-clone

## Vue d'ensemble
Clone minimal de pastebin : création de paste texte avec ID court, consultation par ID, page HTML.

## Stack
Java 17+ pur, `com.sun.net.httpserver.HttpServer`. Aucune dépendance externe.

## Commandes
```
javac -d out src/PastebinServer.java
java -cp out PastebinServer
```
Lancer depuis la racine du projet (`pastebin-clone/`) pour que `public/` et `pastes/` soient trouvés.

## Fichiers clés
- `src/PastebinServer.java` — serveur HTTP, routes `/paste` (POST/GET) et `/` (statique).
- `public/index.html` — UI de création et consultation de paste.
- `pastes/` — généré au runtime, un fichier `.txt` par paste.
