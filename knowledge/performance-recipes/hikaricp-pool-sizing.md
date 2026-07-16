# HikariCP : dimensionner le pool de connexions

> Un pool trop petit sature sous charge ; un pool trop grand sature la base de données elle-même.

## 📏 Comment mesurer
Observer le nombre de connexions actives simultanées sous charge réelle (métriques Actuator
`hikaricp.connections.active`) plutôt que de deviner une valeur.

## 🎚️ Levier
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20   # formule de départ : threads_cpu * (1 + temps_attente/temps_cpu)
      minimum-idle: 5
      connection-timeout: 3000
      leak-detection-threshold: 5000
```
Formule de départ classique (PostgreSQL docs) : `connexions = ((cœurs_cpu * 2) + effective_spindle_count)`
— un point de départ à ajuster avec la mesure réelle, pas une valeur figée.

## 📈 Gain attendu
Éviter à la fois les timeouts de connexion sous charge (pool trop petit) et la contention côté
serveur de base de données (pool trop grand qui dépasse ses connexions max autorisées).

## ⚠️ Piège
Augmenter `maximum-pool-size` en réaction à un incident sans avoir mesuré la cause réelle (souvent
une transaction qui retient la connexion trop longtemps, cf.
[debugging-recipes/connexion-pool-epuise.md](../debugging-recipes/connexion-pool-epuise.md)) ne
résout rien : ça repousse juste le seuil de saturation.
