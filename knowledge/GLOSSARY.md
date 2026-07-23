# Glossaire transverse

> Termes utilisés à travers plusieurs domaines de cette bibliothèque, définis une seule fois ici
> plutôt que répétés (ou définis différemment) dans chaque fiche. Un glossaire spécifique au
> télécom existe séparément dans [`telecom/glossary.md`](telecom/glossary.md).

**ADR (Architecture Decision Record)** — document court et immuable qui capture une décision
d'architecture, son contexte et ses conséquences. Ce dépôt n'a pas encore de dossier dédié pour
ça — les décisions passées sont pour l'instant dispersées dans les fiches `architecture-library/`
concernées.

**Cohérence à terme (eventual consistency)** — un système accepte une fenêtre temporaire
d'incohérence entre ses composants, avec la garantie qu'ils convergent vers le même état à terme.
Voir [`architecture-library/event-driven-vs-request-response.md`](architecture-library/event-driven-vs-request-response.md).

**Guardrail** — une couche de validation entre la sortie d'un composant (souvent un LLM) et
l'action qu'elle déclenche, pour empêcher une sortie incohérente d'atteindre le code métier.
Voir [`ai-engineering/08-guards-safety/`](ai-engineering/08-guards-safety/).

**Idempotence** — propriété d'une opération qui produit le même résultat qu'elle soit exécutée une
ou plusieurs fois. Pas encore de fiche API design dédiée dans ce dépôt ; le pattern revient dans
plusieurs recettes `ai-engineering/` (retry côté agent, appels d'outils rejouables).

**MCP (Model Context Protocol)** — protocole standard permettant à un agent (client) de découvrir
et d'appeler les capacités (tools/resources/prompts) d'un serveur externe. Pas encore de fiche
dédiée ; le sujet le plus proche dans ce dépôt est
[`ai-engineering/07-extensibility/`](ai-engineering/07-extensibility/).

**MVCC (Multi-Version Concurrency Control)** — mécanisme qui permet aux lecteurs et aux écrivains
d'une base de données de ne jamais se bloquer mutuellement, via des versions multiples des mêmes
données. Voir [`database-engineering/postgresql.md`](database-engineering/postgresql.md).

**N+1 (problème)** — une requête initiale suivie de N requêtes supplémentaires exécutées une par
une au lieu d'une seule requête groupée. Voir [`engineering-failures/hibernate-n-plus-1.md`](engineering-failures/hibernate-n-plus-1.md).

**RAG (Retrieval-Augmented Generation)** — technique consistant à récupérer des passages de
documents pertinents et à les injecter dans le prompt d'un LLM pour ancrer sa réponse dans des
données réelles plutôt que dans sa seule mémoire paramétrique.
Voir [`ai-engineering/02-rag-architectures/`](ai-engineering/02-rag-architectures/).

**Saga** — pattern de transaction distribuée qui décompose une opération métier multi-services en
étapes locales compensables, sans transaction ACID globale.
Voir [`architecture-library/saga-pattern.md`](architecture-library/saga-pattern.md).

**Tool calling** — capacité d'un LLM à invoquer une fonction/méthode décrite par un schéma plutôt
que de produire seulement du texte libre. Voir
[`ai-engineering/03-agentic-workflows/`](ai-engineering/03-agentic-workflows/) pour des exemples
concrets ; pas encore de fiche spécifique à l'intégration Spring AI dans ce dépôt.
