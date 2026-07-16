# Oracle Database

> Base de référence en environnement d'entreprise historique (télécom, ERP) — encore massivement
> présente en production dans des systèmes BSS/provisioning et des ERP legacy (cf.
> `knowledge/telecom`, `knowledge/legacy-modernization/oracle-adf.md`).

## 🏗️ Architecture
Modèle multi-thread partagé (contrairement au process-per-connection de PostgreSQL) : plusieurs
sessions partagent des processus serveur, ce qui change la façon de dimensionner les pools de
connexions et d'interpréter les métriques de charge.

## 🔑 Index
B-tree par défaut ; index bitmap pertinent pour des colonnes à faible cardinalité en contexte
décisionnel (reporting), à éviter en OLTP à forte volumétrie d'écriture (contention accrue).
Toujours vérifier le plan d'exécution (`EXPLAIN PLAN`) avant de conclure qu'un index manque.

## 🔒 Transactions, locks, isolation
Niveau `READ COMMITTED` par défaut, avec un mécanisme de lecture cohérente basé sur les segments
d'annulation (undo) plutôt que le versionnage de lignes de PostgreSQL — les lecteurs ne bloquent
pas non plus les écrivains, mais le mécanisme sous-jacent diffère (important pour dimensionner les
segments d'annulation sur des transactions longues).

## 🌐 Réplication & partition
Data Guard pour la haute disponibilité/reprise après sinistre ; partitionnement de table riche
(range, hash, composite) déjà mature de longue date, pertinent pour les grandes tables de
provisioning/historique.

## 💾 Backup / Restore
RMAN (Recovery Manager) comme outil de référence, avec sauvegarde incrémentale et point de
restauration précis.

## 📊 Monitoring
AWR (Automatic Workload Repository) et ASH (Active Session History) pour le diagnostic de
performance ; surveiller en priorité les temps d'attente (`wait events`) plutôt que la seule charge
CPU pour identifier un goulot d'étranglement réel.
