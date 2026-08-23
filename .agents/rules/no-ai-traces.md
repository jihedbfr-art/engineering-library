---
description: "Aucune trace d'outil d'IA dans les commits, PR ou code"
---

# Règle : Aucune trace d'outil d'IA

- **NE JAMAIS** ajouter de ligne `Co-Authored-By` référençant un assistant d'IA (Claude, ChatGPT, Copilot, etc.) dans un message de commit.
- **NE JAMAIS** mentionner un assistant d'IA, un outil de génération, ou le fait qu'une contribution a été générée par IA — dans un message de commit, une description de PR, ou un nom de branche.
- **NE JAMAIS** ajouter de commentaire dans le code sans demande explicite de l'utilisateur. Un commit ne doit pas porter la trace visible de la façon dont il a été produit.
- Les messages de commit et descriptions de PR se lisent comme s'ils avaient été écrits par l'ingénieur humain propriétaire du dépôt : factuels, sur le fond (quoi/pourquoi), sans mention d'outillage IA.
- **Vérification avant de commiter :** relire le message de commit et la description de PR pour s'assurer qu'aucune des mentions ci-dessus ne s'y trouve, avant de créer le commit ou la PR.
