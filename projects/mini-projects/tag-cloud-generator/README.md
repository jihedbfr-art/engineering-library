# tag-cloud-generator

Colle un texte, obtiens un nuage de mots-clés pondéré par fréquence — les mots les plus
répétés s'affichent en plus grand. Tout se passe côté client, aucune donnée n'est envoyée
nulle part.

## Lancer

Ouvrir `index.html` dans un navigateur.

## Détails

- Filtre optionnel des mots courants français (le, la, un, que...) via la case à cocher.
- Les 40 mots les plus fréquents sont retenus et affichés dans un ordre mélangé (pas trié
  du plus grand au plus petit) pour donner un vrai effet de nuage plutôt qu'une liste.
- Regex simple sur les lettres accentuées françaises (`[a-zà-ÿ]+`) — pas un vrai tokenizer
  linguistique, donc les mots composés avec apostrophe (`aujourd'hui`) sont coupés en deux.
