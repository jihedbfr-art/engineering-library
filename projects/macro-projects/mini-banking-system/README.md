# Mini Banking System

> Core banking backend sécurisé, transactionnel et conforme aux exigences d'audit financier. Développé avec Spring Boot 3.2.5, Java 17 et Spring Data JPA.

## Architecture & Garanties Financières

Ce projet implémente un moteur bancaire minimaliste orienté entreprise garantissant la cohérence stricte des opérations financières et de l'historique comptable.

- **Stack** : Java 17, Spring Boot 3.2.5, Spring Data JPA, Base de données H2, Validation Spring Boot.
- **Règles Métier Financières** :
  - **Gestion du découvert configurabilité** : Retraits et virements autorisés jusqu'au plafond `balance + overdraftLimit`.
  - **Atomicités des virements (`@Transactional`)** : Débit du compte source et crédit du compte destinataire exécutés au sein d'une transaction unique. En cas d'erreur, annulation intégrale (`rollback`).
  - **Journal d'Audit Immuable (Append-Only Audit Log)** : Chaque opération (réussie ou échouée) génère un enregistrement de transaction immuable avec horodatage et motif d'échec en cas d'erreur.
  - **Gestion globale des exceptions REST** : Réponses HTTP normalisées (`402 Payment Required` si solde insuffisant, `404 Not Found`, `400 Bad Request`).

## Structure du Projet

```text
mini-banking-system/
├── src/main/java/com/jihedapps/banking/
│   ├── controller/      # Endpoints REST (Accounts, Transactions, History)
│   ├── dto/             # Objets de transfert de données et validations
│   ├── entity/          # Entités JPA (Account, Transaction, Enums)
│   ├── exception/       # Exceptions métier et GlobalExceptionHandler
│   ├── repository/      # Repositories JPA avec requêtes JPQL
│   └── service/         # Logique métier transactionnelle
└── src/test/java/       # Tests d'intégration JUnit 5 / Spring Boot Test
```

## Démarrage Rapide

### Compiler et exécuter les tests

```bash
mvn clean test
```

### Lancer l'application

```bash
mvn spring-boot:run
```

L'application démarre sur `http://localhost:8080`. La console H2 est accessible sur `http://localhost:8080/h2-console`.

## Endpoints API

| Méthode | Endpoint | Description | HTTP Code Succès |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/accounts` | Création d'un compte bancaire | `201 Created` |
| `GET` | `/api/accounts/{accountNumber}` | Solde et détails d'un compte | `200 OK` |
| `POST` | `/api/transactions/deposit` | Dépôt sur un compte | `200 OK` |
| `POST` | `/api/transactions/withdraw` | Retrait d'un compte | `200 OK` |
| `POST` | `/api/transactions/transfer` | Virement atomique entre 2 comptes | `200 OK` |
| `GET` | `/api/transactions/{accountNumber}/history` | Historique complet d'audit d'un compte | `200 OK` |
