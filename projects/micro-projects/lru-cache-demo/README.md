# lru-cache-demo

Implémentation maison d'un cache LRU (Least Recently Used) en Java pur, avec `get`/`put` en O(1)
via une HashMap + une liste doublement chaînée explicite (pas de `LinkedHashMap` magique).
Une démo console prouve l'éviction du bloc le moins récemment utilisé quand la capacité est dépassée.

## Lancer

```bash
cd src
javac LRUCache.java Main.java
java Main
```

## Exemple

```
put(C, 3)  -> cache plein (capacite 3)
  [C, B, A]

get(A) -> 1  (A redevient le plus recemment utilise)
  [A, C, B]

put(D, 4) -> cache plein, doit evincer le LRU actuel (B)
  [D, A, C]

=== Resultat: SUCCES - eviction LRU correcte ===
```
