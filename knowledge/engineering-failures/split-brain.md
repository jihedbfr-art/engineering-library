# Split brain (deux instances se croient toutes les deux leader)

> Une coupure réseau partielle sépare un cluster en deux groupes qui continuent chacun de fonctionner indépendamment, chacun persuadé d'être le seul en charge — résultat : deux "leaders" actifs en même temps, avec des écritures divergentes ou un job dupliqué qui s'exécute deux fois.

## 🔍 Cause

Tout système qui élit un leader unique pour éviter qu'une action ne s'exécute en double (un job planifié, un traitement de queue, une écriture consolidée) repose sur l'hypothèse que les instances peuvent se coordonner de façon fiable. Un split brain survient quand une coupure réseau (partition) sépare le cluster en deux sous-groupes qui ne se voient plus mutuellement, mais voient chacun encore une source de coordination (par exemple, chaque groupe garde l'accès à sa propre réplique de base de données ou à son propre Zookeeper/etcd local) — sans mécanisme de quorum strict, les deux groupes peuvent chacun conclure "je suis le seul survivant, je devrais être leader" et agir en conséquence simultanément.

## 🚨 Symptômes

- Un job planifié censé s'exécuter une seule fois s'exécute en double sur la même fenêtre de temps, généré depuis deux instances différentes — typiquement détecté par des effets de bord dupliqués (deux notifications envoyées pour le même événement, une facture générée deux fois).
- Logs applicatifs montrant deux instances qui revendiquent simultanément le rôle de leader dans une fenêtre de temps qui coïncide avec un incident réseau ou une latence anormale entre datacenters/zones.
- Incohérence de données qui ne peut s'expliquer que par deux écritures concurrentes venant de deux sources qui ne se savaient pas concurrentes.

## 🩺 Comment diagnostiquer

Le split brain est difficile à observer en flagrant délit — il se prouve après coup, en croisant les logs des deux instances impliquées avec les métriques réseau de la période :
```bash
# Corréler les timestamps des logs des deux instances suspectées d'avoir agi en double
# avec les métriques de connectivité réseau/latence inter-zone de la même fenêtre
kubectl logs instance-a --since-time="2026-07-15T03:00:00Z" | grep -i leader
kubectl logs instance-b --since-time="2026-07-15T03:00:00Z" | grep -i leader
```
```sql
-- Si le leadership est arbitré par verrou distribué en base (ex: pg_advisory_lock),
-- vérifier l'historique des acquisitions/libérations autour de l'incident
SELECT * FROM pg_locks WHERE locktype = 'advisory';
```
Si l'infrastructure sous-jacente (Zookeeper, etcd, base de données répliquée) a ses propres logs d'événements de partition réseau, ils confirment généralement la fenêtre exacte de la coupure — croiser cette fenêtre avec le moment où les deux instances ont chacune décidé d'agir en leader confirme le diagnostic.

## ✅ Solution

- **Quorum plutôt que détection locale** : un mécanisme d'élection de leader doit exiger l'accord d'une majorité stricte de nœuds (`(N/2)+1`), jamais une décision qu'un nœud isolé peut prendre seul en se basant uniquement sur ce qu'il voit de son côté de la partition — c'est le principe même qui rend Raft/Paxos plus sûrs qu'une élection ad-hoc.
- **Verrou distribué avec bail (lease) à expiration courte** plutôt qu'un simple flag "je suis leader" : avec ShedLock ou un verrou PostgreSQL (`pg_try_advisory_lock`) à durée de vie limitée, un leader qui perd la connectivité au point de coordination central perd automatiquement son statut de leader après expiration, au lieu de continuer à agir indéfiniment sur la base d'un statut obtenu avant la coupure.
```java
@Scheduled(cron = "0 0 * * * *")
@SchedulerLock(name = "generation-facture-mensuelle", lockAtMostFor = "PT10M", lockAtLeastFor = "PT1M")
public void genererFacturesMensuelles() { /* ... */ }
```
- **Fencing** : quand la reconnexion a lieu après une partition, l'ancien leader potentiellement "zombie" doit être explicitement empêché d'écrire (token de génération incrémenté à chaque nouvelle élection, toute écriture avec un token obsolète est rejetée), pas seulement supposé s'être arrêté de lui-même.

## 🛡️ Prévention

- Ne jamais implémenter une élection de leader maison basée sur une simple vérification locale ("je ping l'autre instance, si ça ne répond pas je deviens leader") — utiliser un mécanisme de verrou distribué éprouvé (ShedLock, un verrou base de données avec bail court) plutôt que de réinventer un consensus distribué, notoirement difficile à faire correctement.
- Fenêtre de bail (lease) courte sur tout verrou de leadership, pour limiter la durée pendant laquelle un split brain, s'il survient malgré tout, peut avoir un effet réel avant expiration automatique.
- Idempotence de toute action déclenchée par le leader (voir la discipline appliquée côté [event-driven](../architecture-library/event-driven.md)) comme filet de sécurité supplémentaire — même avec un bon mécanisme de leadership, traiter l'action déclenchée comme potentiellement dupliquée limite l'impact réel d'un split brain résiduel.

## 🔗 Liens
- [architecture-library/event-driven.md](../architecture-library/event-driven.md) — l'idempotence côté consommateur est la même discipline qui limite les dégâts d'un split brain
- [debugging-recipes/deadlock-postgres.md](../debugging-recipes/deadlock-postgres.md) — autre pathologie de coordination, différente mais dans la même famille de problèmes de concurrence distribuée
