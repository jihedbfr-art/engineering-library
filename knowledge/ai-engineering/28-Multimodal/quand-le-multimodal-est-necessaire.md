# Multimodal — quand c'est réellement nécessaire pour un backend d'entreprise

## 🎯 Objectif
Éviter d'ajouter une capacité multimodale (image, audio) à une application par effet de mode, et
identifier les cas où elle résout un vrai problème métier.

## 🧩 Contexte d'usage
Une demande du type "on pourrait aussi analyser les images/PDF scannés/captures d'écran" apparaît
dans un projet principalement textuel (support, documentation, provisioning).

## 🛠️ Recette
Cas où le multimodal apporte une vraie valeur, par opposition à un ajout cosmétique :
- **Extraction de documents scannés** : factures, bons de commande, formulaires papier numérisés —
  le texte n'existe pas ailleurs sous forme structurée, l'image/PDF est la seule source.
- **Diagnostic visuel** : captures d'écran d'erreurs, schémas d'architecture à interpréter — utile
  en support technique quand la description texte seule est ambiguë.
- **Ce qui n'en a généralement pas besoin** : un assistant de support textuel classique, un système
  de classification de tickets, un RAG documentaire — ajouter du multimodal ici n'apporte rien tant
  que l'entrée réelle des utilisateurs reste du texte.

## ✅ Résultat attendu
Une capacité multimodale ajoutée seulement quand l'entrée réelle du système n'existe pas sous forme
de texte exploitable autrement — pas comme fonctionnalité "parce que le modèle le permet".

## ⚠️ Piège
- **Ajouter le multimodal en solution à un problème de qualité RAG** : si l'extraction de documents
  texte est déjà mauvaise, ajouter de l'image ne résout pas le problème sous-jacent (chunking,
  retrieval, cf. `04-RAG`).
- **Sous-estimer le coût** : le traitement d'image/audio coûte généralement plus cher par requête
  que du texte pur — à ne déclencher que pour les entrées qui le nécessitent réellement, pas
  systématiquement.
