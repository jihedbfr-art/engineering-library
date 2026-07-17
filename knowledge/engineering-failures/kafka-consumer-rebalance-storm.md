# Rebalance storm (Kafka)

> Un consumer group qui n'arrête plus de rebalancer : chaque rebalance stoppe toute consommation le
> temps de la synchronisation, et si le cycle se répète en boucle, le lag explose sans qu'aucun
> consumer n'ait l'air "en panne" individuellement.

## 🔍 Cause
Le rebalance se déclenche à chaque changement de membership du groupe (un consumer qui rejoint,
quitte, ou est jugé mort par le broker). Un consumer est jugé mort si `max.poll.interval.ms` est
dépassé entre deux appels à `poll()` — typiquement parce que le traitement d'un batch de messages
prend plus longtemps que l'intervalle configuré (appel réseau lent en aval, GC pause longue, ou
simplement un traitement plus lourd que prévu au moment du dimensionnement initial). Le broker
l'exclut du groupe, déclenche un rebalance, le consumer revient au poll suivant, rejoint à nouveau
→ nouveau rebalance. En environnement de mediation/billing avec un débit élevé d'événements
d'usage, ce cycle peut se répéter en continu sans jamais se stabiliser.

## 🚨 Symptômes
- Lag consumer qui croît de façon erratique, pas linéaire — des sauts corrélés aux logs de
  rebalance, pas une dérive constante.
- Logs applicatifs remplis de `Attempting to auto-commit` / `Revoking previously assigned
  partitions` / `(Re-)joining group` en boucle serrée.
- Débit de traitement qui s'effondre alors que la CPU des consumers reste basse — le temps passé en
  rebalance ne fait pas de travail utile.
- Symptôme trompeur : chaque consumer pris individuellement semble en bonne santé (pas d'erreur,
  pas de crash) — le problème est visible seulement au niveau du groupe.

## 🩺 Comment diagnostiquer
Vérifier l'état du groupe côté broker :
```bash
kafka-consumer-groups.sh --bootstrap-server <broker> --describe --group <group-id> --state
```
Chercher `PreparingRebalance` / `CompletingRebalance` qui reviennent fréquemment plutôt qu'un état
`Stable` durable. Croiser avec les logs applicatifs pour repérer si le déclencheur est un
`max.poll.interval.ms` dépassé :
```
Consumer <id> ... member ... left the group because its poll loop is spending too much time
between poll calls
```
Mesurer le temps réel de traitement d'un batch de messages (métrique applicative autour de l'appel
métier dans le `poll()`) et comparer à `max.poll.interval.ms` configuré — c'est presque toujours là
que l'écart se révèle.

## ✅ Solution
Deux leviers, à combiner selon la cause réelle :
- **Réduire `max.poll.records`** pour que chaque batch traité entre deux `poll()` soit plus petit,
  donc plus rapide à traiter — solution la plus rapide à déployer, sans changer l'architecture.
- **Augmenter `max.poll.interval.ms`** si le traitement est légitimement long (appel à un système
  aval lent) — mais cela retarde aussi la détection d'un consumer réellement mort, à doser.
- **Sortir le traitement lourd du thread de poll** : accuser réception rapidement (`poll` +
  commit), déléguer le traitement métier à un pool de threads séparé — plus robuste sur le long
  terme mais demande de revoir la logique de commit (risque de perte si mal fait, cf.
  `at-least-once` vs `at-most-once`).

```properties
# point de départ raisonnable si le traitement par message est de l'ordre de 50-200ms
max.poll.records=50
max.poll.interval.ms=300000
session.timeout.ms=45000
```

## 🛡️ Prévention
- Mesurer le temps de traitement par batch **avant** de fixer `max.poll.records` en prod, pas après
  coup — dimensionner sur la base d'un test de charge réaliste, pas sur la valeur par défaut.
- Alerter sur la fréquence de rebalance du groupe (pas seulement sur le lag), c'est le signal
  précoce avant que le lag ne devienne visible.
- Si le traitement dépend d'un appel réseau externe (cas fréquent en mediation : enrichissement
  d'un événement d'usage via un service tiers), prévoir un timeout strict et un circuit breaker sur
  cet appel — un appel externe qui traîne est la cause la plus fréquente de dépassement de
  `max.poll.interval.ms` en pratique.

## 🔗 Liens
- [data-engineering/streaming-kafka.md](../data-engineering/streaming-kafka.md)
- [telecom/billing/mediation.md](../telecom/billing/mediation.md)
- [performance-recipes/](../performance-recipes/) — dimensionnement de pools et circuit breakers
