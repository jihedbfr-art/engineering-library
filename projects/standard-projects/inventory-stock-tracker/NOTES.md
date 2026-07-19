# inventory-stock-tracker

Suivi de stock par article avec historique de mouvements (entree/sortie) et seuil de reappro.
Projet de la bibliotheque `standard-projects`, autonome.

## Stack (verifiee dans pom.xml)

- Spring Boot 3.2.5, Java 17, groupId `com.jihedapps`
- spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation
- spring-boot-starter-thymeleaf (pages HTML : liste des articles, detail + mouvements)
- H2 en fichier (`./data/inventorydb`), pas de H2 en memoire
- Pas de frontend JS : Thymeleaf cote serveur uniquement

## Commandes

```
mvn compile          # verifier la compilation
mvn test              # lancer les tests (ItemServiceTest, StockMovementServiceTest)
mvn spring-boot:run   # lancer l'app sur le port 8085
mvn package            # produire le jar (target/inventory-stock-tracker-0.1.0.jar)
```

## Fichiers cles

- `entity/Item.java` : SKU unique, quantite, seuil de reappro, `isLowStock()` derive
  (`quantity <= reorderThreshold`), pas persiste comme colonne separee
- `entity/StockMovement.java`, `MovementType.java` (`IN`/`OUT`) : chaque mouvement reference
  un `Item` et porte sa propre date (`timestamp`, defaut a la creation)
- `repository/ItemRepository.java` : `findLowStock()` en `@Query` JPQL (comparaison entre
  deux colonnes de la meme entite, pas exprimable en requete derivee Spring Data)
- `service/ItemService.java` : CRUD article, rejette un SKU deja utilise a la creation
  (`IllegalArgumentException`, pas encore mappee a un code HTTP dedie — voir "Points d'attention")
- `service/StockMovementService.java` : **seul** point d'entree qui modifie `Item.quantity`,
  dans la meme transaction que l'enregistrement du mouvement ; leve `InsufficientStockException`
  (409) si une sortie depasse le stock disponible
- `controller/ItemController.java`, `StockMovementController.java` : API REST JSON
- `controller/InventoryBoardController.java` : pages HTML (`/` liste, `/items/{id}` detail
  + formulaire de mouvement)
- `exception/GlobalExceptionHandler.java` : mappe `ResourceNotFoundException` -> 404,
  `InsufficientStockException` -> 409, erreurs de validation -> 400

## Points d'attention

- `ItemService.create()` leve une `IllegalArgumentException` simple pour un SKU dupplique,
  pas encore une exception dediee mappee proprement dans `GlobalExceptionHandler` — elle
  remonte donc en 500 par defaut plutot qu'en 409/400. A corriger si ce projet sert de base
  a autre chose de plus serieux qu'un exemple de bibliotheque ; laisse tel quel ici,
  assume comme limite connue plutot que corrige a la va-vite.
- Le formulaire HTML de mouvement (`InventoryBoardController.recordMovement`) ne catch pas
  `InsufficientStockException` avant redirection : une sortie refusee remonte l'erreur JSON
  du `GlobalExceptionHandler` telle quelle plutot qu'un message utilisateur sur la page —
  fonctionne, mais l'experience n'est pas soignee sur ce chemin precis.
- `Item.quantity` ne doit jamais etre modifie hors de `StockMovementService.record()` —
  c'est ce qui garantit que l'historique des mouvements reste la source de verite.
