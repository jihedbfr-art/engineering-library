# Patterns multi-agents

## 🎯 Objectif
Choisir la bonne topologie d'agents (superviseur/workers, pipeline, peer-to-peer) selon la nature
de la tâche, au lieu de tout confier à un agent unique surchargé de contexte.

## 🧩 Contexte d'usage
Une tâche complexe qui mélange plusieurs compétences distinctes — ex. explorer un code, planifier
un changement, l'implémenter, le vérifier — bénéficie de séparer ces rôles en agents dédiés plutôt
qu'un seul prompt géant. Utile pour de l'ingénierie logicielle assistée (revue de code, génération
de tests, migration) sur un stack comme celui de la bibliothèque (`projects/`).

## 🛠️ Recette
Trois topologies courantes :

1. **Superviseur / Workers** : un agent orchestrateur décompose la tâche, distribue des
   sous-tâches à des agents spécialisés (recherche, code, revue), agrège les résultats.
2. **Pipeline séquentiel** : chaque agent transforme la sortie du précédent (ex. `explorer` →
   `planifier` → `implémenter` → `vérifier`), sans retour arrière — simple mais rigide.
3. **Peer-to-peer avec état partagé** : plusieurs agents lisent/écrivent un état commun (fichier,
   base, mémoire partagée) et se coordonnent par ce médium plutôt que par messages directs.

```text
Superviseur
 ├── Agent Recherche  → contexte/état existant
 ├── Agent Planification → plan d'implémentation
 ├── Agent Code       → diff proposé
 └── Agent Revue      → verdict + findings
```

## ✅ Résultat attendu
Chaque agent reste dans son rôle avec un contexte réduit et pertinent (moins d'hallucination,
moins de coût par appel), et le superviseur garde la vue d'ensemble sans surcharger un seul modèle
avec tout l'historique.

## ⚠️ Piège
- **Sur-découpage** : trop d'agents pour une tâche simple multiplie la latence et le coût sans
  gain de qualité — réserver le multi-agent aux tâches réellement composites.
- **État partagé incohérent** : dans le pattern peer-to-peer, deux agents qui écrivent le même
  état sans coordination (pas de verrou logique) peuvent se marcher dessus.
- **Boucle sans convergence** : un pattern révision/critique entre deux agents (générateur ↔
  critique) doit avoir une condition d'arrêt explicite (nombre max d'itérations, seuil de qualité),
  sinon boucle infinie ou coût illimité.
