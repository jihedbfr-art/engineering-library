# notes-app-spring

## Vue d'ensemble
API CRUD de notes + page HTML statique, servies par un seul serveur Java (pas de Spring — voir README).

## Stack
Java 17+ pur, `com.sun.net.httpserver.HttpServer`. Aucune dépendance externe, JSON parsé à la main.

## Commandes
```
javac -d out src/NotesServer.java
java -cp out NotesServer
```
Lancer depuis la racine du projet (`notes-app-spring/`) pour que `public/` et `notes.json` soient trouvés.

## Fichiers clés
- `src/NotesServer.java` — serveur HTTP, routes `/notes` (API) et `/` (statique), persistance JSON.
- `public/index.html` — UI de gestion des notes.
- `notes.json` — généré au runtime, stockage des notes (ignorer/supprimer pour reset).
