# Fine-tuner ou prompter ? La question à se poser avant d'investir

## 🎯 Objectif
Éviter d'investir dans un fine-tuning coûteux quand un prompt bien conçu (few-shot, structured
output) résout déjà le problème.

## 🧩 Contexte d'usage
Une équipe constate qu'un modèle généraliste "ne fait pas exactement ce qu'on veut" sur une tâche
répétitive (classification métier, format de sortie spécifique, ton de réponse) et envisage un
fine-tuning.

## 🛠️ Recette
Ordre de priorité avant d'envisager un fine-tuning (chaque étape est plus rapide et moins coûteuse
que la suivante) :

1. **Améliorer le prompt** : instructions plus précises, exemples few-shot représentatifs,
   structured output (cf. `02-Prompt-Engineering`). Résout la majorité des cas de "sortie pas
   assez précise".
2. **RAG** (cf. `04-RAG`) : si le problème est un manque de connaissance factuelle spécifique au
   domaine, injecter le contexte pertinent au moment de la requête plutôt que d'essayer de
   "graver" cette connaissance dans les poids du modèle.
3. **Fine-tuning léger (LoRA/QLoRA)** : seulement si le prompt + RAG bien conçus ne suffisent
   toujours pas, typiquement pour un style/ton très spécifique et stable, ou un format de sortie
   extrêmement contraint sur un très grand volume de requêtes où chaque token économisé en prompt
   compte.
4. **Fine-tuning complet (SFT sur tout le modèle)** : rarement justifié hors d'un contexte de
   recherche ou d'un besoin d'échelle massive avec équipe ML dédiée.

## ✅ Résultat attendu
La majorité des besoins métier réels se résolvent aux étapes 1-2, sans jamais nécessiter
d'entraînement de modèle — un fine-tuning se justifie seulement quand ces étapes moins coûteuses
ont été essayées et documentées comme insuffisantes.

## ⚠️ Piège
- **Fine-tuner pour injecter de la connaissance factuelle** : un fine-tuning n'est pas un bon outil
  pour "apprendre des faits" au modèle (il les oublie/déforme facilement) — c'est le rôle du RAG,
  pas du fine-tuning.
- **Sous-estimer le coût de maintenance** : un modèle fine-tuné doit être réentraîné à chaque
  évolution significative du besoin ou du modèle de base — un prompt, lui, se modifie en une
  minute sans réentraînement.
