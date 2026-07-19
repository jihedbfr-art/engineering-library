# Redis

> Pas une base de données au sens classique — un cache/store en mémoire, rapide parce qu'il fait peu de choses et les fait bien.

## 🏗️ Architecture

Single-threaded pour l'exécution des commandes (un seul thread traite les opérations sur une struct de données donnée à un instant T — évite les problèmes de concurrence interne, pas besoin de locks). Ça surprend la première fois : pas de multi-threading magique, la performance vient du fait que tout est en RAM et que les structures de données sont pensées pour être rapides à manipuler (skip lists pour les sorted sets, hash tables pour les hash). Le vrai goulot d'étranglement en pratique n'est presque jamais le CPU mais la bande passante réseau ou la taille des valeurs stockées.

## 🔑 Index

Pas d'index au sens SQL — les structures de données *sont* l'index. String, Hash, List, Set, Sorted Set, Stream. Le choix de structure est la vraie décision de perf : un `HGETALL` sur un hash de 50 champs est en O(N), un `SMEMBERS` sur un set énorme peut bloquer le thread principal le temps de l'opération — c'est ce genre de commande "lente sur une grosse collection" qu'il faut traquer, pas un index manquant.

## 🔒 Transactions, locks, isolation

`MULTI`/`EXEC` donne une transaction "best effort" : les commandes s'exécutent atomiquement les unes après les autres, mais **sans rollback** — si une commande échoue au milieu, les précédentes restent appliquées. Ce n'est pas ACID au sens base relationnelle. Pour un vrai besoin d'atomicité conditionnelle (check-then-set), `WATCH` + `MULTI`/`EXEC` fait de l'optimistic locking, ou plus simple : un script Lua exécuté atomiquement côté serveur (`EVAL`).

## 🌐 Réplication & partition

Réplication asynchrone primaire → replicas (donc une fenêtre de perte possible en cas de crash juste après une écriture, avant la propagation). Redis Cluster partitionne les données sur 16384 hash slots répartis entre nœuds — la clé qui détermine le slot peut être forcée avec `{tag}` dans le nom de clé, utile pour garantir que des clés liées finissent sur le même nœud (multi-key operations).

## 💾 Backup / Restore

RDB (snapshot périodique complet) pour une sauvegarde compacte et rapide à restaurer ; AOF (Append Only File, log de chaque écriture) pour une durabilité plus fine au prix d'un fichier plus lourd. En pratique : RDB seul si Redis est utilisé en cache pur (perte tolérable, la source de vérité est ailleurs) ; RDB + AOF si Redis porte des données qu'on ne veut pas perdre (queue, session store critique).

## 📊 Monitoring

`INFO memory` (fragmentation, `used_memory` vs `used_memory_rss`), taux d'éviction (`evicted_keys` — si ça monte, la mémoire allouée est sous-dimensionnée par rapport au volume de clés), latence des commandes (`redis-cli --latency` ou `SLOWLOG`), et le ratio hit/miss (`keyspace_hits` / `keyspace_misses`) qui dit si le cache sert vraiment à quelque chose ou si tout part en base derrière.

## Note perso

La partie qui surprend le plus les gens qui découvrent Redis en venant du monde SQL : il n'y a pas de "requête lente" à optimiser au sens EXPLAIN ANALYZE, mais des commandes intrinsèquement O(N) exécutées sur des collections qui ont grossi sans qu'on s'en aperçoive. Le vrai travail de tuning Redis, c'est souvent de repérer quelle commande a été écrite en pensant "petite collection" un jour où elle l'était vraiment. Voir [engineering-failures/redis-cache-stampede.md](../engineering-failures/redis-cache-stampede.md) pour un autre mode de panne classique, côté expiration cette fois plutôt que taille.
