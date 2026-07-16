# Observabilité — tracer les appels LLM comme le reste du système

## 🎯 Objectif
Traiter un appel à un LLM avec le même niveau d'observabilité qu'un appel à une base de données ou
une API externe — trace, latence, coût — au lieu de le laisser comme une boîte noire dans les logs.

## 🧩 Contexte d'usage
Dès qu'un appel LLM entre en production, les questions classiques d'exploitation se posent : quelle
latence P95, quel coût par jour, quel taux d'échec, quel prompt a produit telle sortie en cas de
bug rapporté par un utilisateur.

## 🛠️ Recette
Chaque appel LLM doit produire une trace structurée comparable à celle d'un appel HTTP sortant
classique (cf. l'intégration APM déjà en place pour le reste de l'architecture) :

```text
Span "llm.call"
├── model: "modele-utilise"
├── prompt_tokens / completion_tokens / total_tokens
├── latency_ms
├── cost_estimate (calculé à partir du tarif du modèle)
├── success / failure (+ raison si échec : timeout, rate limit, validation guardrail)
└── trace_id corrélé à la requête utilisateur d'origine (cf. tracing distribué classique)
```

Avec Spring AI, les `Advisor` (cf. `15-Spring-AI`) permettent d'intercepter chaque appel pour y
injecter ce logging sans dupliquer le code dans chaque point d'appel.

## ✅ Résultat attendu
Un dashboard capable de répondre à "combien coûte l'IA ce mois-ci", "quelle est la latence P95 de
l'assistant" et "quel prompt a produit cette réponse incorrecte signalée par un client" — les trois
questions qu'un incident ou une revue budgétaire posera immanquablement.

## ⚠️ Piège
- **Logger le contenu complet des prompts/réponses sans filtrage** : si les données utilisateur
  transitent par le prompt, les logs bruts peuvent devenir un point de fuite de données
  personnelles — appliquer la même politique de rétention/masquage que sur les autres logs
  sensibles.
- **Pas de corrélation avec la requête d'origine** : un appel LLM tracé isolément, sans lien avec le
  `trace_id` de la requête utilisateur globale, rend le diagnostic d'incident beaucoup plus lent.
