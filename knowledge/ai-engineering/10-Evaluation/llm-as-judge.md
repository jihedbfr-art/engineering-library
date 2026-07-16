# Évaluation — LLM-as-judge

## 🎯 Objectif
Évaluer automatiquement la qualité d'une sortie de LLM (résumé, réponse RAG, code généré) à l'échelle,
quand une métrique exacte (comme un test unitaire) n'existe pas.

## 🧩 Contexte d'usage
Utile pour comparer deux prompts, deux modèles, ou détecter une régression après un changement de
prompt système — typiquement en CI avant de déployer un changement sur un assistant en prod (ex.
le tool calling de `15-Spring-AI`).

## 🛠️ Recette
1. Définir une **rubrique** explicite (critères notés séparément : exactitude factuelle,
   pertinence, concision, ton) plutôt qu'un score global vague.
2. Faire juger la sortie par un modèle différent (ou plus fort) que celui qui l'a produite, avec
   le prompt de jugement séparé du prompt de génération.
3. Toujours fournir la référence/contexte source au juge (pas seulement la sortie seule), sinon le
   juge évalue la forme et pas le fond.

```text
Prompt juge :
"Voici un contexte source : {context}
Voici une réponse à évaluer : {answer}
Note de 1 à 5 chacun des critères suivants, avec une justification courte :
- exactitude factuelle par rapport au contexte
- complétude
- concision
Réponds en JSON structuré : {"exactitude": n, "completude": n, "concision": n, "justification": "..."}"
```

## ✅ Résultat attendu
Un score reproductible et décomposé par critère, exploitable pour comparer deux versions d'un
prompt/modèle dans le temps (régression testing), pas juste un avis flou du type « ça a l'air bien ».

## ⚠️ Piège
- **Biais de préférence de style** : un juge LLM tend à préférer les réponses plus longues ou plus
  formatées, indépendamment de leur exactitude — neutraliser en normalisant longueur/format avant
  jugement, ou en le précisant explicitement dans le prompt du juge.
- **Juge et généreur trop proches** : utiliser le même modèle pour générer et juger amplifie les
  biais communs — préférer un modèle différent ou une règle déterministe quand c'est possible.
- **Sur-confiance dans le score** : un LLM-as-judge reste un proxy, pas une vérité absolue — le
  croiser avec un échantillon d'évaluation humaine périodique.
