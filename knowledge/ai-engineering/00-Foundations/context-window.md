# Context Window — comprendre et gérer la fenêtre de contexte

## 🎯 Objectif
Comprendre ce qu'est réellement la fenêtre de contexte d'un LLM, pourquoi elle a un coût direct
en argent et en qualité de réponse, et comment concevoir une application qui ne la sature pas.

## 🧩 Contexte d'usage
Dès qu'une application envoie au modèle plus que la question de l'utilisateur — historique de
conversation, documents récupérés (RAG), résultats d'outils, instructions système — la gestion de
la fenêtre de contexte devient un problème d'ingénierie à part entière, pas un détail.

## 🛠️ Recette
- **La fenêtre de contexte est un budget, pas une capacité illimitée.** Chaque token compté est
  facturé (entrée + sortie) et allonge la latence. Un système en production doit budgétiser :
  `system prompt + historique + contexte récupéré + marge de sortie ≤ limite du modèle`.
- **Dégradation avant la limite dure** : au-delà d'un certain volume, la qualité de rappel
  d'information (« lost in the middle ») chute avant même d'atteindre la limite technique du
  modèle — les infos placées au milieu d'un contexte long sont moins bien exploitées que celles en
  début/fin.
- **Stratégies de gestion** :
  1. Résumer l'historique de conversation au-delà d'un certain nombre de tours plutôt que de tout
     renvoyer verbatim.
  2. Ne récupérer (RAG) que les passages réellement pertinents, pas des documents entiers.
  3. Placer les instructions critiques en début ET en fin de prompt sur les contextes longs.

```text
Budget de contexte typique (exemple) :
  system prompt figé        ~500 tokens
  historique résumé         ~1 000 tokens
  contexte RAG récupéré     ~3 000 tokens
  marge de réponse          ~2 000 tokens
  ------------------------------------
  Total                     ~6 500 tokens sur une fenêtre de 32k → marge confortable
```

## ✅ Résultat attendu
Un système qui reste rapide et précis même après des dizaines de tours de conversation, avec un
coût par requête prévisible plutôt que croissant linéairement avec la durée de la session.

## ⚠️ Piège
- **Tout envoyer « au cas où »** : coller l'intégralité d'un historique ou d'une base de
  connaissances dans le contexte parce que « la fenêtre est grande » dégrade la précision et
  explose le coût — une fenêtre large n'est pas une invitation à l'utiliser en entier.
- **Compression trop agressive** : résumer l'historique trop tôt ou trop fort peut faire perdre un
  détail que l'utilisateur mentionne rappeler plus tard — garder les derniers échanges verbatim et
  ne résumer que le plus ancien.
