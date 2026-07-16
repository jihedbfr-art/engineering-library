# Ollama — développer et tester sans dépendre d'une API payante

## 🎯 Objectif
Utiliser un modèle local via Ollama pour le développement et les tests d'une fonctionnalité IA,
sans consommer de crédits API ni dépendre d'une connexion réseau externe à chaque exécution.

## 🧩 Contexte d'usage
Développement d'une fonctionnalité RAG ou tool calling (cf. `04-RAG`, `15-Spring-AI`) où chaque
exécution de test contre une vraie API payante coûterait de l'argent et ralentirait la boucle de
développement local, ou dans un contexte où les données ne doivent pas quitter la machine locale.

## 🛠️ Recette
- **Développement itératif rapide** : lancer un modèle open-weight (Llama, Mistral, Qwen) en local
  via Ollama pour itérer sur les prompts et la structure de l'application sans coût ni latence
  réseau, puis basculer sur le modèle de production seulement pour la validation finale.
- **Tests automatisés déterministes** : dans une CI, un modèle local évite la dépendance à une clé
  API externe (secrets à gérer, quota partagé, coût par run de pipeline) pour les tests qui
  vérifient la structure de l'intégration plutôt que la qualité fine des réponses.

```java
// Spring AI : changer de provider ne change que la configuration, pas le code métier
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama3.1
// vs en production :
spring:
  ai:
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
```

- **Ne pas confondre "ça marche en local" et "prêt pour la production"** : un modèle local plus
  petit valide la structure du code (le tool est-il bien appelé, le JSON bien formé) mais pas la
  qualité de raisonnement du modèle de production — garder une étape de validation sur le vrai
  modèle avant mise en production.

## ✅ Résultat attendu
Une boucle de développement rapide et gratuite pour la structure de l'intégration, avec une
validation finale sur le modèle de production réservée aux étapes qui comptent (revue, recette).

## ⚠️ Piège
- **Comparer les performances du modèle local et du modèle de production** comme s'ils étaient
  interchangeables — un petit modèle local sert à valider la plomberie, pas à juger la qualité
  finale des réponses.
- **Oublier de tester avec le vrai modèle avant la mise en production** : une intégration qui
  fonctionne avec un modèle local peut révéler des différences de comportement (formatage, respect
  des instructions) une fois basculée sur le modèle réel.
