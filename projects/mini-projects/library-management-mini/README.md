# Library Management Mini

## But
CRUD de livres (titre, auteur, ISBN, disponible/emprunté) exposé via une API REST,
avec une petite page HTML qui la consomme. Stockage dans un fichier JSON local
(`books.json`), pas de base de données.

## Mini-architecture
- `src/Main.java` : serveur HTTP (`com.sun.net.httpserver.HttpServer`), routes REST
  sous `/api/books`, mini-parseur/sérialiseur JSON maison (classe `Json`), persistance
  fichier (`books.json`, créé/relu au démarrage).
- `public/index.html` : page statique servie à `/`, consomme l'API en `fetch`.

## Lancer
```
cd library-management-mini
javac -d out src/Main.java
java -cp out Main
```
Puis ouvrir http://localhost:8080/ (le fichier `books.json` est créé à côté du
répertoire de lancement, avec 3 livres d'exemple au premier démarrage).

## Endpoints
- `GET /api/books` — liste
- `GET /api/books/{id}` — détail
- `POST /api/books` — création (`{title, author, isbn}`)
- `PUT /api/books/{id}` — mise à jour partielle (ex: `{available:false}`)
- `DELETE /api/books/{id}` — suppression

## Choix technique
`com.sun.net.httpserver.HttpServer` (inclus dans le JDK) au lieu de Spring Boot,
comme imposé pour ce mini-projet — zéro dépendance, zéro Maven/Gradle, juste `javac`.
Le JSON est parsé/généré à la main (classe interne `Json`) car aucune librairie
externe n'est autorisée ; le parseur reste volontairement simple (objets plats,
pas de JSON imbriqué) car les besoins du modèle `Book` ne demandent rien de plus.
