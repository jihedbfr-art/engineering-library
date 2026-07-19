# ElasticSearch

> Pas une base de données transactionnelle — un moteur de recherche full-text distribué qu'on utilise parfois comme base secondaire, jamais comme source de vérité.

## 🏗️ Architecture

Construit sur Apache Lucene : chaque document est indexé en inversé (inverted index — pour chaque terme, la liste des documents qui le contiennent), ce qui rend la recherche full-text rapide au prix d'un index bien plus lourd que les données brutes. Un cluster ElasticSearch répartit les données en shards (fragments d'un index) distribués sur plusieurs nœuds, chaque shard pouvant avoir des replicas pour la disponibilité. La distinction à garder en tête en permanence : ElasticSearch est optimisé pour la recherche et l'agrégation sur de gros volumes, pas pour des écritures transactionnelles fréquentes et cohérentes — c'est presque toujours un système secondaire alimenté depuis une base de vérité (PostgreSQL, Kafka), jamais la base primaire d'une application métier.

## 🔑 Index

Le concept d'"index" ElasticSearch n'est pas comparable à un index SQL — c'est l'équivalent d'une table entière, avec son propre mapping (schéma des champs et de leur type d'analyse). À l'intérieur, chaque champ texte est passé par un analyzer (tokenization, minuscule, suppression des mots vides, stemming) qui détermine comment il devient cherchable — deux champs avec le même contenu mais un mapping différent (`text` analysé vs `keyword` exact) répondent complètement différemment à la même requête. Le mapping doit être pensé à la création de l'index ; le changer après coup sur un champ existant nécessite en général de réindexer.

## 🔒 Transactions, locks, isolation

Pas de transactions ACID multi-documents. Chaque document est indexé/mis à jour atomiquement individuellement, avec un contrôle de version optimiste (`_seq_no`/`_primary_term`) pour éviter les écritures concurrentes silencieusement écrasées — mais aucune notion de transaction qui engloberait plusieurs documents à la fois. Une écriture n'est pas immédiatement visible en recherche : par défaut, le "refresh" qui rend les nouveaux documents cherchables se produit toutes les secondes, pas instantanément (near-real-time, pas real-time) — un piège classique pour qui teste "j'écris puis je cherche aussitôt" dans le même test unitaire sans attendre le refresh.

## 🌐 Réplication & partition

Partitionnement natif dès la création de l'index (nombre de shards primaires fixé à la création, difficile à changer après coup sans réindexer intégralement — c'est le paramètre le plus important à bien dimensionner en amont). Chaque shard primaire a des replicas configurables, répartis automatiquement sur les nœuds du cluster pour la disponibilité et pour paralléliser la charge de lecture.

## 💾 Backup / Restore

Snapshots incrémentaux vers un dépôt externe (S3, système de fichiers partagé) via l'API snapshot — capture l'état d'un ou plusieurs index à un instant donné sans bloquer les écritures. Comme pour toute base secondaire alimentée depuis une source de vérité, la vraie question de résilience n'est souvent pas "combien de temps pour restaurer le snapshot" mais "combien de temps pour réindexer entièrement depuis la source primaire" — cette deuxième option étant parfois plus simple si l'index n'est qu'une projection dérivée.

## 📊 Monitoring

Latence de recherche et d'indexation (`_nodes/stats`), état de santé du cluster (green/yellow/red — yellow signifie des replicas non assignés, red signifie des shards primaires manquants, à traiter en urgence), utilisation JVM heap sur chaque nœud (ElasticSearch tourne sur la JVM et souffre des mêmes pauses GC qu'une application Java classique si le heap est mal dimensionné), et le nombre de segments Lucene par shard (trop de petits segments dégrade la recherche, un merge périodique les consolide).

## Note perso

Le réflexe à corriger le plus souvent chez qui découvre ElasticSearch après le relationnel : vouloir l'utiliser comme base de vérité "parce que la recherche est tellement plus rapide". Ce n'est pas un hasard si elle l'est — ElasticSearch sacrifie la cohérence transactionnelle stricte et la fraîcheur immédiate pour ça. Le pattern qui marche presque toujours : PostgreSQL (ou une autre base transactionnelle) reste la source de vérité, un connecteur ou un flux d'événements (Kafka) alimente ElasticSearch en quasi temps réel pour la recherche et les agrégations, et en cas de divergence, on peut toujours réindexer depuis la source. Perdre le snapshot ElasticSearch n'est jamais une catastrophe si cette discipline est respectée.
