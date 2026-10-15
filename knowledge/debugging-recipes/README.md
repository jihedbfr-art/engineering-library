# debugging-recipes

[Version française](README.fr.md)

"It does not work" becomes a checklist tied to the symptom. The aim is to identify the next useful command or test, rather than repeat general theory.

## Entry format

Exact symptom · likely causes, starting with the common ones · step-by-step diagnosis · fix. See [`_TEMPLATE.md`](_TEMPLATE.md).

## Cas documentés récemment

- [Image de test introuvable avant les assertions](image-testconteneurs-introuvable.md) · [English](test-container-image-unavailable.md) : distinguer erreur de registre et échec applicatif, puis vérifier le comportement du substitut.

## Backlog

Spring Boot startup failure · missing bean · circular dependency · `LazyInitializationException` · invalid JWT · stuck Kafka consumer · exiting Docker container · Kubernetes `CrashLoopBackOff` · Oracle timeout · PostgreSQL deadlock. Add entries through the **ajouter-entree-savoir** skill.

## Added recipe

- [MCP tool call loses the authenticated user](mcp-tool-security-context.md) — separate HTTP rejection, missing roles, and Reactor context loss; test the MCP route and two simultaneous clients. [Français](mcp-tool-security-context.fr.md).
