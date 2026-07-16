# MCP — Model Context Protocol : introduction

## 🎯 Objectif
Comprendre ce qu'est le Model Context Protocol, pourquoi il existe, et comment un serveur MCP
expose des *tools*/*resources*/*prompts* à un client (agent, IDE, CLI) de façon standardisée.

## 🧩 Contexte d'usage
Dès qu'on veut connecter un agent (ex. un assistant de code) à une source externe — une base de
données, une API interne, un système de fichiers, un outil métier — sans écrire un connecteur
propriétaire par paire (agent × outil). MCP remplace le M×N de connecteurs ad hoc par un protocole
commun côté client et côté serveur.

## 🛠️ Recette
Architecture minimale d'un serveur MCP :
- **Transport** : stdio (process local) ou HTTP+SSE (service distant).
- **Capacités déclarées** : `tools` (fonctions appelables avec schéma JSON), `resources` (données
  lisibles, ex. fichiers/URI), `prompts` (templates réutilisables).
- **Cycle de vie** : le client négocie les capacités à la connexion, puis appelle les tools comme
  des fonctions distantes typées (JSON Schema en entrée/sortie).

```text
Client (agent)  <—stdio/HTTP+SSE—>  Serveur MCP
                                       ├── tools/list, tools/call
                                       ├── resources/list, resources/read
                                       └── prompts/list, prompts/get
```

Exemple minimal côté Java (Spring AI expose un starter MCP serveur/client — cf. `15-Spring-AI`) :
un tool est une méthode annotée avec un nom, une description et un schéma de paramètres ; le
serveur les liste automatiquement au client qui se connecte.

## ✅ Résultat attendu
Un agent peut découvrir dynamiquement les capacités d'un serveur (sans hardcoder les endpoints),
appeler un tool avec des arguments validés par schéma, et recevoir une sortie structurée — le tout
sans coupler le code de l'agent à l'implémentation du serveur.

## ⚠️ Piège
- **Sur-permissif** : un tool qui expose une commande shell ou une requête SQL brute sans
  validation ni scope = surface d'attaque (cf. `12-Security`, prompt injection / tool abuse).
- **Transport mal choisi** : stdio pour un outil qui doit être partagé entre plusieurs clients
  distants ne fonctionne pas — HTTP+SSE est alors nécessaire, avec authentification à ajouter
  explicitement (MCP ne l'impose pas par défaut).
- **Schémas trop vagues** : un tool avec des paramètres non typés/non documentés pousse le modèle
  à halluciner des arguments — décrire précisément chaque paramètre dans le schéma JSON.
