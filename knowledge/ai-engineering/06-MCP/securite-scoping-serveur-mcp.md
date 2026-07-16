# Sécurité et scoping d'un serveur MCP

## 🎯 Objectif
Concevoir un serveur MCP (cf. `06-MCP/introduction.md`) qui expose exactement les capacités
nécessaires à un client donné, ni plus ni moins — au-delà de l'introduction au protocole, ici la
question est : comment le sécuriser en pratique.

## 🧩 Contexte d'usage
Un serveur MCP interne expose des tools qui touchent des données réelles (base de données,
documents internes, actions métier). Une fois qu'un client (agent) y a accès, il faut s'assurer
qu'il ne peut agir que dans son périmètre légitime.

## 🛠️ Recette
- **Authentification au niveau transport** : pour un serveur MCP en HTTP+SSE partagé entre
  plusieurs clients, ne jamais s'appuyer sur le seul protocole MCP pour l'auth — ajouter une couche
  standard (JWT, mTLS) comme pour n'importe quelle API interne.
- **Scoping par tool, pas par serveur entier** : un même serveur MCP peut exposer des tools de
  lecture (recherche, consultation) et d'écriture (modification, suppression) — le scope
  d'autorisation doit se faire tool par tool, pas en tout-ou-rien sur la connexion.
- **Périmètre de données explicite dans chaque tool** : un tool "rechercher un client" doit filtrer
  par le contexte d'autorisation de l'appelant (cf. `15-Spring-AI/chat-client-tool-calling.md` sur
  l'erreur classique de dériver l'identité depuis un argument du prompt plutôt que du contexte de
  sécurité authentifié).
- **Journaliser chaque appel de tool** avec l'identité de l'appelant, comme n'importe quel appel
  d'API sensible (cf. `13-Observability`).

```text
Client agent ──(JWT service-à-service)──> Serveur MCP
                                              ├── tool "search_invoices"   [lecture, scope: user]
                                              ├── tool "cancel_invoice"    [écriture, scope: admin]
                                              └── tool "export_report"     [lecture, scope: manager]
```

## ✅ Résultat attendu
Un serveur MCP où chaque tool a un périmètre d'autorisation explicite et vérifié indépendamment,
avec une trace complète de qui a appelé quoi — la compromission d'un client ne donne pas
automatiquement accès à tous les tools disponibles sur le serveur.

## ⚠️ Piège
- **Confondre "le client a une connexion valide" et "le client peut tout faire"** : le protocole
  MCP négocie les capacités disponibles, pas l'autorisation fine — cette dernière reste à
  implémenter explicitement dans chaque tool.
- **Tools d'écriture sans confirmation** : exposer un tool qui supprime ou modifie des données de
  façon irréversible sans étape de confirmation humaine est le point d'entrée classique d'un abus,
  accidentel ou via prompt injection (cf. `12-Security`).
