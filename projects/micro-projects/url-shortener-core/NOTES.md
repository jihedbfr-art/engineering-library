# url-shortener-core

Logique cœur d'un raccourcisseur d'URL : shorten(url)->code, resolve(code)->url, en mémoire.

## Stack

Java pur, aucune dépendance, aucun framework, aucun serveur HTTP (juste la logique métier).

## Lancer / tester

```bash
cd src
javac UrlShortener.java Main.java
java Main
```

## Fichiers clés

- `src/UrlShortener.java` — deux `HashMap` (code->url et url->code, évite les doublons),
  `generateUniqueCode()` génère un code base62 de 6 caractères avec retry si collision.
- `src/Main.java` — démonstration console : shorten, resolve, doublon, code inconnu.

## Points d'attention

- `urlToCode` garantit qu'une même URL raccourcie deux fois renvoie toujours le même code.
- Constructeur `UrlShortener(long seed)` disponible pour des démos/tests reproductibles.
