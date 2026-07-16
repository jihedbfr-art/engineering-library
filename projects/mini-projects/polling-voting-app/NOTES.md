# polling-voting-app

## Vue d'ensemble
Sondages (question + options), vote, résultats en %. Stockage en mémoire, non persistant (voir README).

## Stack
Java 17+ pur, `com.sun.net.httpserver.HttpServer`. Aucune dépendance externe.

## Commandes
```
javac -d out src/VotingServer.java
java -cp out VotingServer
```
Lancer depuis la racine du projet (`polling-voting-app/`) pour que `public/` soit trouvé.

## Fichiers clés
- `src/VotingServer.java` — serveur, routes `/polls` (GET/POST) et `/vote` (POST).
- `public/index.html` — création de sondage + affichage des résultats avec barres de progression.

## Note
Stockage en mémoire : redémarrer le serveur efface tous les sondages et votes.
