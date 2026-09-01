# 0003 — Pourquoi le Transactional Outbox Pattern plutôt que le Dual Write ?

- **Statut** : Accepté
- **Date** : 2026-09-02
- **Contexte projet** : `bpmn-provisioning-patterns` (activation de ligne, portabilité MNP)

## Contexte
Dans les environnements BSS (Business Support Systems), lorsqu'un client active un forfait 5G via l'API, nous devons faire deux choses simultanément :
1. Mettre à jour l'état de l'abonnement dans la base de données relationnelle (PostgreSQL).
2. Publier un événement `SubscriptionActivated` sur Apache Kafka pour que le composant de provisionnement réseau (HLR/HSS) active réellement la ligne.

Le motif naturel des développeurs est le **Dual Write** (écrire dans la BD, puis envoyer à Kafka dans la même méthode). Cependant, Kafka ne participe pas aux transactions JTA/XA de la base de données. Si le serveur plante juste après le `commit()` de la base mais juste avant le `send()` Kafka, la base est à jour, mais le réseau ne sera jamais provisionné (le client paie, mais n'a pas internet). Inversement, si Kafka reçoit le message mais que le `commit()` de la BD échoue (Timeout), le réseau est activé mais le client n'est pas facturé.

## Options envisagées
| Option | Avantages | Inconvénients |
|---|---|---|
| **Dual Write (Try/Catch)** | Très simple à implémenter, aucun composant externe requis. | Aucune garantie de consistance (Two-Generals Problem). Inacceptable en Telecom. |
| **2PC / XA Transactions** | Garantie forte (ACID) entre la BD et le broker de messages. | Kafka ne supporte pas XA. Chute drastique des performances, blocages des verrous. |
| **Transactional Outbox** | Consistance éventuelle garantie (At-Least-Once delivery). Pas de verrouillage distribué. | Nécessite un composant de Polling ou de CDC (Debezium) pour lire la table outbox. Complexité d'infrastructure. |

## Décision
Nous avons choisi le **Transactional Outbox Pattern**, avec un **relais par polling** applicatif
(`OutboxRelay`, `@Scheduled`) plutôt qu'un CDC Debezium.

**Pourquoi l'outbox ?**
L'intégrité financière et le provisionnement réseau sont non-négociables. L'outbox consiste à
enregistrer l'événement métier (`SubscriptionActivated`) dans une table `portability_outbox` au sein
de la **même transaction locale** que la mise à jour de l'abonnement. Puisque c'est une transaction
PostgreSQL locale (ACID), soit les deux réussissent, soit les deux échouent.

**Pourquoi le polling plutôt que Debezium ?**
Le CDC est techniquement supérieur — il ne charge pas la base de requêtes de scrutation et capte
l'événement au niveau du WAL. Il coûte en revanche un cluster Kafka Connect à déployer, surveiller
et mettre à jour. Pour le volume de ce projet, un relais qui interroge la table toutes les secondes
et marque `published_at` suffit, tient dans le même artefact Spring Boot, et se teste sans
infrastructure supplémentaire. Debezium reste le chemin de sortie si le polling devient le goulot
d'étranglement — la table outbox est la même dans les deux cas, seul le lecteur change.

## Conséquences
- ✅ **Positives** : Éradication totale des états fantômes (ligne activée sans facturation, ou facturation sans ligne). Les performances d'écriture de l'API sont excellentes car nous n'attendons pas l'acquittement réseau de Kafka.
- ⚠️ **Négatives / dette acceptée** : le système passe d'une consistance forte à une **consistance
  éventuelle** — l'événement part après le commit, pas pendant. Le relais par polling ajoute une
  requête périodique sur la table, et une latence bornée par son intervalle (1 s ici) : c'est le
  prix payé pour ne pas opérer de Kafka Connect.
- 🔁 **Ce que ça nous engage à faire ensuite** : puisque la livraison est *At-Least-Once*, les
  consommateurs **doivent** être idempotents. C'est fait : table `processed_events` avec clé
  primaire sur l'`eventId`, l'insert laissant la base rejeter la seconde livraison
  (`ProcessedEventRepository`). Le pattern est générable via
  [`kafka-idempotent-consumer-scaffolder`](https://github.com/jihedbfr-art/dev-tools-workbench/tree/main/tools/kafka-idempotent-consumer-scaffolder).

## Références
- [Microservices.io — Transactional Outbox](https://microservices.io/patterns/data/transactional-outbox.html)
- Implémentation : [`bpmn-provisioning-patterns`](https://github.com/jihedbfr-art/bpmn-provisioning-patterns)
  (`OutboxRelay`, `V1__outbox_and_processed_events.sql`)
- Diagnostic d'une outbox en production : [`kafka-outbox-verifier`](https://github.com/jihedbfr-art/dev-tools-workbench/tree/main/tools/kafka-outbox-verifier)
