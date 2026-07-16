# personal-finance-dashboard

Suivi de transactions personnelles (revenus/depenses) avec budgets par categorie et
alerte de depassement. API REST + page HTML de resume mensuel.

## Architecture

- Spring Boot 3.2.5 / Java 17, groupId `com.jihedapps`.
- Persistance : Spring Data JPA + H2 en mode fichier (`./data/financedb.mv.db`), les
  donnees survivent a un redemarrage.
- Frontend : une page statique `static/index.html` qui consomme l'API via `fetch`
  (pas de framework front, choix assume pour rester dans le budget du lot).
- Entites : `Transaction` (montant, categorie, date, type INCOME/EXPENSE, description),
  `Budget` (categorie, plafond mensuel).

## Lancer le projet

```bash
mvn spring-boot:run
```

L'application demarre sur `http://localhost:8081`.

- Page de resume : `http://localhost:8081/index.html`
- Console H2 (debug) : `http://localhost:8081/h2-console`
  (JDBC URL `jdbc:h2:file:./data/financedb`, user `sa`, mot de passe vide)

## Endpoints principaux

| Methode | URL | Description |
|---|---|---|
| GET | `/api/transactions` | Liste toutes les transactions |
| GET | `/api/transactions/{id}` | Detail d'une transaction |
| POST | `/api/transactions` | Creer une transaction |
| PUT | `/api/transactions/{id}` | Modifier une transaction |
| DELETE | `/api/transactions/{id}` | Supprimer une transaction |
| GET | `/api/budgets` | Liste les budgets |
| POST | `/api/budgets` | Creer un budget |
| PUT | `/api/budgets/{id}` | Modifier un budget |
| DELETE | `/api/budgets/{id}` | Supprimer un budget |
| GET | `/api/summary/monthly?year=YYYY&month=M` | Resume mensuel par categorie, avec flag `overBudget` si les depenses depassent le plafond du budget de la categorie |

### Exemple de creation de transaction

```json
POST /api/transactions
{
  "amount": 45.90,
  "category": "Alimentation",
  "date": "2026-07-10",
  "type": "EXPENSE",
  "description": "Courses"
}
```

## Limitations connues

- Pas d'authentification : projet mono-utilisateur.
- Le resume mensuel ne gere qu'un mois a la fois (pas d'agregation multi-mois).
- Validation minimale (Bean Validation), pas de gestion d'erreurs globale (`@ControllerAdvice`).
