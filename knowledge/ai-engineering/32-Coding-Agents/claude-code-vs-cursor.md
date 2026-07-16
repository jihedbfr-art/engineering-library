# Agents de code — Claude Code vs Cursor : quand utiliser lequel

## 🎯 Objectif
Choisir entre un agent en ligne de commande (Claude Code) et un IDE augmenté (Cursor) selon la
nature de la tâche, plutôt que d'en adopter un seul par habitude pour tout faire.

## 🧩 Contexte d'usage
Sur un mono-repo comme celui-ci, les deux approches ont leur place à des moments différents du
travail : exploration/refactor large versus édition fine ligne par ligne.

## 🛠️ Recette
- **Agent CLI (Claude Code)** : pertinent pour les tâches qui touchent beaucoup de fichiers ou
  demandent une séquence d'actions autonome (scaffolder un projet, migrer une centaine de fichiers,
  faire une revue de code complète, orchestrer git). Fonctionne bien avec des skills packagées
  (`.claude/skills/`) pour les flux répétés.
- **IDE augmenté (Cursor et équivalents)** : pertinent quand le développeur reste dans la boucle
  ligne par ligne — comprendre un algorithme complexe pas à pas, itérer visuellement sur une UI,
  ou quand chaque changement doit être vu et validé immédiatement dans son contexte visuel.
- **Critère de décision simple** : si la tâche se décrit en une phrase et peut être vérifiée après
  coup (tests, diff relu) → agent CLI. Si la tâche demande un aller-retour visuel constant → IDE.

## ✅ Résultat attendu
Utiliser l'agent CLI pour le gros du travail mécanique répétitif (comme le peuplement de cette
bibliothèque elle-même), et l'IDE pour le travail fin qui demande un jugement visuel continu —
sans opposer les deux comme s'il fallait choisir un camp définitivement.

## ⚠️ Piège
- **Laisser l'agent CLI committer sans relecture** sur des changements larges — la vitesse de
  génération ne dispense jamais de relire le diff avant de committer (cf. `20-Claude`).
- **Sous-utiliser les skills** : répéter le même prompt détaillé à chaque session au lieu de le
  packager une fois en skill fait perdre le principal bénéfice de l'agent CLI sur les tâches
  récurrentes.
