# Portefeuille de projets agentiques

Un catalogue de 500 idées de projets d'ingénierie agentique — agents autonomes, serveurs MCP,
sécurité Zero-Trust appliquée aux outils — classées par granularité, plus l'analyse d'architecture
qui les sous-tend.

> **Ce que c'est, et ce que ce n'est pas.** Ce dossier est un **catalogue d'idées**, pas un
> ensemble de projets. Aucun des 500 n'est écrit. Il vit dans `knowledge/` et non dans `projects/`
> pour cette raison exacte : `projects/` contient des projets applicatifs réels et complets, et une
> ligne de tableau n'en est pas un. Un projet qui sort d'ici et devient réel prend son propre
> dossier sous `projects/`, avec son `CLAUDE.md`, et disparaît de la liste des idées.

## Contenu

| Document | Ce qu'il contient |
|---|---|
| [Analyse d'architecture](analyse-architecture.md) | Les fondations : orchestration d'agents (sous-agents, déclencheurs, hooks de cycle de vie), standardisation MCP sur JSON-RPC 2.0, et le modèle de sécurité — empoisonnement d'outils (OWASP MCP Top 10), passerelles MCP, et l'échange de jetons RFC 8693 avec Keycloak pour supprimer le *token passthrough*. |
| [Échelle 1 — Nano (1-100)](echelles/01-nano-1-to-100.md) | Unités d'exécution : hooks, intercepteurs, utilitaires de validation. |
| [Échelle 2 — Micro (101-200)](echelles/02-micro-101-to-200.md) | Composants isolés et outils MCP unitaires. |
| [Échelle 3 — Mini (201-300)](echelles/03-mini-201-to-300.md) | Services complets à responsabilité unique. |
| [Échelle 4 — Standard (301-400)](echelles/04-standard-301-to-400.md) | Applications multi-composants. |
| [Échelle 5 — Macro (401-500)](echelles/05-macro-401-to-500.md) | Plateformes et architectures distribuées. |
| [Projets phares](projets-phares/) | Trois spécifications détaillées, choisies parce qu'elles recoupent le reste de l'écosystème : passerelle MCP de publication sociale, essaim de sagas de provisioning BSS, et validation de menaces MCP via SPI Keycloak. |

## Statut de vérification

À lire avant de traiter l'analyse comme une référence :

- **Les primitives du SDK d'orchestration citées** (sandboxes distantes à état, sous-agents en
  worktrees Git isolés, les neuf points d'interception des hooks) viennent de la documentation
  éditeur. Elles n'ont **pas** été vérifiées contre un SDK en exécution dans cet écosystème.
- **Les points MCP, OWASP et RFC 8693 recoupent du connu.** L'échange de jetons On-Behalf-Of avec
  la revendication `act` est le mécanisme décrit dans la RFC 8693, et Keycloak l'implémente. Le
  reste de l'écosystème touche déjà ce terrain : voir
  [`spring-keycloak-toolkit`](https://github.com/jihedbfr-art/spring-keycloak-toolkit) et
  [`keycloak-spi-workbench`](https://github.com/jihedbfr-art/keycloak-spi-workbench).
- **Les 500 descriptions sont des intentions, pas des conceptions.** Chacune tient en une phrase.
  Une idée qui passe à l'implémentation demande d'abord un ADR dans
  [`engineering-decisions`](../../engineering-decisions/), comme n'importe quelle autre.

## Voir aussi

- [`12-mcp-protocol-and-servers`](../12-mcp-protocol-and-servers/) — le savoir MCP déjà capitalisé.
- [`11-agent-identity-and-access`](../11-agent-identity-and-access/) — identité et accès des agents.
- [`08-guards-safety`](../08-guards-safety/) — garde-fous et sécurité d'exécution.
