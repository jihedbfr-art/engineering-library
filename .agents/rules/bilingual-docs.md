---
description: "Documentation bilingue obligatoire (EN + FR) pour tout README"
---

# Règle : Documentation bilingue (README)

- Tout fichier `README.md` doit avoir un jumeau `README.fr.md` dans le même dossier, avec un contenu équivalent (pas une traduction mot à mot, mais la même information).
- Les fichiers techniques restent en anglais uniquement : `SKILL.md`, tout ce qui vit sous `docs/` (guides, formats, références techniques). Pas de jumeau `.fr.md` pour ces fichiers.
- **Mise à jour synchronisée :** toute modification de fond apportée à un `README.md` doit être répercutée dans son `README.fr.md` dans la même PR. Un couple désynchronisé est un défaut à corriger, pas un état acceptable.
- **Nouveau dossier :** tout nouveau dossier (racine de repo, domaine, skill) qui reçoit un `README.md` reçoit son `README.fr.md` au même moment — pas dans une PR séparée « plus tard ».
- **Vérification avant de conclure qu'un repo est conforme :** lister tous les `README.md` du repo et vérifier qu'un `README.fr.md` existe à côté de chacun, avant d'affirmer que la couverture bilingue est complète.
