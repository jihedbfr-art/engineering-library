# notes-app-spring

## But
API REST CRUD de notes (GET/POST/DELETE `/notes`) + page HTML pour lister/ajouter des notes.

## Choix technique (écart voulu)
Le nom du dossier vient de la consigne d'origine ("notes-app-spring"), mais l'implémentation utilise
**`com.sun.net.httpserver.HttpServer`** (JDK standard), pas Spring Boot/Maven — pour éviter toute
dépendance réseau (téléchargement de dépendances Maven) non garantie dans cet environnement. Le
JSON est parsé/généré à la main (pas de Jackson), suffisant pour ce modèle simple à 2 champs.

## Architecture
- `src/NotesServer.java` : serveur HTTP unique. Route `/notes` (API JSON), route `/` (fichiers statiques
  depuis `public/`). Notes stockées en mémoire + persistées dans `notes.json` à chaque écriture.
- `public/index.html` : page unique avec formulaire d'ajout + liste des notes (fetch API).

## Lancer
Depuis le dossier `notes-app-spring/` (important : `public/` et `notes.json` sont relatifs au
répertoire courant) :
```
javac -d out src/NotesServer.java
java -cp out NotesServer
```
Puis ouvrir http://localhost:8080
