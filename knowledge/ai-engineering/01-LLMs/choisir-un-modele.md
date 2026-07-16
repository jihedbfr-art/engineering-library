# Choisir un modèle pour une application backend Java/Spring

## 🎯 Objectif
Sélectionner un LLM pour un cas d'usage d'entreprise (classification, extraction, assistant
interne) selon des critères concrets, plutôt que selon le classement générique du moment.

## 🧩 Contexte d'usage
Intégration d'un LLM dans un service Spring Boot (via `ChatClient`, cf. `15-Spring-AI`) pour une
tâche définie : résumé de tickets, classification d'email, assistant de recherche interne.

## 🛠️ Recette
Grille de critères, dans l'ordre où ils éliminent réellement des candidats :

1. **Contrainte de résidence des données** : certains contextes (secteur régulé, télécom, banque)
   imposent que les données ne quittent pas un périmètre géographique/contractuel précis — ça
   élimine d'emblée certaines options avant même de comparer les performances.
2. **Support du tool calling / sortie structurée** : indispensable si le modèle doit s'intégrer à
   des méthodes Java existantes (cf. `15-Spring-AI`) plutôt que produire du texte libre.
3. **Rapport qualité/coût sur la tâche précise** : un modèle plus petit et moins cher suffit
   souvent pour de la classification ; un modèle plus capable se justifie pour du raisonnement
   multi-étapes complexe. Mesurer sur des exemples réels du domaine (cf. `10-Evaluation`), pas sur
   un benchmark générique.
4. **Latence acceptable pour l'usage** : un assistant interactif tolère mal plus de quelques
   secondes ; un traitement batch nocturne tolère un modèle plus lent mais moins cher.

## ✅ Résultat attendu
Un choix de modèle documenté (sous forme d'ADR, cf. `engineering-decisions`) avec les critères
réels qui ont tranché — reproductible et réévaluable quand un nouveau modèle sort, plutôt qu'un
choix figé par habitude.

## ⚠️ Piège
- **Choisir sur la réputation plutôt que sur la tâche** : le modèle le plus connu n'est pas
  toujours le plus adapté à une tâche étroite comme la classification — un modèle plus petit bien
  prompté fait souvent aussi bien pour moins cher.
- **Ne jamais revisiter le choix** : le paysage des modèles change vite ; une décision non
  documentée (pas d'ADR) devient un choix qu'on n'ose plus remettre en question faute de connaître
  la raison initiale.
