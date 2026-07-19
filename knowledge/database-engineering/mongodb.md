# MongoDB

> Base documentaire : schéma flexible par design, pertinente quand la forme des données varie d'un enregistrement à l'autre — pas un remplacement par défaut de PostgreSQL.

## 🏗️ Architecture

Stockage orienté document (BSON, essentiellement du JSON binaire typé) — chaque document peut avoir une structure différente des autres dans la même collection, il n'y a pas de schéma imposé au niveau du moteur (un schéma peut être appliqué côté application ou via `$jsonSchema` en validation, mais ce n'est jamais obligatoire). Un `mongod` gère un ou plusieurs replica sets ; en cluster (`sharding`), les données sont réparties sur plusieurs shards selon une **shard key** choisie à la création de la collection — ce choix est difficile à changer après coup, donc à ne pas prendre à la légère.

## 🔑 Index

- **Simple/composé** : comme en SQL, sur un ou plusieurs champs.
- **Multikey** : automatique dès qu'un champ indexé contient un tableau — indexe chaque élément individuellement.
- **Text** : recherche plein texte basique, un seul index texte par collection (limitation à connaître avant de s'y appuyer pour une vraie recherche produit).
- Comme partout : chaque index accélère la lecture mais coûte en écriture et en espace disque ; le piège spécifique à Mongo est l'index multikey sur un tableau qui grossit sans limite, qui dégrade silencieusement les performances d'écriture avec le temps.

## 🔒 Transactions, locks, isolation

Les transactions multi-documents ACID existent depuis la version 4.0 (répliqué) et 4.2 (shardé), mais restent plus coûteuses en performance qu'une écriture atomique sur un seul document — qui, elle, est toujours atomique nativement, sans transaction explicite nécessaire. La vraie discipline de modélisation Mongo consiste à **structurer les documents pour que les opérations qui doivent être atomiques le soient sur un seul document** (embedding plutôt que référence, quand ça a du sens) plutôt que de compter systématiquement sur des transactions multi-documents, qui existent surtout comme filet de sécurité, pas comme pattern par défaut.

## 🌐 Réplication & partition

Replica set (typiquement 3 nœuds minimum) : un primary reçoit les écritures, les secondaries répliquent de façon asynchrone — bascule automatique (election) si le primary tombe. Sharding horizontal pour scaler l'écriture au-delà d'un seul serveur, avec une shard key qui détermine la distribution : une mauvaise shard key (faible cardinalité, ou hotspot d'écriture sur une plage de valeurs monotone comme un timestamp) concentre la charge sur un seul shard et annule tout l'intérêt du sharding.

## 💾 Backup / Restore

`mongodump`/`mongorestore` pour des volumes modérés (attention : pas un snapshot cohérent à l'instant T sur un cluster shardé sans précaution supplémentaire) ; snapshot au niveau du volume de stockage (LVM, ou natif au cloud provider) pour un vrai plan de reprise cohérent sur un cluster de production.

## 📊 Monitoring

Ratio de cache WiredTiger (`serverStatus().wiredTiger.cache`), latence des opérations (`mongostat`, `mongotop` pour identifier les collections chaudes), lag de réplication entre primary et secondaries (`rs.printSecondaryReplicationInfo()`) — un lag qui grandit est le signal précoce le plus fiable d'un secondary qui ne suit plus, avant que ça devienne un problème de disponibilité en cas de failover.

## Note perso

Le piège le plus fréquent en venant du monde relationnel : traiter Mongo comme "PostgreSQL sans les contraintes", et recréer un modèle relationnel classique en Mongo avec des références partout à la place de foreign keys — ce qui donne le pire des deux mondes (pas de JOIN natif performant, pas de contraintes d'intégrité). Le bon réflexe Mongo, c'est d'embarquer (embedding) ce qui est lu/écrit ensemble, et de ne référencer que ce qui a un cycle de vie réellement indépendant. C'est un changement de façon de modéliser, pas juste un changement de moteur de stockage.
