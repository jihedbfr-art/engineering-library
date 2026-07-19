# Connection leak (pool de connexions qui ne se vide jamais)

> Le pool de connexions base de données s'épuise progressivement en production, pas sous un pic de charge ponctuel mais sur la durée — chaque connexion "empruntée" finit par ne plus jamais revenir dans le pool, jusqu'à ce qu'il n'en reste plus aucune de disponible.

## 🔍 Cause

Une connexion JDBC empruntée au pool (HikariCP typiquement) doit être explicitement rendue (`close()`) pour redevenir disponible — `close()` sur une connexion poolée ne ferme pas la connexion réseau sous-jacente, elle la remet dans le pool. Le leak se produit quand un chemin de code emprunte une connexion et ne passe jamais par ce `close()`, le plus souvent parce qu'une exception est levée entre l'ouverture et la fermeture sans `try-with-resources` ni `finally` pour garantir la libération quel que soit le chemin de sortie. Contrairement à un pic de trafic qui épuise le pool temporairement (voir [debugging-recipes/connexion-pool-epuise.md](../debugging-recipes/connexion-pool-epuise.md) pour le diagnostic symptôme par symptôme), un vrai leak ne se résorbe jamais tout seul même quand le trafic retombe — les connexions perdues le restent définitivement jusqu'au redémarrage de l'application.

## 🚨 Symptômes

- Le nombre de connexions actives du pool (`HikariPoolMXBean.getActiveConnections()`) augmente en tendance sur plusieurs heures, sans jamais redescendre au niveau de repos habituel même en période de faible trafic — c'est le signal distinctif face à un simple pic temporaire.
- Timeouts `Connection is not available, request timed out` qui apparaissent d'abord de façon isolée puis de plus en plus fréquemment, avec un pattern qui correspond au volume cumulé de requêtes traitées depuis le dernier redémarrage plutôt qu'au trafic instantané.
- Côté base de données, `pg_stat_activity` (PostgreSQL) montre un nombre de connexions ouvertes par l'application qui ne redescend jamais, souvent avec plusieurs connexions en état `idle` depuis très longtemps.

## 🩺 Comment diagnostiquer

```bash
# 1. Suivre la tendance du pool dans le temps, pas juste sa valeur instantanée
# (exposé via Actuator + Micrometer si configuré)
curl localhost:8080/actuator/metrics/hikaricp.connections.active

# 2. Côté base, repérer les connexions ouvertes depuis anormalement longtemps
```
```sql
SELECT pid, state, state_change, now() - state_change AS duree_idle, query
FROM pg_stat_activity
WHERE application_name = 'mon-service' AND state = 'idle'
ORDER BY state_change ASC
LIMIT 20;
```
```
# 3. Activer le leak detection de HikariCP en environnement de test/pré-prod
# pour capturer la stack trace exacte du code qui a emprunté une connexion
# sans la rendre dans le délai configuré
spring.datasource.hikari.leak-detection-threshold=30000   # 30s, log un warning avec stack trace
```
Le `leak-detection-threshold` est l'outil le plus direct : au lieu de deviner où chercher dans le code, HikariCP logue la stack trace exacte de l'emprunt de connexion qui n'a pas été rendue dans le délai — inutile de reproduire manuellement, il suffit de laisser tourner sous trafic normal avec le seuil activé.

## ✅ Solution

Le correctif est toujours le même une fois la stack trace du leak identifiée : garantir la fermeture quel que soit le chemin de sortie (succès, exception, retour anticipé).
```java
// leak potentiel — si getBalance() lève une exception, close() n'est jamais appelé
Connection conn = dataSource.getConnection();
BigDecimal balance = getBalance(conn);
conn.close();

// correct — try-with-resources garantit close() dans tous les cas
try (Connection conn = dataSource.getConnection()) {
    BigDecimal balance = getBalance(conn);
    return balance;
}
```
Avec Spring Data JPA, le leak vient rarement d'une `Connection` manipulée directement (le framework la gère) mais plus souvent d'une session Hibernate maintenue ouverte plus longtemps que prévu — un `EntityManager` injecté manuellement et jamais fermé, ou un `@Transactional` sur une méthode qui appelle un traitement bien plus long que la transaction ne devrait durer, retardant d'autant la libération de la connexion sous-jacente.

## 🛡️ Prévention

- `try-with-resources` systématique pour toute ressource JDBC manipulée directement, sans exception — c'est le genre de règle qu'une revue de code doit vérifier explicitement plutôt que faire confiance à l'habitude.
- Activer `leak-detection-threshold` en environnement de test/pré-production en continu (pas seulement pour investiguer un incident déjà survenu) — le coût est négligeable et il attrape le problème avant qu'il n'atteigne la production.
- Garder les transactions courtes : une transaction `@Transactional` qui englobe un appel réseau externe lent ou un traitement long retient une connexion pendant toute sa durée, ce qui n'est techniquement pas un leak mais produit le même symptôme d'épuisement du pool sous charge.

## 🔗 Liens
- [debugging-recipes/connexion-pool-epuise.md](../debugging-recipes/connexion-pool-epuise.md) — le diagnostic symptôme par symptôme, y compris les cas de pic temporaire non liés à un leak
- [wrong-transaction.md](wrong-transaction.md) — une transaction mal bornée peut retenir une connexion bien plus longtemps que nécessaire sans être un leak au sens strict
