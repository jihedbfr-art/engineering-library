# PGVector vs Milvus — quand migrer d'une base relationnelle vers une base vectorielle dédiée

## 🎯 Objectif
Décider objectivement quand PostgreSQL/PGVector suffit et quand une base vectorielle dédiée
(Milvus, Qdrant) devient nécessaire, plutôt que d'adopter une base spécialisée par défaut.

## 🧩 Contexte d'usage
Un système RAG (cf. `04-RAG`) grandit : le volume de vecteurs augmente, la latence de recherche
devient sensible, ou plusieurs équipes veulent interroger la même base vectorielle indépendamment
du reste de l'application métier.

## 🛠️ Recette
Grille de décision :

| Critère | PGVector reste adapté | Migrer vers Milvus/Qdrant |
|---|---|---|
| Volume de vecteurs | < quelques millions | dizaines de millions+ |
| Colocalisation avec les données métier | Oui, jointures SQL utiles (filtrer par utilisateur, statut...) | Non, besoin isolé |
| Latence de recherche exigée | Quelques dizaines de ms acceptables | < 10ms à grande échelle |
| Équipe/infra | Déjà experte PostgreSQL, pas de budget infra dédié | Prête à opérer un service supplémentaire |
| Fonctionnalités avancées (sharding auto, réplication vectorielle) | Pas nécessaire | Nécessaire |

Dans la majorité des applications d'entreprise (pas des moteurs de recherche à l'échelle d'un
Google), PGVector avec un index HNSW correctement dimensionné couvre largement le besoin — la
bascule vers un système dédié se justifie par des métriques mesurées (latence P99 dépassée, volume
réel constaté), pas par anticipation.

## ✅ Résultat attendu
Un choix d'infrastructure justifié par des chiffres réels (volume, latence mesurée) plutôt que par
la nouveauté technologique — et une base de données en moins à opérer tant que ce n'est pas
nécessaire.

## ⚠️ Piège
- **Sur-ingénierie précoce** : introduire Milvus dès le premier prototype RAG ajoute une pièce
  d'infrastructure (cluster, monitoring, backup) sans bénéfice mesurable à faible volume.
- **Sous-dimensionner l'index PGVector** : sans index HNSW/IVFFlat correctement configuré, une
  recherche vectorielle sur PostgreSQL dégénère en scan complet — le problème est souvent la
  configuration de l'index, pas le moteur lui-même.
