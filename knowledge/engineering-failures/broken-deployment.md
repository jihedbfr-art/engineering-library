# Broken deployment (migration de schéma incompatible avec le rolling deploy)

> Le déploiement passe tous les tests CI, l'image se déploie sans erreur, et pourtant la production renvoie des erreurs 500 en masse pendant plusieurs minutes — parce qu'une migration de base de données a changé un schéma sur lequel l'ancienne version du code, encore en train de tourner sur d'autres pods pendant le rolling deploy, dépend toujours.

## 🔍 Cause

Un rolling deployment (Kubernetes par défaut) remplace les pods progressivement, pas tous à la fois — pendant la fenêtre de transition, l'ancienne et la nouvelle version de l'application tournent **simultanément**, servant du trafic en parallèle. Si une migration de base de données appliquée avec la nouvelle version supprime une colonne, renomme une table, ou change un type de colonne dont l'ancienne version du code dépend encore, les pods qui tournent encore avec l'ancien code se mettent à échouer dès que la migration s'applique — alors même que le code de ces pods n'a pas changé. Le piège classique : une migration testée en isolation (base vidée, une seule version du code) passe parfaitement en CI, parce que le test ne simule jamais la coexistence de deux versions du code sur le même schéma.

## 🚨 Symptômes

- Pic d'erreurs 500 corrélé précisément avec l'exécution de la migration (Flyway/Liquibase), pas avec le déploiement de l'image en lui-même — l'écart temporel entre les deux donne le vrai indice.
- Seule une partie du trafic échoue pendant la fenêtre de rolling deploy (les requêtes qui tombent sur les pods pas encore mis à jour), donnant un taux d'erreur partiel et fluctuant plutôt qu'une panne totale nette — souvent pris à tort pour un problème de charge ou d'instabilité réseau au premier coup d'œil.
- Erreurs applicatives typiques : `column "X" does not exist`, `relation "Y" does not exist`, ou une désérialisation JPA qui échoue parce que le mapping d'entité ne correspond plus au schéma réel.

## 🩺 Comment diagnostiquer

```bash
# 1. Confirmer la fenêtre exacte du rolling deploy et la comparer à l'heure des erreurs
kubectl rollout history deployment/mon-service
kubectl get events --field-selector involvedObject.name=mon-service --sort-by='.lastTimestamp'

# 2. Vérifier l'historique d'exécution de la migration
```
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_on DESC LIMIT 5;
```
```bash
# 3. Corréler précisément : les erreurs 500 ont-elles commencé AVANT ou APRÈS
# l'exécution effective de la migration, pas juste avant/après le déploiement
# de l'image (les deux événements ne sont pas simultanés dans un rolling deploy)
```

## ✅ Solution

- **Migrations en deux temps (expand/contract)** : jamais supprimer ou renommer une colonne dans la même migration qui l'utilise encore côté nouveau code. D'abord une migration qui **ajoute** la nouvelle colonne/table en gardant l'ancienne fonctionnelle (le code ancien ET nouveau peuvent coexister), déployer, laisser le rolling deploy se terminer complètement, puis une migration séparée ultérieure qui retire l'ancienne colonne une fois qu'aucun pod ne l'utilise plus.
```sql
-- Migration 1 (déployée avec le nouveau code qui sait lire les deux colonnes) :
ALTER TABLE commande ADD COLUMN statut_v2 VARCHAR(20);
-- ... code applicatif écrit dans les deux colonnes en parallèle le temps de la transition ...

-- Migration 2 (seulement après confirmation que 100% des pods tournent la nouvelle version) :
ALTER TABLE commande DROP COLUMN statut;
```
- **Rollback immédiat du déploiement** (pas de la migration seule) si le pic d'erreurs est détecté pendant la fenêtre de rollout — `kubectl rollout undo` ramène l'ancienne version du code, cohérente avec l'état pré-migration si la migration n'a pas encore cassé la compatibilité descendante.
- **Health check et readiness probe stricts** qui empêchent Kubernetes de router du trafic vers un pod dont la version ne correspond pas au schéma courant, réduisant la fenêtre d'exposition même si l'incompatibilité existe brièvement.

## 🛡️ Prévention

- Règle systématique : toute migration qui modifie une colonne/table existante doit être compatible avec **l'ancienne ET la nouvelle version du code** au moment où elle s'exécute — c'est la seule façon de rendre un rolling deploy réellement sûr, pas une option à appliquer seulement sur les migrations "qui semblent risquées".
- Tester le scénario de coexistence en CI/pré-production, pas seulement la migration en isolation : déployer la migration, exécuter la suite de tests de l'**ancienne** version du code contre le nouveau schéma, confirmer qu'elle passe encore avant de considérer la migration sûre pour un rolling deploy.
- Stratégie de déploiement blue-green ou canary pour les changements de schéma les plus sensibles, quand le risque de la fenêtre de coexistence est jugé trop élevé même avec l'approche expand/contract.

## 🔗 Liens
- [architecture-library/multi-tenant.md](../architecture-library/multi-tenant.md) et [saga-pattern.md](../architecture-library/saga-pattern.md) — même discipline de compatibilité progressive appliquée à d'autres types de changement d'état partagé
- [debugging-recipes/spring-boot-ne-demarre-pas.md](../debugging-recipes/spring-boot-ne-demarre-pas.md) — cas voisin quand c'est le démarrage lui-même, pas le trafic en cours, qui échoue sur un schéma incompatible
