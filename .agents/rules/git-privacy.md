---
description: "Règle stricte de confidentialité Git (pas de données personnelles)"
---

# Règle : Confidentialité Git et Données Personnelles

- **NE JAMAIS** configurer d'adresse e-mail personnelle ou d'autres informations privées sensibles dans les configurations Git (`git config user.email`).
- Utiliser systématiquement une adresse e-mail masquée ou générique (par exemple `@users.noreply.github.com`) pour les commits sur des dépôts publics, à moins d'une indication contraire explicite pour un dépôt privé.
- Toujours vérifier le statut `git config --get user.email` avant de commiter pour s'assurer que l'identité configurée ne fuite pas d'informations personnelles sur les répertoires publics.
- **Nettoyage :** Si des données personnelles sont détectées en clair dans le code ou dans l'historique avant un push, interrompre l'action et nettoyer ces données.
