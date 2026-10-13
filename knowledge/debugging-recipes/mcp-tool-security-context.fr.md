# L'appel d'un outil MCP perd l'identité de l'utilisateur

> La connexion SSE accepte le jeton, mais l'outil protégé renvoie une erreur d'authentification. Même l'administrateur ne peut pas l'appeler.

Cette recette concerne une configuration Spring Boot 3.3.5, Spring AI 1.0.0 et Spring Security resource server avec serveur MCP WebMVC. Les autres transports et versions demandent une vérification distincte.

## Causes probables

1. La requête est refusée avant l'ouverture de la session MCP : jeton absent ou invalide, émetteur incorrect, ou règle de sécurité sur `/sse`.
2. Le jeton est accepté, mais ses rôles ne sont pas convertis en autorités utilisées par `@PreAuthorize`.
3. La requête HTTP possède un `SecurityContext` authentifié, mais le callback de l'outil s'exécute sur un worker Reactor sans ce contexte lié au thread.

## Diagnostic, dans cet ordre

1. Appeler `GET /sse` sans jeton puis avec un jeton de test valide. Un `401` pour la première requête prouve seulement la protection HTTP ; il ne prouve rien sur l'autorisation des outils.
2. Avec un vrai client MCP, appeler un outil de lecture en tant que lecteur et un outil privilégié en tant qu'administrateur. Relever le résultat MCP et son indicateur d'erreur. Un refus d'outil peut être renvoyé comme erreur MCP plutôt que comme HTTP `403`.
3. Comparer avec un appel direct de la même méthode `@PreAuthorize` sous une authentification de test. Si cet appel réussit mais que l'appel MCP échoue pour le même administrateur, examiner le passage au traitement asynchrone avant de modifier les rôles.
4. Refaire l'appel privilégié en tant que lecteur après celui de l'administrateur, puis lancer les deux clients simultanément. Le lecteur doit rester refusé dans les deux cas. Ne jamais afficher les jetons dans les logs ou les résultats de test.

Dans un projet Maven doté d'un test d'intégration qui démarre son fournisseur d'identité et utilise un client MCP :

```bash
mvn -q verify
```

Vérifier le rapport de test et les quatre observations : SSE anonyme refusé, outil de lecture permis au lecteur, outil privilégié refusé au lecteur, outil privilégié permis à l'administrateur. Le test d'une méthode appelée directement ne suffit pas.

## Correctif du cas confirmé

Le serveur de référence testé enregistre un `ThreadLocalAccessor` Micrometer pour `SecurityContextHolder` et active la propagation automatique du contexte Reactor avant l'exécution des callbacks. Les méthodes protégées conservent `@PreAuthorize` : chaque outil contrôle toujours les rôles de l'appelant. Après la correction, retester le trajet MCP complet, surtout deux clients simultanés de rôles différents.

La configuration correspondante dans ce projet Spring AI 1.0.0 est :

```java
@PostConstruct
void enableSecurityContextPropagation() {
  ContextRegistry.getInstance()
      .registerThreadLocalAccessor(
          "spring.security.context",
          SecurityContextHolder::getContext,
          SecurityContextHolder::setContext,
          SecurityContextHolder::clearContext);
  Hooks.enableAutomaticContextPropagation();
}
```

Enregistrer l'accesseur et le hook au démarrage, avant les nouveaux abonnements Reactor. Conserver le test d'intégration avec le client MCP : cet extrait seul ne prouve pas que le contexte atteint le callback dans une autre application.

Ne pas placer le contexte de sécurité dans une variable globale et ne pas déduire l'identité d'un argument d'outil. Le contexte appartient à la requête authentifiée. Si le jeton ou la conversion des rôles est en cause, corriger cette frontière plutôt que d'ajouter une propagation.

## Si cela ne suffit pas

Vérifier les chemins du transport MCP réellement utilisé et la version de Spring AI. Dans la configuration de référence, SSE utilise `/sse` et les messages `/mcp/message` ; un POST vers `/mcp` renvoie `404`. Les cinq tests d'intégration passants de cette configuration couvraient des appels successifs et une paire lecteur/admin simultanée. Ce n'était pas un test de charge.

Implémentation de référence : [configuration du contexte de sécurité de MCP Guard](https://github.com/jihedbfr-art/mcp-keycloak-guard/blob/177fdef/src/main/java/dev/jihed/mcpguard/config/SecurityContextPropagationConfig.java). Le commit lié est publié dans la [PR nº 1](https://github.com/jihedbfr-art/mcp-keycloak-guard/pull/1) ; il n'est pas encore intégré à la branche principale.

Références officielles : [transport du serveur MCP Spring AI 1.0](https://docs.spring.io/spring-ai/reference/1.0/api/mcp/mcp-server-boot-starter-docs.html) et [propagation de contexte Reactor](https://projectreactor.io/docs/core/release/reference/advanced-contextPropagation.html).
