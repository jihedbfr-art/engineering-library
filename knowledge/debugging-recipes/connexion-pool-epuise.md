# `HikariPool-1 - Connection is not available, request timed out after Xms`

> Le pool de connexions HikariCP (par défaut avec Spring Boot) n'a plus de connexion disponible à
> distribuer ; les nouvelles requêtes attendent puis échouent en timeout.

## Causes probables (fréquentes → rares)
1. Des connexions sont retenues plus longtemps que nécessaire (transaction longue, appel externe
   fait à l'intérieur d'une transaction JPA au lieu d'après).
2. Le pool est sous-dimensionné par rapport à la charge réelle (`maximum-pool-size` trop bas).
3. Une fuite de connexion : une ressource ouverte manuellement (JDBC brut) jamais fermée.

## Diagnostic pas-à-pas
```text
# 1. Activer les logs HikariCP (leak-detection-threshold) pour repérer les connexions non rendues
# 2. Chercher dans le code les transactions qui englobent un appel réseau externe (HTTP, Kafka)
#    en plus de l'accès base — l'appel externe lent retient la connexion inutilement
# 3. Comparer maximum-pool-size au nombre de threads concurrents réels sous charge
```

## Correctif
- Sortir tout appel externe (HTTP, Kafka, calcul long) du périmètre `@Transactional` — la
  transaction ne doit couvrir que les accès base eux-mêmes.
- Dimensionner `maximum-pool-size` en fonction de la charge mesurée, pas par défaut arbitraire (cf.
  [performance-recipes/hikaricp-pool-sizing.md](../performance-recipes/hikaricp-pool-sizing.md)).
- Activer `leak-detection-threshold` en environnement de test pour détecter les fuites avant la
  production.

## Si ça ne suffit pas
Vérifier aussi côté base de données (PostgreSQL/Oracle) le nombre de connexions max autorisées
côté serveur — un pool applicatif correctement dimensionné peut quand même se heurter à une limite
serveur plus basse.
