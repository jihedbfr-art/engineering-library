# `deadlock detected` (PostgreSQL)

> Deux transactions (ou plus) s'attendent mutuellement pour libérer un verrou, PostgreSQL détecte
> le cycle et tue l'une des deux avec `ERROR: deadlock detected`.

## Causes probables (fréquentes → rares)
1. Deux transactions mettent à jour les mêmes lignes mais **dans un ordre différent** — la
   transaction A verrouille la ligne 1 puis attend la ligne 2, pendant que la transaction B
   verrouille la ligne 2 puis attend la ligne 1. C'est de loin la cause la plus fréquente.
2. Une transaction longue qui touche plusieurs tables dans un ordre incohérent selon le chemin de
   code emprunté (deux méthodes différentes qui mettent à jour les mêmes tables mais pas dans le
   même ordre selon qu'on passe par l'API REST ou un job batch, par exemple).
3. Un verrou explicite (`SELECT ... FOR UPDATE`) posé dans un ordre différent d'un appel à
   l'autre, typiquement quand la clause `WHERE` ne trie pas les lignes de façon déterministe.

## Diagnostic pas-à-pas
```sql
-- 1. Le message d'erreur PostgreSQL donne déjà les deux PID en conflit et les requêtes exactes :
--    "Process 1234 waits for ShareLock on transaction 5678; blocked by process 5678.
--     Process 5678 waits for ShareLock on transaction 1234; blocked by process 1234."
--    Les deux requêtes complètes sont loggées si log_lock_waits/log_statement sont actifs.

-- 2. Voir les verrous actifs en temps réel si le deadlock se reproduit régulièrement
SELECT pid, mode, relation::regclass, granted
FROM pg_locks
WHERE NOT granted;

-- 3. Croiser avec pg_stat_activity pour voir la requête exacte associée à chaque PID bloqué
SELECT pid, state, wait_event_type, query
FROM pg_stat_activity
WHERE pid IN (1234, 5678);
```
Activer les logs détaillés si ce n'est pas déjà fait, pour que le prochain deadlock se
diagnostique sans avoir à le reproduire en direct :
```
log_lock_waits = on
deadlock_timeout = 1s
```

## Correctif
- **Ordonner systématiquement les verrous** : si deux transactions touchent les mêmes lignes,
  s'assurer qu'elles le font toujours dans le même ordre (typiquement, trier par clé primaire
  avant de verrouiller — `SELECT ... FOR UPDATE ORDER BY id`). C'est le correctif le plus fiable,
  parce qu'il élimine la cause structurelle plutôt que de juste réduire la probabilité.
- **Réduire la portée de la transaction** : verrouiller le moins de lignes possible, le moins
  longtemps possible — une transaction courte laisse une fenêtre de conflit bien plus étroite
  qu'une transaction qui fait plusieurs allers-retours réseau avant de commit.
- **Retry côté application** : PostgreSQL tue automatiquement l'une des deux transactions en
  conflit (jamais les deux) — l'application doit être prête à retenter la transaction perdante
  plutôt que de simplement propager l'erreur à l'utilisateur. Un deadlock occasionnel sous forte
  concurrence n'est pas forcément un bug à éliminer à 100%, c'est un cas à gérer proprement.

```java
@Retryable(retryFor = DeadlockLoserDataAccessException.class, maxAttempts = 3,
           backoff = @Backoff(delay = 50, multiplier = 2))
@Transactional
public void updateAccounts(Long fromId, Long toId, BigDecimal amount) {
    // ordonner les IDs avant de verrouiller élimine la cause structurelle du deadlock ;
    // le retry reste un filet de sécurité, pas la solution principale
    Long first = fromId < toId ? fromId : toId;
    Long second = fromId < toId ? toId : fromId;
    accountRepository.findByIdForUpdate(first);
    accountRepository.findByIdForUpdate(second);
    // ... logique métier
}
```

## Si ça ne suffit pas
Si le deadlock implique plus de deux transactions ou des tables avec beaucoup de contraintes de
clé étrangère, vérifier aussi les verrous posés implicitement par les FK (`SELECT` sur la table
parente lors d'un `INSERT`/`UPDATE` sur la table enfant) — un deadlock peut impliquer une table à
laquelle aucune requête applicative ne fait référence explicitement dans le code incriminé.
