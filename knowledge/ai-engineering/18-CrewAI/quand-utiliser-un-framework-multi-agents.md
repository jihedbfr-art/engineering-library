# Quand utiliser un framework multi-agents (CrewAI, AutoGen) plutôt que du code maison

## 🎯 Objectif
Décider si un framework dédié aux équipes d'agents (CrewAI, AutoGen) apporte une vraie valeur par
rapport à une orchestration multi-agents codée directement en Spring (cf.
`05-Agents/multi-agent-patterns.md`).

## 🧩 Contexte d'usage
Une architecture multi-agents (superviseur/workers, cf. `05-Agents`) grandit en complexité et
l'équipe se demande si un framework dédié simplifierait la maintenance.

## 🛠️ Recette
- **Ce que ces frameworks apportent réellement** : des abstractions toutes prêtes pour définir des
  rôles d'agents, orchestrer leur collaboration, et gérer la mémoire partagée — utile quand
  l'orchestration devient complexe (plus de 3-4 agents avec des interactions non triviales).
- **Ce qu'ils coûtent** : une nouvelle dépendance (souvent Python) à opérer à côté d'un stack
  Java/Spring existant, avec sa propre courbe d'apprentissage et son propre cycle de mise à jour.
- **Alternative pragmatique pour ce stack** : pour 2-3 agents avec un flux d'orchestration simple
  (superviseur qui distribue à des workers spécialisés), un enchaînement de `ChatClient` Spring AI
  (cf. `15-Spring-AI`) orchestré par du code Java classique reste plus simple à opérer et à
  déboguer qu'un framework externe, tant que la complexité réelle ne le justifie pas.

## ✅ Résultat attendu
Une décision d'architecture justifiée par la complexité réelle de l'orchestration multi-agents
constatée, pas par l'attrait d'un framework à la mode — avec le même principe de "vérifier avant
d'introduire une dépendance" que pour toute autre brique d'architecture (cf.
`27-Open-Source-Models`, `07-Vector-Databases`).

## ⚠️ Piège
- **Introduire un framework multi-agents pour 2 agents simples** : la complexité ajoutée
  (dépendance, runtime séparé) dépasse largement le bénéfice pour une orchestration qui tiendrait en
  quelques classes Java.
- **Sous-estimer la complexité réelle une fois à 5+ agents interdépendants** : au-delà d'un certain
  nombre d'agents avec des interactions croisées, du code maison non structuré devient difficile à
  maintenir — c'est le seuil où un framework dédié commence à se justifier.
