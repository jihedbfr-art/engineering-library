# Cache stampede (Redis)

> Une clé chaude expire, et au lieu d'une régénération, ce sont des centaines de requêtes qui frappent la base en même temps — le cache censé la protéger devient le déclencheur de sa surcharge.

## 🔍 Cause

Une clé très demandée (config partagée, résultat d'agrégation, référentiel tarifaire) a un TTL. Au moment précis où elle expire, toutes les requêtes qui arrivent en même temps constatent un cache miss et vont, chacune indépendamment, recalculer la valeur en base — au lieu qu'une seule requête recalcule pendant que les autres attendent. Si le calcul est coûteux (jointure lourde, agrégation, appel à un service tiers) et que le trafic est important, cette fenêtre de quelques dizaines à quelques centaines de millisecondes suffit à envoyer un pic de charge brutal sur la base, potentiellement suffisant pour la faire chuter — ce qui aggrave encore la situation puisque les requêtes suivantes échouent aussi.

## 🚨 Symptômes

- Pic de charge base de données parfaitement corrélé, à la seconde près, avec l'expiration d'une clé Redis précise — pas une dérive progressive.
- Latence applicative qui explose brièvement puis se stabilise, en dents de scie répétées si la clé a un TTL court et un trafic soutenu.
- `keyspace_misses` Redis qui monte en pic synchronisé plutôt qu'en tendance lente.
- Dans les cas sévères : le pool de connexions base s'épuise pendant le pic (voir [debugging-recipes/connexion-pool-epuise.md](../debugging-recipes/connexion-pool-epuise.md)), ce qui transforme un problème de cache en incident de disponibilité complet.

## 🩺 Comment diagnostiquer

Corréler l'horodatage du pic de charge base avec les logs d'expiration/miss Redis :
```bash
redis-cli --latency-history -i 1
redis-cli info stats | grep keyspace
```
Identifier la ou les clés en cause en loggant, côté applicatif, chaque cache miss avec le nom de clé — sur une infra à fort trafic, il suffit généralement de regarder quelles clés ont le TTL le plus court combiné au taux de lecture le plus élevé pour trouver le coupable avant même que l'incident se reproduise.

## ✅ Solution

Trois approches, pas mutuellement exclusives :
- **Lock de régénération** : la première requête qui constate le miss pose un verrou court (`SET key value NX PX 5000`), recalcule, puis republie la valeur — les autres requêtes, pendant ce temps, servent l'ancienne valeur (si elle est encore lisible sous une autre clé) ou attendent/retentent au lieu de recalculer chacune de leur côté.
- **Early expiration probabiliste** (XFetch) : une petite fraction des requêtes, dans la fenêtre juste avant l'expiration réelle, déclenche volontairement une régénération anticipée — pondérée pour que la probabilité augmente à mesure qu'on approche du TTL. Étale la charge de régénération au lieu de la concentrer sur un seul instant.
- **TTL avec jitter** : au lieu d'un TTL fixe identique pour toutes les entrées d'un même type, ajouter une variation aléatoire (±10-20%) pour désynchroniser les expirations — utile surtout quand beaucoup de clés similaires ont été peuplées en même temps (ex. warm-up de cache au démarrage) et expireraient donc toutes ensemble sans ce correctif.

```java
// lock de régénération, version simplifiée
String cached = redis.get(key);
if (cached == null) {
    boolean gotLock = redis.set(lockKey(key), "1", "NX", "PX", 5000);
    if (gotLock) {
        cached = expensiveRecompute();
        redis.set(key, cached, "EX", ttlWithJitter());
        redis.del(lockKey(key));
    } else {
        cached = redis.get(staleFallbackKey(key)); // ou attente courte + retry
    }
}
```

## 🛡️ Prévention

- Sur toute clé identifiée comme "chaude" (lecture très fréquente, calcul coûteux), appliquer systématiquement un jitter sur le TTL dès sa création — c'est le correctif le moins cher et il élimine la cause la plus fréquente en pratique (beaucoup de clés peuplées ensemble, expirant ensemble).
- Surveiller le ratio hit/miss par préfixe de clé, pas seulement globalement — une baisse localisée sur un préfixe précis est le signal précoce avant que le pic base ne devienne visible en production.
- Pour les valeurs vraiment critiques et coûteuses à recalculer, envisager un TTL "logique" côté applicatif plus court que le TTL Redis réel : on sert une valeur légèrement périmée pendant qu'une régénération asynchrone a lieu en arrière-plan, sans jamais laisser la clé expirer pour de vrai côté Redis.

## 🔗 Liens

- [database-engineering/redis.md](../database-engineering/redis.md)
- [debugging-recipes/connexion-pool-epuise.md](../debugging-recipes/connexion-pool-epuise.md) — le mode de défaillance en aval quand le stampede n'est pas contenu
