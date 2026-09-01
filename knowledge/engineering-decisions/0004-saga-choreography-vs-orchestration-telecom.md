# 0004 — Saga Pattern : Chorégraphie vs Orchestration dans le Provisioning Telecom

- **Statut** : Accepté
- **Date** : 2026-09-02
- **Contexte projet** : `bpmn-provisioning-patterns` (Activation de lignes 5G / Portabilité MNP)

## Contexte
L'activation d'un abonnement mobile complet implique plusieurs microservices distribués :
1. `BillingService` (Créer le compte facturation)
2. `CrmService` (Mettre à jour le profil client)
3. `CoreNetworkService` (Provisionner le HLR/HSS 5G)
4. `MnpService` (Portabilité du numéro, si applicable)

Si l'étape 3 échoue (ex: erreur réseau Huawei/Nokia), nous devons annuler l'étape 1 et 2 (rollback distribué). Le pattern **Saga** est obligatoire. Mais doit-on utiliser une approche par **Chorégraphie** (par événements) ou par **Orchestration** (commandes centralisées) ?

## Options envisagées
| Option | Avantages | Inconvénients |
|---|---|---|
| **Chorégraphie (Kafka Events)** | Couplage faible. Aucun point de défaillance unique. Idéal pour des flux simples (2 à 3 étapes). | L'effet "Pinball" (boule de flipper) : impossible de savoir visuellement où en est une transaction complexe. Les boucles et les timeouts sont un cauchemar à gérer. |
| **Orchestration (Camunda/BPMN)** | Visibilité totale (dashboard). Gestion native des timeouts, des retries et des transactions de compensation (Undo). | Ajoute un couplage centralisé : l'orchestrateur doit connaître les endpoints/topics des participants. Légère latence supplémentaire. |

## Décision
Nous avons choisi l'**Orchestration via Camunda (BPMN)**.

**Pourquoi ?**
Les flux de provisionnement Telecom ou BSS sont **trop complexes et trop critiques** pour la chorégraphie. L'activation d'une SIM inclut des SLAs stricts (ex: Timeout après 30 secondes), des boucles d'attente asynchrones (attendre que l'opérateur donneur valide la portabilité), et des règles de rollback très spécifiques. 
En chorégraphie, surveiller un timeout global sur 5 microservices nécessite des hacks (Timers Kafka, bases de données intermédiaires). 
Camunda permet de dessiner le flux visuellement (BPMN) : l'état de chaque activation client est persisté. Si une erreur survient au bout de la chaîne, l'orchestrateur déclenche automatiquement les événements de *Compensation* (remboursement, annulation) des étapes précédentes.

## Conséquences
- ✅ **Positives** : Le support technique de niveau 2 peut voir exactement où une activation est bloquée via l'interface Camunda Cockpit. Le code métier des microservices (`Billing`, `CoreNetwork`) est purgé de la logique de rollback complexe.
- ⚠️ **Négatives / dette acceptée** : Camunda devient une brique critique. L'implémentation
  actuelle est **Camunda 7.23 embarqué** dans l'application Spring Boot — simple à opérer, mais le
  moteur partage le sort du service qui l'héberge. Passer à un moteur en cluster (Camunda 8 /
  Zeebe) est le chemin de sortie si la disponibilité du provisionnement doit être découplée.
- 🔁 **Ce que ça nous engage à faire ensuite** : Les microservices ne doivent plus s'appeler entre eux de manière synchrone. Ils deviennent de simples "Workers" qui écoutent les commandes de l'orchestrateur (pattern Command/Reply).

## Références
- [Camunda — Microservices Orchestration](https://camunda.com/microservices/)
- [Saga Pattern — Orchestration vs Choreography](https://microservices.io/patterns/data/saga.html)
- Implémentation : [`bpmn-provisioning-patterns`](https://github.com/jihedbfr-art/bpmn-provisioning-patterns)
- Vérification statique des chemins de compensation :
  [`bpmn-saga-linter`](https://github.com/jihedbfr-art/dev-tools-workbench/tree/main/tools/bpmn-saga-linter)
