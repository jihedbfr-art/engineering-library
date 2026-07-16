# Gemini — l'intérêt du multimodal natif pour un cas d'usage précis

## 🎯 Objectif
Identifier quand le multimodal natif de Gemini (texte+image+audio dans un seul appel) apporte un
avantage réel par rapport à une architecture qui traiterait chaque modalité séparément.

## 🧩 Contexte d'usage
Un cas d'usage où l'entrée combine plusieurs modalités en même temps et où leur interprétation
conjointe compte (cf. `28-Multimodal/quand-le-multimodal-est-necessaire.md` pour la question
préalable : est-ce que le multimodal est même nécessaire ici).

## 🛠️ Recette
- **Avantage réel du multimodal natif** : analyser une capture d'écran d'erreur accompagnée d'une
  description texte en un seul appel, sans étape d'OCR séparée puis fusion manuelle des deux
  résultats — utile en support technique (cf. `35-Case-Studies`) quand les utilisateurs joignent
  souvent une capture à leur description.
- **Intégration via Spring AI** : comme pour les autres fournisseurs (cf.
  `21-OpenAI/function-calling-vs-tool-calling-spring.md`), passer par l'abstraction `ChatClient`
  plutôt que par le SDK natif garde la possibilité de changer de fournisseur multimodal sans
  réécrire le code métier.

## ✅ Résultat attendu
Un traitement multimodal utilisé seulement là où l'entrée réelle des utilisateurs le justifie
(pièces jointes visuelles + texte), avec le gain mesurable d'éviter une étape de fusion manuelle
entre modalités traitées séparément.

## ⚠️ Piège
- **Ajouter le traitement d'image "parce que Gemini le permet nativement"** sans que le cas d'usage
  réel en ait besoin — retombe sur le même piège que `28-Multimodal`.
- **Coder directement contre le SDK Gemini** pour cette seule fonctionnalité, créant une dépendance
  fournisseur isolée du reste de l'intégration LLM déjà abstraite via Spring AI.
