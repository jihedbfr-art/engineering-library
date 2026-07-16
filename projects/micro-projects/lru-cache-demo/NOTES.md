# lru-cache-demo

Cache LRU maison (HashMap + liste doublement chaînée) avec get/put en O(1), démo console d'éviction.

## Stack

Java pur, aucune dépendance, aucun framework (JDK standard).

## Lancer / tester

```bash
cd src
javac LRUCache.java Main.java
java Main
```

## Fichiers clés

- `src/LRUCache.java` — cache générique `LRUCache<K,V>`, noeuds `Node<K,V>` internes,
  sentinelles `head`/`tail` pour éviter les cas particuliers en tête/queue.
- `src/Main.java` — démonstration console qui prouve l'éviction LRU sur un scénario capacité=3.

## Points d'attention

- Pas de `java.util.LinkedHashMap` : liste doublement chaînée codée à la main.
- `get()` et `put()` déplacent le noeud en tête (MRU) ; l'éviction retire `tail.prev` (LRU).
