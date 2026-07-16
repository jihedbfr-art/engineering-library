# Structured Output — forcer une sortie exploitable par du code

## 🎯 Objectif
Obtenir une sortie de LLM directement désérialisable (JSON conforme à un schéma), plutôt que du
texte libre à parser avec des regex fragiles.

## 🧩 Contexte d'usage
Chaque fois que la sortie du modèle doit être consommée par du code Java (mapping vers un DTO,
décision automatisée, appel d'API en aval) plutôt que lue par un humain.

## 🛠️ Recette
- **Décrire le schéma explicitement**, avec des types et des contraintes précises, pas seulement
  un exemple : le modèle suit mieux un schéma formel qu'une simple illustration.
- **Fermer les champs libres** : préférer des enums à des chaînes libres quand les valeurs
  possibles sont connues (`"statut": "OUVERT" | "FERME" | "EN_ATTENTE"` plutôt qu'une chaîne
  quelconque).
- **Valider côté code, ne jamais faire confiance à la sortie brute** : même avec un schéma bien
  décrit, parser en `try/catch` et rejeter/relancer si la désérialisation échoue.

```java
record TicketClassification(
    String categorie,   // enum-like: "BUG" | "FEATURE" | "QUESTION"
    int priorite,        // 1-5
    boolean necessite_escalade
) {}

// Prompt : demander explicitement un JSON conforme à ce schéma, rien d'autre autour.
// Avec Spring AI : ChatClient expose un BeanOutputConverter qui génère le schéma
// automatiquement depuis le record Java et échoue proprement si la sortie ne matche pas.
```

## ✅ Résultat attendu
Une sortie qui se désérialise directement en objet Java sans étape de nettoyage de texte
intermédiaire, avec un taux d'échec de parsing proche de zéro sur un modèle récent correctement
prompté.

## ⚠️ Piège
- **Laisser le modèle ajouter du texte autour du JSON** ("Voici le résultat : {...}") casse le
  parsing strict — demander explicitement une sortie qui ne contient *que* le JSON, et utiliser le
  mode de sortie structurée natif du modèle/framework quand il existe plutôt que de compter
  uniquement sur l'instruction en langage naturel.
- **Schéma trop permissif** : un champ `string` libre là où un enum suffirait réintroduit de la
  variabilité qu'il faudra normaliser après coup — fermer le schéma dès que possible.
