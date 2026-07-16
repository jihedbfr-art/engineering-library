# Revue de code assistée par IA — bonnes pratiques

## 🎯 Objectif
Utiliser un LLM comme premier filtre de revue de code sans lui déléguer la responsabilité finale
de qualité — garder la revue humaine comme dernière étape, pas comme étape supprimée.

## 🧩 Contexte d'usage
Une revue de code assistée par IA en complément (pas en remplacement) d'une revue humaine, sur un
dépôt avec des standards établis (cf. `docs/standards/`, `knowledge/code-review-guide/`).

## 🛠️ Recette
- **Rôle du LLM : détecter les patterns connus rapidement** — incohérences de style, oublis de
  gestion d'erreur évidents, code dupliqué, absence de tests sur un chemin critique. Ce sont des
  vérifications mécaniques qu'un modèle fait bien et rapidement.
- **Rôle du reviewer humain : juger le contexte et l'intention** — est-ce que ce changement a du
  sens dans l'architecture globale, est-ce que le compromis technique choisi est le bon pour ce
  cas précis. C'est le jugement qu'un LLM ne peut pas remplacer de façon fiable.
- **Vérifier chaque signalement avant d'agir** : un LLM peut signaler un faux problème (mauvaise
  compréhension du contexte métier) aussi facilement qu'un vrai — traiter ses retours comme des
  pistes à vérifier, pas comme des verdicts.
- **Ne jamais faire relire le code par le même modèle qui l'a généré** sans intervention humaine
  entre les deux — cf. le biais évoqué dans `10-Evaluation/llm-as-judge.md` sur juge et générateur
  trop proches.

```text
Flux recommandé :
  Code écrit (humain ou agent) → Revue LLM automatique (rapide, mécanique)
        → Findings triés par un reviewer humain (garde le vrai, écarte le faux)
        → Revue humaine finale avant merge (contexte, architecture, intention)
```

## ✅ Résultat attendu
Une revue plus rapide sur les aspects mécaniques (libérant du temps humain pour le jugement
architectural), sans jamais faire dépendre la qualité finale du code d'un seul filtre automatisé.

## ⚠️ Piège
- **Merger sur la seule approbation d'un LLM** sans revue humaine — perd la vérification du
  contexte métier et de l'intention, qui reste le point fort irremplaçable d'un reviewer humain.
- **Ignorer systématiquement les signalements IA par lassitude** ("il se trompe souvent") sans les
  trier au cas par cas — un signalement sur dix vrais parmi du bruit reste utile si le tri est fait
  sérieusement.
