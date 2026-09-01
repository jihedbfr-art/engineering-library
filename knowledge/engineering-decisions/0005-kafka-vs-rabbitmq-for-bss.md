# 0005 — Streaming Data : Apache Kafka vs RabbitMQ (Message Queue)

- **Statut** : Accepté
- **Date** : 2026-09-02
- **Contexte projet** : Bus de médiation asynchrone (BSS / OSS)

## Contexte
L'architecture microservices nécessite de router des millions d'événements de facturation (Call Detail Records - CDR) depuis le réseau vers le moteur de rating. Dans le passé, les architectures SOA utilisaient des bus de messages traditionnels (JMS, RabbitMQ). Avec la volumétrie 5G, nous devons décider du standard de communication asynchrone pour la nouvelle plateforme BSS.

## Options envisagées
| Option | Avantages | Inconvénients |
|---|---|---|
| **RabbitMQ (AMQP)** | Excellent routage (Exchanges/Bindings). Accusé de réception (ACK) natif par message. Interface de gestion simple. | Modèle "Smart Broker / Dumb Consumer". Les messages sont supprimés après lecture. Pas de rejeu historique possible. Plafonne à quelques dizaines de milliers de msgs/sec. |
| **Apache Kafka** | Débit massif (millions de msgs/sec). Les événements sont persistés (Log append-only). Permet de "rejouer" l'histoire. | Modèle "Dumb Broker / Smart Consumer". Routage basique. L'équilibrage dépend du nombre de partitions. Infrastructure lourde (ZooKeeper/KRaft). |

## Décision
Nous avons standardisé **Apache Kafka** comme épine dorsale de l'architecture.

**Pourquoi ?**
Le facteur décisif est **le rejeu des événements (Event Replay)**, pas seulement le débit. 
En Telecom (Billing), si une nouvelle règle de taxe est déployée en production et qu'elle contient un bug, nous aurons facturé de travers pendant 2 heures. Avec RabbitMQ, les messages sont consommés et supprimés de la file : c'est perdu.
Avec Kafka, le log est persistant (ex: rétention de 7 jours). En cas de bug, il suffit de déployer le correctif, de "rembobiner" l'offset du Consumer Group de 2 heures en arrière, et de relire les événements de facturation (CDRs) pour les corriger. C'est l'Event Sourcing, et c'est un filet de sécurité absolu en production.

## Conséquences
- ✅ **Positives** : Le débit réseau n'est plus un goulot d'étranglement. Plusieurs microservices différents peuvent lire le même topic à leur propre rythme sans se gêner (Contrairement au pub/sub RabbitMQ qui duplique la donnée).
- ⚠️ **Négatives / dette acceptée** : La gestion des erreurs (Dead Letter Queues - DLQ) est beaucoup plus complexe dans Kafka que dans RabbitMQ. Le tri et le routage complexe de messages sont limités.
- 🔁 **Ce que ça nous engage à faire ensuite** : Construire des mécanismes stricts de DLQ (Non-blocking retry topics) via Spring Kafka pour isoler les messages empoisonnés sans bloquer la partition entière.

## Références
- [Kafka vs RabbitMQ — Confluent](https://www.confluent.io/kafka-vs-rabbitmq/)
- [ADR 0003 — Transactional Outbox](0003-transactional-outbox-vs-dual-writes.md), qui dépend de
  cette décision
