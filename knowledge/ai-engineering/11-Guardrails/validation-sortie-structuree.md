# Guardrails — valider la sortie structurée avant exécution

## 🎯 Objectif
Mettre en place une couche de validation entre la sortie brute d'un LLM et toute action qu'elle
déclenche, pour qu'une sortie malformée ou incohérente n'atteigne jamais le code métier tel quel.

## 🧩 Contexte d'usage
Un tool calling (cf. `15-Spring-AI`) ou une sortie structurée (cf. `02-Prompt-Engineering`) qui
alimente directement une action — appel d'API, écriture en base, décision d'escalade — doit être
validée, même si le modèle respecte le schéma la plupart du temps.

## 🛠️ Recette
Trois niveaux de garde-fous, du plus simple au plus strict :

1. **Validation de schéma** : la sortie désérialise-t-elle vers le type attendu ? (déjà couvert par
   la désérialisation stricte, cf. `02-Prompt-Engineering/structured-output.md`).
2. **Validation métier** : les valeurs sont-elles plausibles dans le domaine ? (une priorité de
   ticket à "8" sur une échelle 1-5 doit être rejetée même si le JSON est syntaxiquement valide).
3. **Validation de portée (scope)** : l'action demandée reste-t-elle dans le périmètre autorisé
   pour ce contexte ? (un agent de support ne doit pas pouvoir déclencher une action de suppression
   de compte, même si le modèle "décide" de l'appeler).

```java
public TicketAction validate(TicketAction proposed) {
    if (proposed.priorite() < 1 || proposed.priorite() > 5) {
        throw new InvalidAiOutputException("priorite hors limites: " + proposed.priorite());
    }
    if (!ALLOWED_ACTIONS.contains(proposed.action())) {
        throw new InvalidAiOutputException("action hors perimetre: " + proposed.action());
    }
    return proposed;
}
```

## ✅ Résultat attendu
Un système où une sortie de modèle incohérente ou manipulée (via prompt injection, cf.
`12-Security`) échoue proprement à la validation plutôt que de déclencher silencieusement une
action incorrecte.

## ⚠️ Piège
- **Valider seulement le schéma, pas le métier** : un JSON syntaxiquement parfait peut quand même
  contenir une décision absurde ou dangereuse — la validation de schéma est nécessaire mais très
  loin d'être suffisante.
- **Guardrails invisibles au débogage** : si une action est rejetée silencieusement sans log
  explicite de la raison, l'équipe perd des heures à comprendre pourquoi l'agent "ne fait rien" —
  toujours logger la validation échouée avec le motif précis.
