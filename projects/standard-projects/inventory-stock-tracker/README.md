# inventory-stock-tracker

Suivi de stock par article : chaque article a une quantite courante et un seuil de reappro,
et chaque entree/sortie passe par un mouvement de stock trace (date, type, quantite, motif).
Projet de la bibliotheque `standard-projects`, autonome, sans lien avec les autres projets
du repertoire.

## Pourquoi un mouvement plutot qu'un simple champ quantite modifiable

Le champ `Item.quantity` n'est jamais modifie directement depuis un formulaire ou un `PUT`.
La seule facon de le faire varier est d'enregistrer un `StockMovement` (`IN`/`OUT`), qui met
a jour la quantite dans la meme transaction. Ca donne un historique complet et coherent, et
ca evite le cas classique ou la quantite affichee ne correspond a rien de tracable — le genre
de decalage qui rend un audit de stock impossible a expliquer six mois plus tard.

## Lancer

```
mvn spring-boot:run
```

L'application demarre sur `http://localhost:8085`.

- Liste des articles avec surbrillance des articles sous le seuil de reappro : `http://localhost:8085/`
- Detail d'un article + historique des mouvements + formulaire d'entree/sortie : `http://localhost:8085/items/{id}`
- Console H2 : `http://localhost:8085/h2-console` (JDBC URL `jdbc:h2:file:./data/inventorydb`, user `sa`, pas de mot de passe)

Aucun article n'est cree par defaut : passez par l'API REST pour en creer un.

## API

```bash
# Creer un article
curl -X POST http://localhost:8085/api/items \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-001","name":"Cable HDMI 2m","category":"cables","quantity":20,"reorderThreshold":5}'

# Lister les articles (ou seulement ceux sous le seuil de reappro)
curl http://localhost:8085/api/items
curl "http://localhost:8085/api/items?lowStock=true"

# Enregistrer une sortie de stock (rejetee avec 409 si quantite insuffisante)
curl -X POST http://localhost:8085/api/items/1/movements \
  -H "Content-Type: application/json" \
  -d '{"type":"OUT","quantity":3,"reason":"commande client #4521"}'

# Historique des mouvements d'un article
curl http://localhost:8085/api/items/1/movements
```

## Tests

```
mvn test
```

`ItemServiceTest` et `StockMovementServiceTest` couvrent la logique metier (SKU dupliques,
mise a jour de quantite via mouvement, rejet d'une sortie sans stock suffisant) avec des
repositories mockes.
