# chat-app-websocket

## Vue d'ensemble
Chat texte via HTTP long-polling (PAS un vrai WebSocket, voir README — le JDK standard ne le
supporte pas nativement). Poll toutes les 2s.

## Stack
Java 17+ pur, `com.sun.net.httpserver.HttpServer`. Aucune dépendance externe.

## Commandes
```
javac -d out src/ChatServer.java
java -cp out ChatServer
```
Lancer depuis la racine du projet (`chat-app-websocket/`) pour que `public/` soit trouvé.

## Fichiers clés
- `src/ChatServer.java` — serveur, routes `/send` (POST) et `/messages?since=` (GET, polling).
- `public/index.html` — UI de chat avec `setInterval` de 2s pour le polling.

## Note
Stockage en mémoire : redémarrer le serveur efface l'historique du chat.
