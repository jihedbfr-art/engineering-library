# `ORA-01013`/timeout de connexion ou de requête (Oracle)

> Une requête ou une tentative de connexion Oracle dépasse le délai configuré et échoue avec `ORA-01013: user requested cancel of current operation` ou un timeout JDBC générique (`SQLTimeoutException`), sans que ce soit toujours évident si le problème vient de la requête, du réseau ou du pool de connexions.

## Causes probables (fréquentes → rares)

1. Requête réellement lente côté base — plan d'exécution dégradé (statistiques obsolètes, index manquant ou non utilisé, jointure mal ordonnée) qui dépasse le timeout configuré côté client avant de se terminer.
2. Verrou bloquant : la requête attend qu'une autre transaction relâche un verrou sur les mêmes lignes, et le timeout expire avant que ce verrou ne se libère — dans ce cas la requête elle-même serait rapide si elle pouvait s'exécuter tout de suite.
3. Pool de connexions épuisé côté application, la requête attend une connexion disponible plus longtemps que le timeout d'attente configuré (à distinguer du timeout d'exécution — voir [connexion-pool-epuise.md](connexion-pool-epuise.md) si c'est le vrai symptôme).
4. Latence réseau anormale entre l'application et l'instance Oracle (VPN, passerelle, changement de topologie réseau récent) — plus fréquent qu'on ne le pense sur des architectures avec la base hébergée dans un environnement séparé de l'application (cas typique en télécom/BSS où la base legacy reste sur une infra dédiée).
5. Timeout configuré trop bas par rapport au comportement normal de la requête sous charge réelle — un timeout qui passait en dev avec un jeu de données réduit ne passe plus en prod avec le volume réel.

## Diagnostic pas-à-pas

```sql
-- 1. Identifier les sessions actives et depuis combien de temps elles attendent
SELECT sid, serial#, status, event, seconds_in_wait, sql_id
FROM v$session
WHERE username = 'MON_USER' AND status = 'ACTIVE';

-- 2. Si event indique un verrou (enq: TX, enq: TM...), trouver le bloqueur
SELECT blocking_session, sid, serial#, wait_class, event
FROM v$session
WHERE blocking_session IS NOT NULL;

-- 3. Voir le plan d'exécution réel de la requête suspecte
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_CURSOR(sql_id => 'abc123xyz'));

-- 4. Vérifier l'âge des statistiques de la table concernée
SELECT table_name, last_analyzed, num_rows
FROM user_tables
WHERE table_name = 'MA_TABLE';
```
Côté application (log JDBC/pool), vérifier si le timeout déclenché est un timeout de connexion (attente d'une connexion libre dans le pool) ou un timeout de requête (`queryTimeout` du driver) — le message d'erreur ou la stack trace complète distingue en général les deux, mais c'est facile de les confondre en lisant vite.

## Correctif

- **Statistiques obsolètes** : `EXEC DBMS_STATS.GATHER_TABLE_STATS('SCHEMA', 'MA_TABLE');` — souvent suffisant à lui seul pour faire revenir l'optimiseur sur un bon plan d'exécution.
- **Verrou bloquant** : traiter la cause côté transaction bloquante (transaction trop longue, ordre de verrouillage incohérent — voir [deadlock-postgres.md](deadlock-postgres.md) pour le même principe de fond côté PostgreSQL, la logique de diagnostic se transpose).
- **Timeout mal calibré** : augmenter le timeout côté driver JDBC (`oracle.jdbc.ReadTimeout` ou équivalent au niveau du pool) après avoir mesuré le temps d'exécution réel de la requête en conditions normales — ne jamais augmenter un timeout sans avoir d'abord vérifié qu'il ne masque pas une requête réellement pathologique.
- **Latence réseau** : mesurer la latence brute entre l'application et l'instance (`tnsping`, ou un simple `SELECT 1 FROM DUAL` chronométré) indépendamment de la requête suspecte, pour isoler si le problème est réseau ou requête.

## Si ça ne suffit pas

Si le timeout revient de façon intermittente sans corrélation claire avec la charge ou une requête précise, vérifier les paramètres réseau côté `sqlnet.ora` (`SQLNET.EXPIRE_TIME`, `SQLNET.RECV_TIMEOUT`) — sur des architectures avec un firewall ou un load balancer entre l'application et Oracle, une connexion TCP peut être coupée silencieusement côté réseau sans qu'Oracle ni l'application ne le détectent immédiatement, ce qui ressemble à un timeout de requête alors que la cause est une connexion déjà morte.
