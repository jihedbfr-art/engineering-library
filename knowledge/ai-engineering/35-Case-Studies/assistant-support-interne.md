# Étude de cas — assistant de support interne (RAG + tool calling)

## 🎯 Objectif
Illustrer comment assembler les briques déjà documentées dans ce domaine (RAG, tool calling,
guardrails, observabilité) en une architecture cohérente pour un cas concret : un assistant qui
répond aux questions internes des équipes support sur une plateforme métier.

## 🧩 Contexte d'usage
Une équipe support reçoit des questions répétitives sur le fonctionnement d'une plateforme
(pourquoi ce statut, comment configurer ce paramètre) que la documentation existante couvre, mais
que personne ne prend le temps de chercher sous pression du volume de tickets.

## 🛠️ Recette
Composants assemblés :

```text
Question support (langage naturel)
        │
        ▼
Retrieval : recherche des passages pertinents dans la doc interne (04-RAG + PGVector)
        │
        ▼
Tool calling optionnel : si la question porte sur un cas précis (ex. "pourquoi ce ticket est
bloqué"), appel d'un tool métier qui interroge l'état réel en base (15-Spring-AI)
        │
        ▼
Génération de la réponse, contrainte à ne citer que les passages/données récupérés (04-RAG)
        │
        ▼
Guardrail : validation que la réponse ne contient pas d'action non demandée (11-Guardrails)
        │
        ▼
Trace complète de l'échange (13-Observability) pour audit et amélioration continue
```

- **Démarrage progressif** : commencer par le RAG seul (questions documentaires), n'ajouter le tool
  calling sur des données live qu'une fois le retrieval validé comme fiable — éviter de tout
  construire en même temps.
- **Boucle de rétroaction** : chaque question à laquelle l'assistant ne peut pas répondre
  correctement devient un candidat à ajouter à la documentation source, pas seulement un échec à
  corriger dans le prompt.

## ✅ Résultat attendu
Une réduction du volume de questions répétitives adressées à l'équipe support, avec une traçabilité
complète permettant de mesurer le taux de bonnes réponses (cf. `10-Evaluation`) et d'identifier les
lacunes de documentation réelles.

## ⚠️ Piège
- **Vouloir tout construire d'un coup** (RAG + tools + guardrails + observabilité en même temps)
  plutôt que par étapes validées une à une — chaque brique ajoutée sans validation de la précédente
  multiplie les sources d'erreur possibles.
- **Ne pas fermer la boucle avec la documentation source** : un assistant qui compense
  indéfiniment les lacunes de la doc par du prompt engineering, sans jamais faire remonter ces
  lacunes à corriger à la source, plafonne rapidement en qualité.
