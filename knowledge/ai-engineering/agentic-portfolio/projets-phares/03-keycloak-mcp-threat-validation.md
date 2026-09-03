# 3. Keycloak MCP Threat Validation SPI

Pour protéger les infrastructures contre l'empoisonnement d'outils et l'injection de prompts via des serveurs MCP compromis, ce projet développe un composant SPI (Service Provider Interface) personnalisé pour Keycloak.

Agissant comme un intercepteur de niveau 7 (L7 Proxy), le SPI inspecte de manière asynchrone les requêtes et les réponses JSON-RPC 2.0 traversant la passerelle. Il valide cryptographiquement les schémas d'outils présentés par les serveurs MCP pour s'assurer qu'ils n'ont pas été altérés depuis leur déploiement (protection contre les attaques "Rug Pull"). En outre, le SPI analyse les charges utiles générées par le LLM pour détecter les structures d'injection de commandes système, rejetant toute requête non conforme avant même qu'elle n'atteigne le serveur MCP cible, consolidant ainsi la sécurité Zero-Trust.
