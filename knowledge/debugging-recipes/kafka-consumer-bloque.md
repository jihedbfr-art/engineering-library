# Consumer Kafka bloqué (lag qui ne bouge plus)

> Le consumer group est visible, `Stable`, aucun rebalance en boucle — mais le lag ne descend
> plus du tout, comme si le consumer avait simplement arrêté de traiter des messages.

## Causes probables (fréquentes → rares)
1. **Un message "poison"** : un message dont le traitement lève systématiquement une exception,
   et le code retente indéfiniment le même message sans jamais avancer l'offset — le consumer
   n'est pas planté, il boucle silencieusement sur le même message.
2. **Deadlock ou blocage applicatif** dans le thread de traitement (attente sur un lock, un appel
   réseau vers un service down sans timeout configuré) — le `poll()` ne revient jamais, donc
   aucun nouveau message n'est consommé, mais le consumer reste membre du groupe tant que
   `max.poll.interval.ms` n'est pas dépassé.
3. **Partition sans consumer assigné** : plus de partitions que de consumers actifs dans le
   groupe, ou un consumer mort dont la partition n'a jamais été réassignée — le lag sur cette
   partition précise grossit indéfiniment pendant que les autres partitions avancent normalement.
4. **Commit manuel oublié ou mal placé** : un `commitSync()`/`commitAsync()` qui ne s'exécute
   jamais dans un chemin d'erreur, donnant l'illusion que les messages sont traités (logs de
   succès visibles) alors que l'offset committé ne bouge pas — au redémarrage, tout est reconsommé
   depuis le dernier offset committé réel.

## Diagnostic pas-à-pas
```bash
# 1. Etat du groupe - LAG par partition, pas juste le total
kafka-consumer-groups.sh --bootstrap-server <broker> --describe --group <group-id>
# Reperer une partition specifique dont le LAG grossit pendant que les autres sont stables

# 2. Le consumer est-il toujours membre actif du groupe, ou a-t-il ete exclu silencieusement ?
#    Colonne CONSUMER-ID vide = partition non assignee a un consumer actif

# 3. Thread dump du process consumer pour voir sur quoi le thread de poll/traitement est bloque
jstack <pid> | grep -A20 "kafka-consumer\|poll-thread"

# 4. Verifier si le meme message revient en boucle dans les logs applicatifs
#    (meme offset, meme cle, exception identique repetee)
grep "processing message" app.log | tail -50
```

## Correctif
Selon la cause identifiée :
- **Message poison** : mettre en place une **dead letter queue (DLQ)** — après N tentatives
  échouées, publier le message dans un topic dédié aux erreurs et committer l'offset pour avancer,
  au lieu de bloquer indéfiniment sur le même message. Spring Kafka le fait nativement via
  `DefaultErrorHandler` + `DeadLetterPublishingRecoverer`.
```java
@Bean
DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
    return new DefaultErrorHandler(
        new DeadLetterPublishingRecoverer(template),
        new FixedBackOff(1000L, 3)   // 3 tentatives avant DLQ, pas de boucle infinie
    );
}
```
- **Blocage applicatif** : ajouter des timeouts stricts sur tout appel externe fait depuis le
  traitement d'un message — un appel sans timeout dans un consumer Kafka est la cause la plus
  fréquente de ce symptôme précis, voir aussi
  [engineering-failures/kafka-consumer-rebalance-storm.md](../engineering-failures/kafka-consumer-rebalance-storm.md)
  pour le mode de défaillance apparenté (le blocage y déclenche un rebalance en boucle plutôt
  qu'un lag figé, selon que `max.poll.interval.ms` est dépassé ou non).
- **Partition orpheline** : vérifier le nombre d'instances actives par rapport au nombre de
  partitions — un déploiement qui a réduit le nombre d'instances sans réduire le nombre de
  partitions laisse certaines partitions sans consumer tant qu'un rebalance ne les réassigne pas.
- **Commit mal placé** : s'assurer que le commit se fait dans un `finally` ou après confirmation
  explicite du traitement réussi, jamais avant, et jamais dans un chemin qui peut être sauté par
  une exception non catchée.

## Si ça ne suffit pas
Si le lag grossit sur toutes les partitions de façon uniforme (pas une seule), c'est plus
probablement un problème de débit de traitement insuffisant qu'un blocage — comparer le débit de
consommation réel aux `max.poll.records`/temps de traitement par message, voir
[kafka-consumer-rebalance-storm.md](../engineering-failures/kafka-consumer-rebalance-storm.md)
pour le dimensionnement.
