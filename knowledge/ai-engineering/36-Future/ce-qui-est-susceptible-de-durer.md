# Ce qui est susceptible de durer 10 ans dans ce domaine

## 🎯 Objectif
Distinguer, dans tout ce domaine `ai-engineering`, ce qui relève d'un principe durable et ce qui
relève d'un détail d'implémentation qui aura changé dans deux ans — pour savoir où investir son
temps d'apprentissage en priorité.

## 🧩 Contexte d'usage
Utile en relisant l'ensemble du domaine : certains contenus (noms de modèles précis, versions de
SDK) deviendront obsolètes vite ; d'autres (principes d'architecture, de sécurité, d'évaluation)
resteront valables bien après que les modèles cités aient changé.

## 🛠️ Recette
Ce qui a de bonnes chances de durer :
- **Les principes d'architecture** (`14-AI-Architecture`, `06-MCP` conceptuellement) : séparer
  données et instructions, moindre privilège, observabilité — ces principes viennent de
  l'ingénierie logicielle classique, pas de la mode IA du moment.
- **La méthode d'évaluation** (`10-Evaluation`, `33-Benchmarks`) : mesurer sur son propre cas plutôt
  que se fier à un classement générique restera vrai quel que soit le modèle en tête demain.
- **Les guardrails et la sécurité** (`11-Guardrails`, `12-Security`) : le prompt injection et le
  besoin de validation de sortie ne disparaîtront pas avec un modèle plus intelligent — ils
  changent de forme, pas de nature.

Ce qui a de bonnes chances de changer vite :
- Les noms et classements de modèles précis (`01-LLMs`, `33-Benchmarks`).
- Les SDK et versions d'intégration précises (`15-Spring-AI`, `21-OpenAI`).
- Les limites de contexte, coûts et capacités chiffrées (`00-Foundations/context-window.md`) —
  vraies à la date d'écriture, à revérifier avant de s'y fier.

## ✅ Résultat attendu
Une lecture de ce domaine qui sépare le "pourquoi" (durable) du "comment aujourd'hui" (à
revérifier), pour continuer à en tirer de la valeur même après que plusieurs générations de modèles
soient sorties depuis l'écriture de ces fiches.

## ⚠️ Piège
- **Traiter les chiffres précis comme des vérités permanentes** : un contexte de "32k tokens" ou un
  coût par million de tokens cité aujourd'hui sera faux dans un an — utile pour comprendre l'ordre
  de grandeur du problème, pas comme référence figée.
- **Négliger les principes parce qu'ils semblent "évidents"** : les principes de sécurité et
  d'architecture ci-dessus semblent simples mais sont justement ceux que les incidents réels
  violent le plus souvent en pratique.
