# vLLM — servir un modèle open-source en interne à l'échelle

## 🎯 Objectif
Comprendre pourquoi un moteur de serving dédié comme vLLM est nécessaire dès qu'un modèle
open-source (cf. `27-Open-Source-Models`) doit servir plusieurs utilisateurs simultanément, plutôt
que de l'exposer via une inférence naïve.

## 🧩 Contexte d'usage
Une décision de self-hosting a été prise (contrainte de résidence des données, coût à l'échelle) et
le modèle doit maintenant servir un trafic réel, pas juste un test local (cf.
`25-Ollama/ollama-dev-local.md`, adapté au dev, pas à la production à l'échelle).

## 🛠️ Recette
- **Le problème que vLLM résout** : sans moteur de serving optimisé, chaque requête bloque le GPU
  jusqu'à sa fin de génération — avec plusieurs utilisateurs simultanés, la latence explose. vLLM
  implémente du *continuous batching* (traiter plusieurs requêtes en parallèle sur le même GPU en
  intercalant leurs étapes de génération) et une gestion de mémoire optimisée (PagedAttention) pour
  maximiser le débit.
- **Dimensionnement** : le choix de la taille de modèle et du GPU se fait en fonction du débit
  cible (requêtes/seconde) et de la latence acceptable, pas seulement de la capacité brute du
  modèle — un modèle plus petit bien servi peut battre un modèle plus grand mal dimensionné en
  production.
- **Interface standard** : vLLM expose une API compatible OpenAI, ce qui permet de la brancher
  derrière un `AI Gateway` (cf. `14-AI-Architecture`) sans changer le code appelant.

## ✅ Résultat attendu
Un service de modèle self-hosté qui tient une charge réelle avec plusieurs utilisateurs
concurrents, avec un débit et une latence mesurés et dimensionnés à l'avance plutôt que découverts
en incident de production.

## ⚠️ Piège
- **Sous-dimensionner le GPU par rapport au trafic réel** : un test avec un seul utilisateur ne
  révèle pas les limites de débit sous charge concurrente — tester avec un profil de charge
  représentatif avant la mise en production.
- **Négliger la gestion des mises à jour de modèle** : contrairement à une API gérée, chaque mise à
  jour de version de modèle self-hosté doit être testée et déployée par l'équipe elle-même.
