# Les limites des benchmarks publics pour un choix de modèle métier

## 🎯 Objectif
Comprendre pourquoi un classement public de modèles (leaderboard généraliste) ne suffit pas à
choisir un modèle pour une tâche métier précise, et ce qu'il faut mesurer à la place.

## 🧩 Contexte d'usage
Une décision de choix de modèle (cf. `01-LLMs/choisir-un-modele.md`) où l'équipe est tentée de
trancher simplement en regardant le modèle en tête d'un classement public.

## 🛠️ Recette
- **Les benchmarks publics mesurent des capacités générales** (raisonnement, maths, code générique)
  qui ne correspondent pas forcément à la tâche réelle (classification d'un ticket support en
  français, extraction d'un champ précis dans un document métier).
- **Risque de contamination** : certains modèles ont pu être exposés, directement ou
  indirectement, aux données mêmes du benchmark pendant l'entraînement, gonflant artificiellement
  leur score sans refléter une vraie généralisation.
- **Construire son propre mini-benchmark** : rassembler 20-50 exemples réels et représentatifs de
  la tâche visée (avec la réponse attendue), et comparer 2-3 modèles candidats dessus avec la même
  méthodologie que le LLM-as-judge (cf. `10-Evaluation/llm-as-judge.md`).

```text
Mauvaise méthode : "Le modèle A est premier sur le leaderboard généraliste, on le prend."
Bonne méthode :
  1. Rassembler 30 tickets support réels avec leur bonne classification
  2. Faire tourner modèle A et modèle B dessus
  3. Comparer précision, coût, latence sur CES 30 cas précis
  4. Trancher sur ces chiffres, pas sur le classement public
```

## ✅ Résultat attendu
Un choix de modèle basé sur une mesure directement pertinente pour le cas d'usage réel, avec un
mini-benchmark réutilisable pour réévaluer le choix quand un nouveau modèle sort.

## ⚠️ Piège
- **Confondre score élevé sur un benchmark et adéquation à la tâche** : un modèle excellent en
  mathématiques n'est pas nécessairement le meilleur pour classifier des emails en français avec un
  vocabulaire métier spécifique.
- **Ne construire aucun mini-benchmark interne** : sans mesure propre, chaque changement de modèle
  se décide "à l'impression", ce qui rend impossible de justifier objectivement une régression ou
  une amélioration constatée en production.
