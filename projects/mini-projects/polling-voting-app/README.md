# polling-voting-app

## But
Créer un sondage (question + options), voter, voir les résultats en pourcentage.

## Choix technique
**`com.sun.net.httpserver.HttpServer`** (JDK standard), pas de framework. **Stockage en mémoire
uniquement** (`ConcurrentHashMap`) : redémarrer le serveur efface tous les sondages et votes — voulu
pour rester simple, affiché clairement dans l'UI.

## Architecture
- `src/VotingServer.java` : serveur HTTP. Routes `/polls` (GET liste, POST création),
  `/vote` (POST `{pollId, optionIndex}`), `/` (statique). Les pourcentages sont recalculés à chaque
  lecture à partir des compteurs de votes.
- `public/index.html` : formulaire de création (options dynamiques), liste des sondages avec barres
  de progression et bouton "Voter" par option.

## Lancer
Depuis le dossier `polling-voting-app/` :
```
javac -d out src/VotingServer.java
java -cp out VotingServer
```
Puis ouvrir http://localhost:8085
