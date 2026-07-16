# url-shortener-core

Logique cœur d'un raccourcisseur d'URL en Java pur : `shorten(url) -> code` et
`resolve(code) -> url`, stockage en mémoire (HashMap), code court en base62 sur 6 caractères.
Aucun framework, aucun serveur HTTP — juste la logique.

## Lancer

```bash
cd src
javac UrlShortener.java Main.java
java Main
```

## Exemple

```
shorten("https://jihedapps.dev/knowledge/engineering-cookbook") -> 6fYs2x
resolve("6fYs2x") -> https://jihedapps.dev/knowledge/engineering-cookbook

Re-shorten de la meme URL (doit renvoyer le meme code, pas de doublon):
shorten(...) -> 6fYs2x (identique a 6fYs2x ? true)
```
