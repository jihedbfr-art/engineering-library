# Consumer lag Kafka non détecté

> Un service consommateur Kafka accumule un retard de traitement croissant sans alerte, jusqu'à ce
> que les utilisateurs constatent des notifications ou mises à jour arrivant très en retard.

## 🔍 Cause
Le consommateur traite les messages plus lentement qu'ils n'arrivent (traitement synchrone lourd
dans le listener, ou sous-dimensionnement du nombre de partitions/instances), et aucune métrique de
lag n'est surveillée activement — le système continue de fonctionner, juste de plus en plus en
retard, sans jamais lever d'erreur explicite.

## 🚨 Symptômes
- Notifications ou mises à jour dérivées d'événements Kafka qui arrivent avec un délai croissant.
- Aucune erreur dans les logs applicatifs — le service tourne normalement, juste en retard.
- Le nombre de messages non consommés (lag) augmente en continu si observé après coup.

## 🩺 Comment diagnostiquer
```text
# 1. Vérifier le lag par groupe de consommateurs
kafka-consumer-groups --bootstrap-server <broker> --describe --group notification-service
# 2. Comparer le débit de production (messages/s publiés) au débit de consommation réel
# 3. Vérifier si le traitement dans le listener contient un appel bloquant lent (HTTP, requête lourde)
```

## ✅ Solution
- Sortir tout traitement lourd/bloquant du listener Kafka lui-même (déléguer à un traitement
  asynchrone si nécessaire), pour que la consommation reste rapide.
- Augmenter le nombre de partitions et d'instances consommatrices si le volume dépasse la capacité
  d'une seule instance à absorber le débit de production.

## 🛡️ Prévention
Mettre en place une alerte sur le lag consumer dès la mise en production (pas après coup) — le lag
est une métrique standard exposée par Kafka, son absence de surveillance est le vrai problème
racine ici, pas seulement la lenteur du traitement.

## 🔗 Liens
- [engineering-cookbook/kafka-producer-consumer-spring.md](../engineering-cookbook/kafka-producer-consumer-spring.md)
- [ai-engineering/13-Observability/tracer-appels-llm.md](../ai-engineering/13-Observability/tracer-appels-llm.md) — même principe de traçabilité, appliqué ici à Kafka plutôt qu'aux appels LLM.
