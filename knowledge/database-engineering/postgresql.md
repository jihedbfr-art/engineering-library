# PostgreSQL

> Base relationnelle open-source de référence pour ce stack (projets standard/macro et
> `notes-app-microservices`) : robuste, riche en index, excellent rapport coût/fonctionnalités.

## 🏗️ Architecture
Modèle process-per-connection (chaque connexion est un processus OS, pas un thread léger) — d'où
l'importance de dimensionner le pool applicatif (cf.
[performance-recipes/hikaricp-pool-sizing.md](../performance-recipes/hikaricp-pool-sizing.md))
plutôt que d'ouvrir des connexions sans contrôle.

## 🔑 Index
- **B-tree** (par défaut) : égalité et plages ordonnées, le cas le plus courant.
- **GIN** : recherche dans des types composites (JSONB, tableaux, recherche plein texte).
- **HNSW/IVFFlat** (extension PGVector) : recherche par similarité vectorielle (cf.
  `knowledge/ai-engineering/07-Vector-Databases`).
- Chaque index accélère la lecture mais ralentit l'écriture (mise à jour de l'index à chaque
  insert/update) — ne pas indexer une colonne rarement filtrée.

## 🔒 Transactions, locks, isolation
MVCC (Multi-Version Concurrency Control) : les lecteurs ne bloquent jamais les écrivains et
inversement. Niveau d'isolation par défaut `READ COMMITTED` — suffisant pour la majorité des cas ;
`SERIALIZABLE` nécessaire seulement pour des invariants métier stricts (ex. pas de double
réservation) où deux transactions concurrentes pourraient produire un résultat incohérent.

## 🌐 Réplication & partition
Réplication physique en streaming (WAL) pour la haute disponibilité en lecture ; partitionnement de
table natif (`PARTITION BY RANGE/LIST`) utile au-delà de plusieurs dizaines de millions de lignes
sur une même table.

## 💾 Backup / Restore
`pg_dump`/`pg_restore` pour des volumes modérés ; sauvegarde physique (`pg_basebackup` + archivage
WAL) pour un plan de reprise d'activité avec un point de restauration précis (PITR).

## 📊 Monitoring
Connexions actives (`pg_stat_activity`), requêtes lentes (`pg_stat_statements`), taux de cache hit
(`pg_statio_user_tables`), bloat des tables/index à surveiller en priorité.
