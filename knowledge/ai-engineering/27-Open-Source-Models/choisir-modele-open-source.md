# Choisir un modèle open-source vs une API propriétaire

## 🎯 Objectif
Décider entre s'appuyer sur une API de modèle propriétaire (Claude, GPT, Gemini) et self-hoster un
modèle open-weight (Llama, Mistral, Qwen via vLLM/Ollama), selon des critères concrets plutôt
qu'une préférence idéologique pour l'open-source.

## 🧩 Contexte d'usage
Une décision d'architecture qui se pose particulièrement dans des secteurs à contraintes fortes de
résidence des données (télécom, banque, santé) où les données ne peuvent pas toujours sortir du
périmètre de l'entreprise.

## 🛠️ Recette
Grille de décision :

| Critère | API propriétaire | Modèle open-source self-hosté |
|---|---|---|
| Contrainte de résidence des données | Problématique si le fournisseur est hors périmètre autorisé | Résolue — les données restent sur l'infrastructure interne |
| Qualité de raisonnement à l'état de l'art | Généralement en tête | Souvent un cran derrière sur les tâches complexes |
| Coût à faible volume | Prévisible, pas d'investissement infra | Coût d'infrastructure GPU fixe même à faible usage |
| Coût à très gros volume | Peut devenir élevé | Peut devenir plus rentable une fois l'infra amortie |
| Effort opérationnel | Aucun (API gérée) | Nécessite de gérer le serving (cf. `26-vLLM`), les mises à jour, la scalabilité |

En pratique, dans un contexte comme un opérateur télécom où certaines données (identité,
provisioning) sont sensibles, le choix se fait souvent au cas par cas : API propriétaire pour les
tâches génériques sans donnée sensible, modèle self-hosté pour les cas où la donnée ne peut
techniquement/contractuellement pas sortir.

## ✅ Résultat attendu
Une architecture hybride assumée (cf. `14-AI-Architecture/ai-gateway-pattern.md` pour router entre
plusieurs fournisseurs) plutôt qu'un choix unique appliqué uniformément à tous les cas d'usage,
alors que les contraintes réelles diffèrent selon la sensibilité de la donnée traitée.

## ⚠️ Piège
- **Sous-estimer le coût opérationnel du self-hosting** : faire tourner un modèle open-source en
  production implique de gérer GPU, scalabilité, mises à jour de sécurité — un coût caché souvent
  absent du calcul initial "l'open-source est gratuit".
- **Ne jamais réévaluer le choix** : l'écart de qualité entre modèles propriétaires et open-source
  se réduit vite — une décision figée il y a un an mérite d'être revisitée (cf.
  `01-LLMs/choisir-un-modele.md`).
