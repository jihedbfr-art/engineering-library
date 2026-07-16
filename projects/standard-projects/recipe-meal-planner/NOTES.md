# recipe-meal-planner

Planificateur de repas sur 7 jours avec bibliothèque de recettes et liste de courses générée
automatiquement à partir du planning.

## Stack

Java 17 pur (`com.sun.net.httpserver.HttpServer`), aucune dépendance externe, aucun build tool
(compilation directe avec `javac`). Frontend HTML/CSS/JS vanilla. Persistance : fichiers JSON
locaux dans `data/`.

## Commandes

```bash
javac -d out src/*.java
java -cp out Server 8080
```

Le serveur sert l'API sous `/api/*` et les fichiers statiques de `public/` sur `/`.

## Fichiers clés

- `src/Server.java` — serveur HTTP, routes API, logique métier (recettes, planning, agrégation).
- `src/Json.java` — parseur/writer JSON maison (Map/List/String/Double/Boolean/null uniquement).
- `data/recipes.json`, `data/planning.json` — état persistant, créés automatiquement au premier
  lancement s'ils n'existent pas.
- `public/index.html`, `public/app.js`, `public/style.css` — UI à onglets (Planning / Recettes /
  Liste de courses).

## Règles spécifiques

- Toute écriture sur les fichiers JSON passe par le `ReentrantLock` global (`Server.lock`) —
  ne jamais lire/écrire `data/*.json` en dehors de ce verrou.
- `Json.java` ne gère que les types JSON de base ; ne pas y ajouter de dépendance externe (le but
  du lot est zéro-dépendance).
- La suppression d'une recette doit toujours nettoyer les créneaux de planning qui la référencent
  (voir `DELETE /api/recipes/{id}` dans `RecipesHandler`).
