# Requête lente par index manquant (full table scan silencieux)

> Une requête qui répondait en quelques millisecondes en dev, sur une table de quelques centaines
> de lignes, se met à prendre plusieurs secondes en production — pas parce que la requête a
> changé, mais parce que la table a grossi et que personne n'avait vérifié le plan d'exécution.

## 🔍 Cause

Un `WHERE`, un `JOIN`, ou un `ORDER BY` porte sur une colonne sans index. Sur une petite table, un
scan complet (parcourir toutes les lignes une par une) est indétectable en pratique — quelques
centaines de lignes se parcourent en microsecondes, la différence avec un accès indexé est
invisible. Le problème n'apparaît qu'à l'échelle : sur une table qui a grossi (des dizaines ou
centaines de milliers de lignes en production après des mois d'usage réel), le même scan complet
devient linéairement plus lent, et ce alors même qu'aucun changement de code n'a eu lieu — c'est
la donnée qui a changé, pas la requête. C'est ce qui rend ce genre de problème particulièrement
sournois : il ne se révèle jamais en dev/staging avec un jeu de données réduit, seulement en
production après un temps de croissance suffisant.

## 🚨 Symptômes

- Latence qui se dégrade **progressivement** sur plusieurs semaines/mois pour un même endpoint,
  sans déploiement correspondant — corrélée à la croissance du volume de la table, pas à un
  changement de code.
- CPU base de données qui monte alors que le volume de requêtes par seconde reste stable — chaque
  requête individuelle coûte plus cher à exécuter, pas qu'il y en a plus.
- Le comportement ne se reproduit pas en environnement de test avec un jeu de données réduit,
  ce qui rend le bug difficile à investiguer sans copier un volume de données représentatif.

## 🩺 Comment diagnostiquer

```sql
-- PostgreSQL : EXPLAIN ANALYZE revele immediatement un Seq Scan la ou un Index Scan est attendu
EXPLAIN ANALYZE
SELECT * FROM bookings WHERE guest_email = 'client@example.com';

-- Seq Scan on bookings (cost=0.00..18452.00 rows=1 width=120) (actual time=145.223..145.224 rows=1 loops=1)
--   Filter: (guest_email = 'client@example.com'::text)
--   Rows Removed by Filter: 499999
-- -> parcourt 500k lignes pour en retourner 1 : signature exacte d'un index manquant
```
Croiser avec `pg_stat_statements` pour identifier les requêtes les plus coûteuses en cumulé sur
une période, pas seulement la plus lente en instantané — une requête modérément lente mais
exécutée des milliers de fois par heure pèse souvent plus lourd sur la base qu'une requête très
lente mais rare.

## ✅ Solution

```sql
CREATE INDEX idx_bookings_guest_email ON bookings (guest_email);
```
Puis revérifier avec `EXPLAIN ANALYZE` que le plan passe bien de `Seq Scan` à `Index Scan` (ou
`Bitmap Index Scan` pour une requête qui retourne beaucoup de lignes) — créer l'index sans
revérifier le plan est une erreur fréquente : parfois le planificateur choisit quand même un scan
complet si la sélectivité de la colonne est trop faible (une colonne à deux valeurs possibles,
par exemple, où un index n'apporte souvent rien).

Pour un `ORDER BY` combiné à un `WHERE`, un index composé dans le bon ordre de colonnes évite un
tri en mémoire en plus du filtrage :
```sql
CREATE INDEX idx_bookings_room_checkin ON bookings (room_id, check_in);
```

## 🛡️ Prévention

- Tester les requêtes critiques avec un volume de données représentatif de la production (au
  moins un ordre de grandeur suffisant pour que le planificateur se comporte comme en prod), pas
  avec le jeu de données réduit du développement local — c'est la seule façon fiable de détecter
  ce problème avant qu'il n'atteigne la production.
- Revue systématique du plan d'exécution (`EXPLAIN ANALYZE`) pour toute nouvelle requête ajoutée
  sur une table déjà volumineuse ou destinée à le devenir, pas seulement en cas de plainte de
  lenteur a posteriori.
- Surveiller `pg_stat_statements` en continu plutôt que d'attendre un signalement utilisateur —
  la dégradation progressive décrite ici est justement le genre de problème qu'un monitoring actif
  détecte des semaines avant qu'un utilisateur ne s'en plaigne.

## 🔗 Liens

- [engineering-failures/hibernate-n-plus-1.md](hibernate-n-plus-1.md) — un autre mode de
  dégradation de performance base de données, côté nombre de requêtes plutôt que coût d'une
  requête individuelle
- [database-engineering/postgresql.md](../database-engineering/postgresql.md) — les types
  d'index disponibles et quand utiliser lequel
