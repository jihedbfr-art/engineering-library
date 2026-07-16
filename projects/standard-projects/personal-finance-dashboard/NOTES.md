# personal-finance-dashboard

Suivi de transactions personnelles et budgets par categorie, avec alerte de
depassement. API REST + page HTML de resume mensuel.

## Stack (verifiee)

- Spring Boot 3.2.5, Java 17, Maven 3.9.12, groupId `com.jihedapps`.
- Spring Web, Spring Data JPA, Bean Validation.
- H2 en mode fichier (`./data/financedb`), pas de serveur externe requis.
- Frontend : page statique `src/main/resources/static/index.html` (fetch API),
  pas d'Angular/framework front — choix assume pour le budget du projet.

## Commandes

```bash
mvn compile          # compiler
mvn test              # tests (aucun test unitaire dedie pour l'instant)
mvn spring-boot:run    # lancer sur http://localhost:8081
mvn package            # produire le jar executable
```

## Fichiers cles

- `entity/Transaction.java`, `entity/Budget.java` — modele de donnees.
- `service/SummaryService.java` — calcul du resume mensuel par categorie et
  detection de depassement de budget.
- `controller/TransactionController.java`, `controller/BudgetController.java` — CRUD REST.
- `controller/SummaryController.java` — endpoint `/api/summary/monthly`.
- `resources/static/index.html` — page de resume consommant l'API.
- `resources/application.properties` — config H2 fichier, port 8081.

## Regles specifiques

- Toute nouvelle entite financiere doit avoir son repository Spring Data JPA
  dedie, pas de requetes SQL natives sauf besoin justifie.
- Le calcul de depassement de budget se fait uniquement sur les depenses
  (`TransactionType.EXPENSE`), jamais sur le solde net.
- Ne pas ajouter de dependance de securite/JWT dans ce projet : il reste
  volontairement mono-utilisateur sans authentification.
