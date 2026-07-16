# library-management-mini

API REST + page HTML pour un CRUD de livres, stockage JSON fichier.

## Stack
Java pur, `com.sun.net.httpserver.HttpServer` (JDK standard) — **PAS de Spring Boot**,
**PAS de Maven/Gradle**. Aucune dépendance externe (JSON parsé/généré à la main).

## Commandes
```
javac -d out src/Main.java
java -cp out Main
```
Sert sur http://localhost:8080/. Génère `books.json` dans le répertoire courant.

## Fichiers clés
- `src/Main.java` — tout le serveur : modèle `Book`, `BooksHandler` (routes REST),
  `StaticHandler` (sert `public/index.html`), classe utilitaire `Json` (parse/sérialise
  des objets JSON plats sans dépendance externe), persistance `loadBooks`/`saveBooks`.
- `public/index.html` — UI CRUD minimaliste (fetch vers `/api/books`).
- `books.json` — généré au premier lancement (3 livres d'exemple), non versionné.

## Notes
- Le parseur JSON maison (`Json.parseObject`/`parseArray`) gère uniquement des objets
  plats (pas d'imbrication) — suffisant pour le modèle `Book`.
- Threading : `HttpServer.setExecutor(null)` = exécuteur par défaut (mono-thread) ;
  les accès à `books` sont protégés par `synchronized` par précaution.
