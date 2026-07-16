# blog-engine-spring

## Vue d'ensemble
Blog qui lit des fichiers Markdown dans `posts/` et les rend en HTML (conversion maison, pas Spring
— voir README).

## Stack
Java 17+ pur, `com.sun.net.httpserver.HttpServer`. Aucune dépendance externe.

## Commandes
```
javac -d out src/BlogServer.java src/MarkdownConverter.java
java -cp out BlogServer
```
Lancer depuis la racine du projet (`blog-engine-spring/`) pour que `posts/` soit trouvé.

## Fichiers clés
- `src/BlogServer.java` — serveur HTTP, routes `/` et `/post/{slug}`.
- `src/MarkdownConverter.java` — conversion Markdown -> HTML (sous-ensemble simple, voir en-tête du fichier).
- `posts/*.md` — articles. Ajouter un fichier `.md` suffit pour qu'il apparaisse dans la liste.
