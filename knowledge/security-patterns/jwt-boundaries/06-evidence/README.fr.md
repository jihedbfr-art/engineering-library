# Preuves et reproduction maîtrisée

[English version](README.md) · [Parcours](../README.fr.md)

Utiliser la preuve minimale qui répond à la question, puis préciser sa portée. Les tests du convertisseur prouvent un mapping sur des claims fournis. La suite de JWT signés ajoute validation réelle de signature/claims et HTTP. La suite Keycloak ajoute l’émission réelle. Le smoke script ajoute le JAR exécutable construit et le câblage Compose.

Le smoke checker affiche six noms de scénarios et leurs statuts, sans access token. Le projet de test écoute en loopback ; la validation locale a choisi des ports libres et arrêté uniquement son processus Java et ses ressources Compose. L’exemple public documente ses ports par défaut et une commande d’arrêt limitée à son projet.

Le succès du JAR ne prouve pas une aptitude à la production. Rotation, révocation, sessions navigateur, MFA, séparation de tenants et transport MCP restent hors périmètre. Distinguer une hypothèse non testée d’un défaut observé. Conserver révision, commandes, comptes de tests, plateforme et limites lors d’une passation.

## Rejouer

Suivre les terminaux et prérequis du [laboratoire](https://github.com/jihedbfr-art/spring-keycloak-toolkit/tree/dc2e74e12dafbc337ea94848694fec1bbf1631b6/examples/resource-server). Après construction et démarrage local de Keycloak et du JAR, depuis `examples/resource-server` :

```bash
python smoke.py
```

## Exercices corrigés

1. Quelle preuve ajoute le packaging aux tests HTTP ?

<details>
<summary>Correction</summary>

Lancer le JAR construit et les six scénarios smoke, au lieu de démarrer seulement l’application de test.

</details>

2. Un laboratoire JWT vert peut-il être annoncé comme un test d’autorisation MCP ?

<details>
<summary>Correction</summary>

Non. Il ne comporte ni transport MCP ni invocation d’outil.

</details>

3. Que doit contenir une passation reproductible ?

<details>
<summary>Correction</summary>

Commit exact, commandes, versions, résultats et limites ; ni jetons réels ni entrées privées de production.

</details>

## Sources et preuve

- [Tests de cette révision](https://github.com/jihedbfr-art/spring-keycloak-toolkit/blob/dc2e74e12dafbc337ea94848694fec1bbf1631b6/examples/resource-server/src/test/java/lab/JwtBoundaryTest.java).
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).

---

<div align="center">
  <img src="../../../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>Un projet <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a></sub>
</div>
