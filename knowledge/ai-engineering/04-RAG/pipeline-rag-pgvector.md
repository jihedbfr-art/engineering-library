# Pipeline RAG avec PGVector (Spring Boot)

## 🎯 Objectif
Construire un pipeline RAG (Retrieval-Augmented Generation) minimal mais correct, en réutilisant
PostgreSQL — déjà présent dans le stack (`docs/standards/database.md`) — plutôt que d'introduire
une base vectorielle dédiée dès le premier besoin.

## 🧩 Contexte d'usage
Une base de connaissances interne (documentation produit, notes de procédure, tickets résolus)
doit être interrogeable en langage naturel par un assistant, avec des réponses ancrées dans les
documents réels plutôt qu'hallucinées.

## 🛠️ Recette
1. **Chunking** : découper chaque document en passages de taille raisonnable (300-500 tokens,
   avec chevauchement de ~10-15 %) — pas phrase par phrase (perd le contexte), pas document entier
   (dilue la pertinence).
2. **Indexation** : générer un embedding par chunk, stocker dans une colonne `vector` PGVector avec
   un index HNSW ou IVFFlat selon le volume.
3. **Retrieval** : au moment de la question, calculer l'embedding de la requête, récupérer les k
   passages les plus proches (similarité cosinus), avec un filtre de score minimal pour écarter le
   bruit.
4. **Génération** : injecter les passages récupérés dans le prompt avec leur source, demander au
   modèle de répondre **uniquement** à partir de ces passages et de dire « je ne sais pas » sinon.

```sql
-- Schéma minimal
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE document_chunk (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    embedding VECTOR(1536) NOT NULL
);

CREATE INDEX ON document_chunk USING hnsw (embedding vector_cosine_ops);

-- Retrieval (top 5 passages les plus proches)
SELECT content, 1 - (embedding <=> :query_embedding) AS score
FROM document_chunk
ORDER BY embedding <=> :query_embedding
LIMIT 5;
```

Côté Spring, `VectorStore` de Spring AI (cf. `15-Spring-AI`) encapsule ce schéma directement sur
PGVector — pas besoin de gérer le SQL à la main pour un premier cas d'usage.

## ✅ Résultat attendu
Des réponses citant les documents sources réels, avec un « je ne sais pas » explicite quand
l'information n'est pas dans la base — plutôt qu'une réponse plausible mais inventée.

## ⚠️ Piège
- **Chunking naïf** : couper au caractère près sans respecter les frontières de paragraphe casse
  le sens des passages récupérés — préférer un découpage qui respecte la structure du document.
- **Pas de filtre de score** : renvoyer systématiquement les k meilleurs résultats même quand
  aucun n'est réellement pertinent pousse le modèle à répondre à partir de bruit — fixer un seuil
  de similarité minimal en dessous duquel on répond « pas d'information trouvée ».
- **Oublier la mise à jour** : un document modifié dont les anciens chunks ne sont pas
  supprimés/regénérés fait cohabiter une réponse obsolète avec la version à jour.
