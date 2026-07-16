# pastebin-clone

## But
Créer un paste de texte (`POST /paste`) et le récupérer via un ID court (`GET /paste/{id}`), avec une
page HTML pour créer/consulter un paste.

## Choix technique
Même approche que `notes-app-spring` : **`com.sun.net.httpserver.HttpServer`** (JDK standard), pas de
framework. Chaque paste est stocké dans un fichier texte `pastes/{id}.txt`. L'ID est un code aléatoire
de 6 caractères alphanumériques (regénéré en cas de collision).

## Architecture
- `src/PastebinServer.java` : serveur HTTP. Route `/paste` (POST création, GET/{id} lecture — réponses
  JSON), route `/` (page HTML statique depuis `public/`).
- `public/index.html` : formulaire de création + champ de consultation par ID.

## Lancer
Depuis le dossier `pastebin-clone/` :
```
javac -d out src/PastebinServer.java
java -cp out PastebinServer
```
Puis ouvrir http://localhost:8081
