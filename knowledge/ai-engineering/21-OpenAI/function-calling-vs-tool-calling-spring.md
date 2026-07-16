# OpenAI Function Calling — équivalence avec le tool calling Spring AI

## 🎯 Objectif
Comprendre que le "function calling" OpenAI et le "tool calling" Spring AI (cf. `15-Spring-AI`)
sont la même idée avec un vocabulaire différent, pour ne pas réapprendre le concept à chaque
changement de fournisseur.

## 🧩 Contexte d'usage
Une équipe qui a lu de la documentation OpenAI (function calling, Assistants API) et qui se demande
comment ça se transpose sur le stack Spring AI déjà utilisé dans ce dépôt.

## 🛠️ Recette
- **Le concept est identique** : décrire une fonction avec un nom, une description et un schéma de
  paramètres JSON ; le modèle décide quand l'appeler et avec quels arguments ; le résultat est
  renvoyé au modèle qui compose la réponse finale.
- **La portabilité est le vrai avantage de passer par Spring AI plutôt que le SDK OpenAI brut** :
  le même `@Tool` Java fonctionne avec Claude, GPT ou Gemini selon le provider configuré (cf.
  `01-LLMs/choisir-un-modele.md`) — écrire directement contre le SDK d'un seul fournisseur couple
  le code applicatif à ce fournisseur.
- **Différences pratiques à connaître** : les fournisseurs varient sur le nombre de tools qu'ils
  gèrent bien simultanément, et sur la fiabilité du respect strict du schéma — à valider par domaine
  (cf. `10-Evaluation`) plutôt que de supposer une parité totale.

## ✅ Résultat attendu
Une compréhension transférable d'un fournisseur à l'autre : le concept de tool calling ne change
pas, seule l'implémentation SDK change — et Spring AI abstrait cette différence pour le code métier.

## ⚠️ Piège
- **Coder directement contre un SDK propriétaire** (OpenAI, Anthropic) dans le code métier plutôt
  que contre l'abstraction Spring AI — rend coûteux tout changement de fournisseur plus tard.
- **Supposer une parité totale entre fournisseurs** sur le nombre de tools ou la fiabilité du
  schéma sans le vérifier sur le cas d'usage réel.
