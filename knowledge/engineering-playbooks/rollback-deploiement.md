# Playbook — Rollback de déploiement

> Une nouvelle version déployée en production casse une fonctionnalité critique ou fait chuter un
> indicateur de santé.

## 🚨 Déclencheur
Taux d'erreur HTTP 5xx en hausse anormale après un déploiement, healthcheck qui repasse en rouge,
ou signalement utilisateur immédiat après une mise en production.

## ✅ Pré-requis
- Accès à la plateforme de déploiement (Jenkins/pipeline CI-CD du projet).
- Connaître la version précédente stable (tag/commit) — toujours identifiable via les releases
  SemVer du dépôt concerné.
- Accès aux logs et au dashboard de monitoring pour confirmer l'anomalie avant d'agir.

## 📋 Étapes
1. Confirmer l'anomalie avec une métrique ou un log précis (pas juste une impression) —
   éviter un rollback réactif sur un faux signal.
2. Identifier la dernière version stable connue (tag Git précédent, image Docker précédente).
3. Redéployer cette version stable via le pipeline existant (pas de correctif à chaud en
   production).
4. Si une migration de base de données irréversible a été appliquée par la version défaillante,
   traiter séparément — un rollback de code ne défait pas une migration de schéma déjà jouée.

## 🔎 Vérification
Le taux d'erreur revient à la normale, le healthcheck repasse au vert, et le comportement rapporté
par les utilisateurs est confirmé résolu.

## 📣 Communication
Prévenir l'équipe et les parties prenantes concernées (client, product owner) dès la confirmation
de l'incident, puis à nouveau une fois le rollback effectif et vérifié.

## 📝 Après
Créer une entrée [engineering-failures](../engineering-failures/) documentant la cause racine du
problème introduit par la version défaillante, pour éviter la récurrence.
