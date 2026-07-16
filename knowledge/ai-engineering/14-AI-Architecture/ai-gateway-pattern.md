# AI Gateway — centraliser l'accès aux modèles

## 🎯 Objectif
Éviter que chaque microservice appelle directement un fournisseur de LLM avec sa propre clé API,
sa propre gestion d'erreurs et son propre suivi de coût — centraliser cet accès derrière une
passerelle interne.

## 🧩 Contexte d'usage
Dès qu'un deuxième service dans une architecture microservices (cf. `architecture-library`) a
besoin d'appeler un LLM, la question de la mutualisation se pose : clés API dupliquées, pas de
vision globale du coût, pas de politique de repli uniforme en cas d'indisponibilité du fournisseur.

## 🛠️ Recette
```text
Service A ─┐
Service B ─┼──> AI Gateway ──> Fournisseur LLM (Claude / GPT / modèle interne)
Service C ─┘         │
                      ├── Auth interne (mTLS/JWT service-à-service)
                      ├── Rate limiting par service consommateur
                      ├── Logging/Observabilité centralisés (cf. 13-Observability)
                      ├── Cache de réponses pour les prompts identiques
                      └── Fallback vers un second fournisseur si indisponibilité
```

La passerelle expose une API interne stable (ex. `POST /ai/complete`) découplée du fournisseur
réel — changer de modèle ou ajouter un fournisseur de secours ne touche qu'un seul point, pas
chaque service consommateur.

## ✅ Résultat attendu
Un point unique de visibilité sur le coût et l'usage des LLM dans le système, une politique de
sécurité et de rate limiting appliquée uniformément, et la possibilité de changer de fournisseur
sans redéployer tous les services consommateurs.

## ⚠️ Piège
- **Passerelle qui devient un goulot d'étranglement** : si elle est mal dimensionnée ou synchrone
  sur des appels LLM longs, elle peut ralentir tout le système — prévoir de l'asynchrone
  (webhooks/callbacks) pour les usages non interactifs.
- **Trop de logique métier dans la passerelle** : elle doit rester un point de routage/sécurité/
  observabilité, pas un endroit où s'accumule de la logique spécifique à un domaine métier
  particulier — ça la recouple aux services qu'elle est censée découpler.
