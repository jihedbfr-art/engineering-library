# Neo4j

> Une base où la relation entre deux données coûte aussi peu cher à parcourir qu'un pointeur — pas une jointure recalculée à chaque requête.

## 🏗️ Architecture

Stockage natif en graphe : chaque nœud garde une liste chaînée de ses relations directement sur disque (index-free adjacency) — traverser de A vers ses voisins ne passe jamais par un index global, c'est un accès direct comparable à suivre un pointeur. C'est la différence structurelle avec "faire du graphe" sur une base relationnelle via une table de jointure : dans Neo4j, le coût d'un `MATCH (a)-[:FRIEND]->(b)` ne dépend quasiment pas de la taille totale du graphe, seulement du nombre de relations du nœud de départ. Le langage de requête est Cypher, déclaratif et pensé pour exprimer des motifs de graphe (`(a)-[:KNOWS]->(b)-[:KNOWS]->(c)`) de façon lisible, là où l'équivalent SQL en auto-jointures multiples devient vite illisible au-delà de deux ou trois sauts.

## 🔑 Index

Index B-tree classiques sur les propriétés de nœuds/relations pour les recherches de point d'entrée (`MATCH (u:User {email: $email})`), mais l'essentiel de la performance vient de la traversée native, pas de l'index — une fois le nœud de départ trouvé, parcourir ses relations ne nécessite plus aucun index. Neo4j propose aussi des index full-text et, depuis les versions récentes, des index vectoriels natifs pour la recherche par similarité — utile pour coupler un graphe de connaissances à une couche de recherche sémantique.

## 🔒 Transactions, locks, isolation

ACID complet, contrairement à beaucoup de bases NoSQL qui sacrifient la cohérence pour la performance. Isolation read-committed par défaut. Les écritures concurrentes sur les mêmes nœuds/relations posent des verrous ; un graphe très interconnecté avec beaucoup d'écritures concurrentes sur des nœuds "hub" (un nœud avec énormément de relations, ex: un tag très utilisé) peut devenir un point de contention — le pattern à surveiller est proche du "hot row" relationnel, transposé au graphe.

## 🌐 Réplication & partition

Causal Clustering : un cluster de cœurs (core servers) qui répliquent en écriture avec consensus (Raft), plus des read replicas asynchrones pour scaler la lecture. Pas de partitionnement horizontal natif façon sharding — Neo4j est pensé pour qu'un graphe entier tienne sur une instance (avec des replicas pour la charge de lecture), ce qui le rend moins adapté à des volumes de données qui dépasseraient largement la RAM/disque d'une seule machine costaude.

## 💾 Backup / Restore

`neo4j-admin database dump`/`load` pour un snapshot hors-ligne ; en édition Enterprise, des backups en ligne incrémentaux sans arrêter la base. Toujours tester le restore sur un environnement séparé avant de considérer une politique de sauvegarde comme fiable — c'est vrai pour tout moteur, mais particulièrement facile à négliger sur Neo4j qui reste moins présent dans les habitudes d'ops que PostgreSQL.

## 📊 Monitoring

Temps de traversée par requête Cypher (`PROFILE`/`EXPLAIN` pour voir le plan d'exécution, exactement comme `EXPLAIN ANALYZE` en SQL), taille du page cache versus taille réelle du graphe sur disque (si le graphe ne tient pas en cache, chaque traversée retombe sur disque et la latence explose), et le nombre de transactions actives/en attente pour repérer une contention sur des nœuds hub.

## Note perso

Le piège classique en venant du relationnel : modéliser le graphe comme on modéliserait des tables, avec des propriétés qui devraient être des nœuds séparés. Si "Ville" a du sens comme entité qu'on veut retraverser depuis plusieurs utilisateurs (`(u:User)-[:LIVES_IN]->(c:City)`), c'est un nœud ; si c'est juste un attribut qu'on n'interroge jamais indépendamment, ça reste une propriété sur `User`. La question à se poser à chaque propriété candidate : "est-ce que je vais un jour vouloir traverser DEPUIS cette valeur vers autre chose ?" Si oui, nœud. Sinon, propriété. Voir [database-engineering/postgresql-vs-oracle.md](postgresql-vs-oracle.md) pour le même genre de question de modélisation côté relationnel — le principe de fond (modéliser pour les requêtes qu'on va réellement faire, pas pour la pureté du schéma) est identique.
