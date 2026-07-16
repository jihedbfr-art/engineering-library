# Mémoire d'agent — court terme vs long terme

## 🎯 Objectif
Distinguer les deux formes de mémoire d'un agent conversationnel et choisir le bon mécanisme de
stockage pour chacune, plutôt que de tout traiter comme un seul historique de conversation.

## 🧩 Contexte d'usage
Un assistant qui doit à la fois suivre le fil de la conversation en cours ET se souvenir de faits
durables sur l'utilisateur ou le domaine d'une session à l'autre (préférences, décisions passées,
contexte projet) a besoin de deux mécanismes distincts, pas d'un seul contexte qui grossit
indéfiniment (cf. `00-Foundations/context-window.md`).

## 🛠️ Recette
- **Mémoire court terme (working memory)** : les derniers tours de la conversation en cours,
  envoyés tels quels ou résumés au-delà d'un certain nombre d'échanges. Vit dans le contexte de la
  requête, disparaît à la fin de la session si non persistée.
- **Mémoire long terme (persisted memory)** : des faits extraits et stockés explicitement
  (base relationnelle classique ou vector store pour la recherche sémantique), relus au début
  d'une nouvelle session plutôt que renvoyés en entier dans chaque prompt.

```text
Nouvelle session utilisateur
        │
        ▼
Charger un résumé des faits long terme pertinents (requête ciblée, pas tout l'historique)
        │
        ▼
Injecter ce résumé + la question courante dans le prompt
        │
        ▼
En fin de session : extraire les faits nouveaux dignes d'être retenus → les persister
```

Exemple concret : un assistant de support qui retient "ce client utilise déjà Kafka en prod, ne pas
lui réexpliquer les bases" plutôt que de rejouer tout l'historique de tickets à chaque nouvelle
conversation.

## ✅ Résultat attendu
Un agent qui paraît se souvenir de l'utilisateur sur la durée sans faire grossir le contexte de
chaque requête individuelle — la mémoire long terme reste un stockage externe interrogé à la
demande, pas un contexte qui s'accumule.

## ⚠️ Piège
- **Tout persister sans discernement** : stocker chaque phrase échangée comme "mémoire long terme"
  transforme la base de mémoire en bruit — ne retenir que les faits qui changeraient une future
  réponse.
- **Mémoire long terme jamais réévaluée** : un fait retenu qui devient faux (préférence changée,
  décision annulée) et jamais mis à jour fait dériver l'agent vers des réponses incorrectes en toute
  confiance.
