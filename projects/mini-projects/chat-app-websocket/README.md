# chat-app-websocket

## But
Chat texte partagé entre plusieurs onglets/utilisateurs.

## Choix technique (écart voulu, important)
Le nom du dossier vient de la consigne d'origine ("chat-app-websocket"), mais ce n'est **pas un vrai
WebSocket**. L'API standard du JDK (`com.sun.net.httpserver`) ne supporte pas nativement le protocole
WebSocket sans dépendance externe (ex : Java-WebSocket, Jetty). L'implémentation utilise donc du
**HTTP long-polling** :
- `POST /send {user, message}` ajoute un message à la liste en mémoire.
- `GET /messages?since=<timestamp>` renvoie les messages plus récents que `since`.
- La page HTML interroge `/messages` toutes les **2 secondes** (`setInterval`) et met à jour l'écran.

C'est un compromis assumé pour un chat "presque temps réel" sans dépendance. Pour upgrader vers un
vrai WebSocket, il faudrait soit une dépendance (ex : `org.java-websocket:Java-WebSocket`, ou passer
sur Jetty/Spring qui exposent une API WebSocket), soit implémenter le handshake RFC 6455 à la main
(upgrade HTTP -> TCP brut, framing binaire) — nettement hors du budget de ce mini-projet.

## Architecture
- `src/ChatServer.java` : serveur HTTP, routes `/send`, `/messages`, `/` (statique). Messages stockés
  en mémoire (`CopyOnWriteArrayList`) — redémarrage = historique perdu.
- `public/index.html` : UI de chat, poll toutes les 2s.

## Lancer
Depuis le dossier `chat-app-websocket/` :
```
javac -d out src/ChatServer.java
java -cp out ChatServer
```
Puis ouvrir http://localhost:8084 dans plusieurs onglets pour voir le chat partagé.
