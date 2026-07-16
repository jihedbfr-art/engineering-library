# blog-engine-spring

## But
Moteur de blog qui lit des articles Markdown dans `posts/`, les liste et les rend en HTML.

## Choix technique (écart voulu)
Le nom du dossier vient de la consigne d'origine ("blog-engine-spring"), mais l'implémentation utilise
**`com.sun.net.httpserver.HttpServer`**, pas Spring (pas de dépendance Maven réseau garantie). Le rendu
Markdown -> HTML est fait maison (`MarkdownConverter.java`), un sous-ensemble volontairement simple :
titres `#`/`##`, **gras**, *italique*, paragraphes, listes à puces `- `. Pas de liens, images, code
blocks ni tableaux.

## Architecture
- `posts/*.md` : articles au format Markdown (le titre `# ...` de la première ligne sert de titre
  affiché dans la liste).
- `src/MarkdownConverter.java` : conversion ligne par ligne vers HTML.
- `src/BlogServer.java` : serveur HTTP, route `/` (liste des articles) et `/post/{slug}` (article rendu).

## Lancer
Depuis le dossier `blog-engine-spring/` (pour que `posts/` soit trouvé) :
```
javac -d out src/BlogServer.java src/MarkdownConverter.java
java -cp out BlogServer
```
Puis ouvrir http://localhost:8083
