# Keycloak répond, mais sa sonde Docker échoue

[English version](README.md)

Cas observé avec l'image `quay.io/keycloak/keycloak:26.0` de la démo MCP Guard : le endpoint de découverte du realm répondait HTTP 200, alors que `/health/ready` sur le port 8080 répondait 404. Le processus en cours et la sonde de disponibilité ne vérifiaient pas la même chose.

## Diagnostiquer avant de redémarrer

```bash
docker compose -f docker/docker-compose.yml ps
docker compose -f docker/docker-compose.yml logs --tail 60 keycloak
```

Lire le port, le chemin et la réponse attendue par la sonde. Vérifier si `KC_HEALTH_ENABLED` est activé. Ne pas déduire la disponibilité de la seule ouverture du port applicatif. Éviter de copier tout l'environnement du conteneur dans un ticket : il peut contenir des identifiants.

## Correctif vérifié dans ce cas

Activer `KC_HEALTH_ENABLED=true` et interroger le endpoint de gestion dans le conteneur :

```bash
{ printf 'HEAD /health/ready HTTP/1.0\r\n\r\n' >&0; grep 'HTTP/1.0 200'; } 0<>/dev/tcp/localhost/9000
```

Cette commande Bash s'exécute dans le conteneur Keycloak, pas sur l'hôte. Elle ne nécessite ni curl dans l'image ni publication du port hôte 9000. Le chemin et le port doivent correspondre à la configuration déployée. Le [guide de santé de l'éditeur](https://www.keycloak.org/observability/health) décrit les options et la sonde.

Le [correctif proposé exact](https://github.com/jihedbfr-art/mcp-keycloak-guard/commit/d05ce06) comprend le healthcheck Compose et un guide bilingue. Dans un nouveau clone de la démo positionné sur ce commit, attendre la disponibilité avec :

```bash
docker compose -f docker/docker-compose.yml up -d --wait --wait-timeout 180
```

## Poursuivre le diagnostic au-dessus de la santé HTTP

| Observation | Contrôle suivant |
|---|---|
| Le conteneur est healthy | Initialiser le client MCP avec le transport réellement exposé |
| La requête HTTP réussit | Examiner l'enveloppe JSON-RPC et le résultat de l'outil |
| `result.isError` vaut true | Diagnostiquer l'échec d'exécution ; ne pas annoncer l'action comme réussie |
| La structure d'un résultat textuel est acceptable | Vérifier son sens et les droits nécessaires avant de l'utiliser |

L'[atelier de réponses hors ligne](https://github.com/jihedbfr-art/ai-skills/blob/cfdfc41/skills/05-mcp-protocol-and-tools/mcp-sse-transport-and-resource-providers/result-lab/README.fr.md) fournit six fixtures synthétiques. Exécuter `tool-error.json` et `untrusted-content.json`, puis expliquer pourquoi ni un transport réussi ni `candidate_success` du classificateur ne donnent une autorisation. L'atelier nécessite seulement Python et ne se connecte pas à la démo. Cette version figée est proposée dans une PR, pas dans une release.

## Preuve de disponibilité consignée

Le 14 octobre 2026, la sonde d'origine échouait avec le code de sortie 1. Après correction, l'attente Compose s'est terminée avec succès et plusieurs sondes consécutives ont répondu HTTP 200. Le test utilisait un projet Compose isolé sans port hôte publié ; son conteneur et son réseau ont ensuite été supprimés.

Ce résultat démontre la disponibilité de la configuration de démo testée. Il ne teste ni les rôles utilisateur ni l'appel d'un outil MCP. Conserver séparément les preuves de santé HTTP, d'authentification et d'autorisation des outils. La [PR](https://github.com/jihedbfr-art/mcp-keycloak-guard/pull/2) est une correction proposée, pas une release.

---

<div align="center">
  <img src="../../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>Un projet <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a></sub>
</div>
