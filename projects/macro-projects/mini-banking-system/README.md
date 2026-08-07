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


# System Architecture & Technical Specifications: Mini Banking System

Ce document présente l'analyse d'architecture et de conception logicielle du projet `mini-banking-system`. Réalisé avec Spring Boot 3.2.5 et Java 17, ce micro-service bancaire assure la gestion transactionnelle atomique des comptes et la traçabilité immuable des opérations financières.

---

### 1. Architecture Overview (C4 Container)

Le schéma C4 Container illustre la vue d'ensemble de l'application `mini-banking-system`. L'utilisateur ou client HTTP communique avec le conteneur Spring Boot via des requêtes REST/JSON sur le port 8080. En interne, l'application est structurée en contrôleurs, services applicatifs et dépôts de données accédant à une base de données H2 en mémoire (`jdbc:h2:mem:bankingdb`), avec la console H2 exposée sur `/h2-console`.

```mermaid
graph TB
    subgraph ClientLayer ["Couche Client"]
        Client["Client REST / HTTP<br/>(Postman, App Web/Mobile)"]
    end

    subgraph SpringBootApp ["Application Spring Boot (Port 8080)"]
        direction TB
        Controller["BankingController<br/>(REST Endpoints /api)"]
        Services["Services Métier<br/>(BankingService, TransactionAuditService)"]
        Repositories["Couche Accès Données<br/>(AccountRepository, TransactionRepository)"]
        
        Controller --> Services
        Services --> Repositories
    end

    subgraph DatabaseLayer ["Stockage de Données"]
        H2DB[("H2 In-Memory DB<br/>(jdbc:h2:mem:bankingdb)")]
        H2Console["Console H2<br/>(/h2-console)"]
    end

    Client -- "HTTP REST / JSON" --> Controller
    Repositories -- "Spring Data JPA / SQL" --> H2DB
    Client -. "Navigateur Web" .-> H2Console
    H2Console -. "Accès direct" .-> H2DB
```

---

### 2. Package Diagram

Le diagramme de paquetages représente la structure modulaire de l'application sous le paquetage racine `com.jihedapps.banking`. La communication suit une architecture en couches strictes où `controller` dépend de `dto` et `service`, `service` orchestre la logique en dépendant de `repository`, `entity`, `dto` et `exception`, et `repository` interagit directement avec `entity`.

```mermaid
graph TD
    subgraph com.jihedapps.banking ["com.jihedapps.banking"]
        direction TB
        
        PK_Root["BankingApplication"]
        
        subgraph controller ["controller"]
            BankingController["BankingController"]
        end
        
        subgraph dto ["dto"]
            DTOs["CreateAccountRequest<br/>TransactionRequest<br/>TransferRequest<br/>AccountResponse<br/>TransactionResponse"]
        end
        
        subgraph service ["service"]
            BankingService["BankingService"]
            TransactionAuditService["TransactionAuditService"]
        end
        
        subgraph repository ["repository"]
            AccountRepository["AccountRepository"]
            TransactionRepository["TransactionRepository"]
        end
        
        subgraph entity ["entity"]
            Account["Account"]
            Transaction["Transaction"]
            Enums["TransactionType<br/>TransactionStatus"]
        end
        
        subgraph exception ["exception"]
            Exceptions["AccountNotFoundException<br/>InsufficientBalanceException<br/>InvalidTransactionException<br/>GlobalExceptionHandler"]
        end
        
        controller --> dto
        controller --> service
        service --> dto
        service --> entity
        service --> repository
        service --> exception
        repository --> entity
        dto --> entity
    end
```

---

### 3. Layer Diagram

Le diagramme en couches met en évidence la séparation des responsabilités au sein de l'application (4 tiers). La couche Web réceptionne et valide les DTOs, la couche Service gère l'atomicité transactionnelle et la journalisation d'audit immuable avec propagation `REQUIRES_NEW`, la couche Persistence définit les interfaces `JpaRepository` avec requêtes JPQL sur mesure, et la couche Base de Données assure le stockage persistant H2.

```mermaid
graph TB
    subgraph PresentationLayer ["1. Presentation Layer (Web / REST)"]
        BC["BankingController"]
        GEH["GlobalExceptionHandler"]
        DTO["DTOs (CreateAccountRequest, TransferRequest, etc.)"]
    end

    subgraph ServiceLayer ["2. Business / Service Layer"]
        BS["BankingService (@Transactional)"]
        TAS["TransactionAuditService (@Transactional REQUIRES_NEW)"]
    end

    subgraph PersistenceLayer ["3. Data Access / Repository Layer"]
        AR["AccountRepository (JpaRepository)"]
        TR["TransactionRepository (JpaRepository + JPQL)"]
    end

    subgraph DatabaseLayer ["4. Infrastructure / Database Layer"]
        H2[("H2 In-Memory DB (Hibernate / DDL Auto)")]
    end

    BC --> BS
    BC --> DTO
    GEH ..-> BC : "Intercepte exceptions"
    BS --> TAS
    BS --> AR
    BS --> TR
    TAS --> TR
    AR --> H2
    TR --> H2
```

---

### 4. Component Diagram

Le diagramme de composants décrit la structure interne des beans Spring de l'application et leurs dépendances d'injection de constructeur. `BankingController` dépend directement de `BankingService`. `BankingService` dépend d'`AccountRepository`, `TransactionRepository` et `TransactionAuditService`. `TransactionAuditService` possède sa propre dépendance vers `TransactionRepository` afin d'exécuter l'enregistrement des audits d'échec isolés de la transaction principale.

```mermaid
graph LR
    subgraph Controllers ["Contrôleurs REST"]
        [BankingController]
        [GlobalExceptionHandler]
    end

    subgraph Services ["Services Métier"]
        [BankingService]
        [TransactionAuditService]
    end

    subgraph Repositories ["Dépôts Spring Data JPA"]
        [AccountRepository]
        [TransactionRepository]
    end

    subgraph Entities ["Entités JPA & Modèle"]
        [Account Entity]
        [Transaction Entity]
    end

    [BankingController] -->|Injecte| [BankingService]
    [BankingService] -->|Injecte| [AccountRepository]
    [BankingService] -->|Injecte| [TransactionRepository]
    [BankingService] -->|Injecte| [TransactionAuditService]
    [TransactionAuditService] -->|Injecte| [TransactionRepository]
    
    [AccountRepository] ..->|Gère| [Account Entity]
    [TransactionRepository] ..->|Gère| [Transaction Entity]
    [GlobalExceptionHandler] ..->|Intercepte| [BankingController]
```

---

### 5. ERD (Entity Relationship Diagram)

Le diagramme Entity-Relationship (ERD) montre le schéma relationnel de la base de données H2 généré par Hibernate à partir des annotations Jakarta Persistence (`@Entity`, `@Table`). La table `accounts` contient les informations de solde et de découvert autorisé. La table `transactions` enregistre chaque mouvement financier de manière immuable avec le type (`DEPOSIT`, `WITHDRAWAL`, `TRANSFER`), le statut (`SUCCESS`, `FAILED`) et les numéros de comptes source et cible sous forme de références logiques (`source_account_number`, `target_account_number`).

```mermaid
erDiagram
    accounts {
        BIGINT id PK "IDENTITY"
        VARCHAR account_number UK "NOT NULL"
        VARCHAR owner_name "NOT NULL"
        DECIMAL balance "19, 4, NOT NULL"
        DECIMAL overdraft_limit "19, 4, NOT NULL"
        TIMESTAMP created_at "NOT NULL"
    }

    transactions {
        BIGINT id PK "IDENTITY"
        VARCHAR source_account_number "NULLABLE"
        VARCHAR target_account_number "NULLABLE"
        DECIMAL amount "19, 4, NOT NULL"
        VARCHAR type "NOT NULL (DEPOSIT|WITHDRAWAL|TRANSFER)"
        VARCHAR status "NOT NULL (SUCCESS|FAILED)"
        VARCHAR failure_reason "NULLABLE"
        TIMESTAMP timestamp "NOT NULL"
    }

    accounts ||--o{ transactions : "source_account_number / target_account_number (Logical Ref)"
```

---

### 6. Class Diagram UML

Le diagramme de classes UML présente la structure des entités métier principales (`Account`, `Transaction`), leurs énumérations associées (`TransactionType`, `TransactionStatus`), ainsi que les services et dépôts qui manipulent ce domaine. La méthode métier `canWithdraw(amount)` centralise la vérification du découvert autorisé directement dans l'entité `Account`.

```mermaid
classDiagram
    class Account {
        -Long id
        -String accountNumber
        -String ownerName
        -BigDecimal balance
        -BigDecimal overdraftLimit
        -LocalDateTime createdAt
        +Account()
        +Account(accountNumber, ownerName, balance, overdraftLimit)
        +canWithdraw(amount: BigDecimal) boolean
        +getId() Long
        +getAccountNumber() String
        +setAccountNumber(accountNumber: String) void
        +getOwnerName() String
        +setOwnerName(ownerName: String) void
        +getBalance() BigDecimal
        +setBalance(balance: BigDecimal) void
        +getOverdraftLimit() BigDecimal
        +setOverdraftLimit(overdraftLimit: BigDecimal) void
        +getCreatedAt() LocalDateTime
    }

    class Transaction {
        -Long id
        -String sourceAccountNumber
        -String targetAccountNumber
        -BigDecimal amount
        -TransactionType type
        -TransactionStatus status
        -String failureReason
        -LocalDateTime timestamp
        +Transaction()
        +Transaction(source, target, amount, type, status, failureReason)
        +getId() Long
        +getSourceAccountNumber() String
        +getTargetAccountNumber() String
        +getAmount() BigDecimal
        +getType() TransactionType
        +getStatus() TransactionStatus
        +getFailureReason() String
        +getTimestamp() LocalDateTime
    }

    class TransactionType {
        <<enumeration>>
        DEPOSIT
        WITHDRAWAL
        TRANSFER
    }

    class TransactionStatus {
        <<enumeration>>
        SUCCESS
        FAILED
    }

    class BankingService {
        -AccountRepository accountRepository
        -TransactionRepository transactionRepository
        -TransactionAuditService auditService
        +createAccount(req: CreateAccountRequest) AccountResponse
        +getAccount(accountNumber: String) AccountResponse
        +deposit(req: TransactionRequest) TransactionResponse
        +withdraw(req: TransactionRequest) TransactionResponse
        +transfer(req: TransferRequest) TransactionResponse
        +getAccountHistory(accountNumber: String) List~TransactionResponse~
        -findAccountOrThrow(accountNumber: String) Account
    }

    class TransactionAuditService {
        -TransactionRepository transactionRepository
        +logTransaction(source, target, amount, type, status, failureReason) Transaction
    }

    class AccountRepository {
        <<interface>>
        +findByAccountNumber(accountNumber: String) Optional~Account~
        +existsByAccountNumber(accountNumber: String) boolean
    }

    class TransactionRepository {
        <<interface>>
        +findByAccountNumber(accNum: String) List~Transaction~
    }

    Transaction --> TransactionType : uses
    Transaction --> TransactionStatus : uses
    BankingService --> AccountRepository : relies on
    BankingService --> TransactionRepository : relies on
    BankingService --> TransactionAuditService : relies on
    TransactionAuditService --> TransactionRepository : relies on
    AccountRepository ..> Account : manages
    TransactionRepository ..> Transaction : manages
```

---

### 7. Sequence Diagram (POST /api/transactions/transfer)

Le diagramme de séquence retrace le flux d'exécution complet lors de l'appel au service de virement atomique (`POST /api/transactions/transfer`). Il illustre la double branche : en cas de solde insuffisant (incluant le découvert autorisée), l'opération est auditée avec le statut `FAILED` au moyen de la transaction autonome `TransactionAuditService` (`REQUIRES_NEW`), puis une exception `InsufficientBalanceException` est levée et convertie en réponse HTTP `402 Payment Required` par le `GlobalExceptionHandler`. En cas de succès, les deux comptes sont mis à jour au sein de la transaction courante et l'audit enregistre `SUCCESS`.

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Controller as BankingController
    participant Service as BankingService
    participant AccRepo as AccountRepository
    participant Audit as TransactionAuditService
    participant TxRepo as TransactionRepository
    participant ExceptionHandler as GlobalExceptionHandler

    Client->>Controller: POST /api/transactions/transfer (TransferRequest)
    Note over Controller: Validation Bean Jakarta (@Valid)
    
    alt Validation Echouée
        Controller-->>ExceptionHandler: MethodArgumentNotValidException
        ExceptionHandler-->>Client: 400 Bad Request (JSON Error)
    else Validation Réussie
        Controller->>Service: transfer(req) [@Transactional]
        
        alt Source == Target
            Service-->>ExceptionHandler: InvalidTransactionException
            ExceptionHandler-->>Client: 400 Bad Request
        end
        
        Service->>AccRepo: findByAccountNumber(source)
        AccRepo-->>Service: Account source
        Service->>AccRepo: findByAccountNumber(target)
        AccRepo-->>Service: Account target
        
        Service->>Service: source.canWithdraw(amount)
        
        alt Solde + Overdraft Insuffisant
            Service->>Audit: logTransaction(source, target, amount, TRANSFER, FAILED, "Solde source insuffisant")
            Note over Audit: [@Transactional(propagation = REQUIRES_NEW)]
            Audit->>TxRepo: save(Transaction FAILED)
            TxRepo-->>Audit: Transaction saved
            Audit-->>Service: Transaction logged
            Service-->>ExceptionHandler: throw InsufficientBalanceException
            ExceptionHandler-->>Client: 402 Payment Required (JSON Error)
        else Solde Suffisant
            Service->>Service: source.setBalance(balance - amount)
            Service->>Service: target.setBalance(balance + amount)
            Service->>AccRepo: save(source)
            Service->>AccRepo: save(target)
            Service->>Audit: logTransaction(source, target, amount, TRANSFER, SUCCESS, null)
            Audit->>TxRepo: save(Transaction SUCCESS)
            TxRepo-->>Audit: Transaction saved
            Audit-->>Service: Transaction logged
            Service-->>Controller: TransactionResponse
            Controller-->>Client: 200 OK (TransactionResponse JSON)
        end
    end
```

---

### 8. State Diagram (Transaction Status)

Le diagramme d'états décrit le cycle de vie d'une transaction dans l'application. Une transaction est instanciée lors de la réception d'une demande de dépôt, retrait ou virement, puis passe directement à un état final persistant immuable (`SUCCESS` ou `FAILED`) selon les résultats de la validation des règles de solde et de découvert.

```mermaid
stateDiagram-v2
    [*] --> Submitted : Envoi requête (Deposit / Withdraw / Transfer)
    
    state Submitted {
        [*] --> ValidatingAccount : Vérification existence comptes
        ValidatingAccount --> CheckingRules : Comptes valides
        CheckingRules --> EvaluatingBalance : Contrôle solde + overdraftLimit
    }

    Submitted --> FAILED : Solde ou Overdraft Insuffisant / Erreur Métier
    Submitted --> SUCCESS : Solde suffisant & Mise à jour effectuée

    state FAILED {
        [*] --> LoggedFailed : Enregistrement immuable avec failureReason\n(via REQUIRES_NEW)
    }

    state SUCCESS {
        [*] --> LoggedSuccess : Enregistrement immuable dans DB
    }

    FAILED --> [*]
    SUCCESS --> [*]
```

---

### 9. Security & Exception Handling Flow

Le diagramme de flux de sécurité et de gestion des erreurs illustre l'absence de Spring Security explicite (selon le `pom.xml`) et se concentre sur le pipeline de validation de sécurité applicative, la gestion fine des exceptions et l'isolation transactionnelle. Les erreurs de validation d'entrée ou de solde sont interceptées par le `GlobalExceptionHandler` pour garantir une réponse REST standardisée.

```mermaid
flowchart TD
    Req["Requête HTTP Entrante"] --> ValFilter{"Validation DTO (@Valid)"}
    
    ValFilter -- "Invalide (ex: Montant négatif)" --> ErrVal["MethodArgumentNotValidException"]
    ErrVal --> GEH["GlobalExceptionHandler"]
    GEH --> Resp400["400 Bad Request (JSON)"]

    ValFilter -- "Valide" --> ExecSvc["Exécution BankingService"]
    
    ExecSvc --> ChkAcc{"Compte Existant ?"}
    ChkAcc -- "Non" --> ErrNotFound["AccountNotFoundException"]
    ErrNotFound --> GEH
    GEH --> Resp404["404 Not Found (JSON)"]

    ChkAcc -- "Oui" --> ChkBal{"canWithdraw() == true ?"}
    ChkBal -- "Non" --> AuditFail["TransactionAuditService.logTransaction(FAILED)"]
    AuditFail --> ErrBal["InsufficientBalanceException"]
    ErrBal --> GEH
    GEH --> Resp402["402 Payment Required (JSON)"]

    ChkBal -- "Oui" --> ExecTx["Mise à jour Solde + Audit SUCCESS"]
    ExecTx --> Resp200["200 OK / 201 Created (JSON Response)"]
```

---

### 10. Deployment Diagram

Le diagramme de déploiement montre l'environnement d'exécution physique et logique de l'application `mini-banking-system`. L'application Spring Boot s'exécute dans un processus JVM Java 17 embarquant un serveur Web Tomcat sur le port 8080 et une base de données H2 `in-memory`.

```mermaid
deployment
    node "Machine Hôte / Serveur Applicatif" {
        node "JVM 17 Environment" {
            artifact "mini-banking-system-1.0.0-SNAPSHOT.jar" {
                component "Spring Boot 3.2.5 Application Engine"
                component "Tomcat Embedded Web Server (Port 8080)"
                component "H2 Engine (In-Memory JDBC)"
            }
        }
    }

    node "Clients Externes" {
        component "Postman / Client HTTP"
        component "Navigateur Web (Console H2)"
    }

    "Postman / Client HTTP" -- "HTTP / REST (Port 8080)" --> "Tomcat Embedded Web Server (Port 8080)"
    "Navigateur Web (Console H2)" -- "HTTP / /h2-console" --> "Tomcat Embedded Web Server (Port 8080)"
    "Spring Boot 3.2.5 Application Engine" -- "JDBC In-Memory" --> "H2 Engine (In-Memory JDBC)"
```


# System Architecture & Technical Specifications: Mini Banking System

Ce document présente l'analyse d'architecture et de conception logicielle du projet `mini-banking-system`. Réalisé avec Spring Boot 3.2.5 et Java 17, ce micro-service bancaire assure la gestion transactionnelle atomique des comptes et la traçabilité immuable des opérations financières.

---

### 1. Architecture Overview (C4 Container)

Le schéma C4 Container illustre la vue d'ensemble de l'application `mini-banking-system`. L'utilisateur ou client HTTP communique avec le conteneur Spring Boot via des requêtes REST/JSON sur le port 8080. En interne, l'application est structurée en contrôleurs, services applicatifs et dépôts de données accédant à une base de données H2 en mémoire (`jdbc:h2:mem:bankingdb`), avec la console H2 exposée sur `/h2-console`.

```mermaid
graph TB
    subgraph ClientLayer ["Couche Client"]
        Client["Client REST / HTTP<br/>(Postman, App Web/Mobile)"]
    end

    subgraph SpringBootApp ["Application Spring Boot (Port 8080)"]
        direction TB
        Controller["BankingController<br/>(REST Endpoints /api)"]
        Services["Services Métier<br/>(BankingService, TransactionAuditService)"]
        Repositories["Couche Accès Données<br/>(AccountRepository, TransactionRepository)"]
        
        Controller --> Services
        Services --> Repositories
    end

    subgraph DatabaseLayer ["Stockage de Données"]
        H2DB[("H2 In-Memory DB<br/>(jdbc:h2:mem:bankingdb)")]
        H2Console["Console H2<br/>(/h2-console)"]
    end

    Client -- "HTTP REST / JSON" --> Controller
    Repositories -- "Spring Data JPA / SQL" --> H2DB
    Client -. "Navigateur Web" .-> H2Console
    H2Console -. "Accès direct" .-> H2DB
```

---

### 2. Package Diagram

Le diagramme de paquetages représente la structure modulaire de l'application sous le paquetage racine `com.jihedapps.banking`. La communication suit une architecture en couches strictes où `controller` dépend de `dto` et `service`, `service` orchestre la logique en dépendant de `repository`, `entity`, `dto` et `exception`, et `repository` interagit directement avec `entity`.

```mermaid
graph TD
    subgraph com.jihedapps.banking ["com.jihedapps.banking"]
        direction TB
        
        PK_Root["BankingApplication"]
        
        subgraph controller ["controller"]
            BankingController["BankingController"]
        end
        
        subgraph dto ["dto"]
            DTOs["CreateAccountRequest<br/>TransactionRequest<br/>TransferRequest<br/>AccountResponse<br/>TransactionResponse"]
        end
        
        subgraph service ["service"]
            BankingService["BankingService"]
            TransactionAuditService["TransactionAuditService"]
        end
        
        subgraph repository ["repository"]
            AccountRepository["AccountRepository"]
            TransactionRepository["TransactionRepository"]
        end
        
        subgraph entity ["entity"]
            Account["Account"]
            Transaction["Transaction"]
            Enums["TransactionType<br/>TransactionStatus"]
        end
        
        subgraph exception ["exception"]
            Exceptions["AccountNotFoundException<br/>InsufficientBalanceException<br/>InvalidTransactionException<br/>GlobalExceptionHandler"]
        end
        
        controller --> dto
        controller --> service
        service --> dto
        service --> entity
        service --> repository
        service --> exception
        repository --> entity
        dto --> entity
    end
```

---

### 3. Layer Diagram

Le diagramme en couches met en évidence la séparation des responsabilités au sein de l'application (4 tiers). La couche Web réceptionne et valide les DTOs, la couche Service gère l'atomicité transactionnelle et la journalisation d'audit immuable avec propagation `REQUIRES_NEW`, la couche Persistence définit les interfaces `JpaRepository` avec requêtes JPQL sur mesure, et la couche Base de Données assure le stockage persistant H2.

```mermaid
graph TB
    subgraph PresentationLayer ["1. Presentation Layer (Web / REST)"]
        BC["BankingController"]
        GEH["GlobalExceptionHandler"]
        DTO["DTOs (CreateAccountRequest, TransferRequest, etc.)"]
    end

    subgraph ServiceLayer ["2. Business / Service Layer"]
        BS["BankingService (@Transactional)"]
        TAS["TransactionAuditService (@Transactional REQUIRES_NEW)"]
    end

    subgraph PersistenceLayer ["3. Data Access / Repository Layer"]
        AR["AccountRepository (JpaRepository)"]
        TR["TransactionRepository (JpaRepository + JPQL)"]
    end

    subgraph DatabaseLayer ["4. Infrastructure / Database Layer"]
        H2[("H2 In-Memory DB (Hibernate / DDL Auto)")]
    end

    BC --> BS
    BC --> DTO
    GEH ..-> BC : "Intercepte exceptions"
    BS --> TAS
    BS --> AR
    BS --> TR
    TAS --> TR
    AR --> H2
    TR --> H2
```

---

### 4. Component Diagram

Le diagramme de composants décrit la structure interne des beans Spring de l'application et leurs dépendances d'injection de constructeur. `BankingController` dépend directement de `BankingService`. `BankingService` dépend d'`AccountRepository`, `TransactionRepository` et `TransactionAuditService`. `TransactionAuditService` possède sa propre dépendance vers `TransactionRepository` afin d'exécuter l'enregistrement des audits d'échec isolés de la transaction principale.

```mermaid
graph LR
    subgraph Controllers ["Contrôleurs REST"]
        [BankingController]
        [GlobalExceptionHandler]
    end

    subgraph Services ["Services Métier"]
        [BankingService]
        [TransactionAuditService]
    end

    subgraph Repositories ["Dépôts Spring Data JPA"]
        [AccountRepository]
        [TransactionRepository]
    end

    subgraph Entities ["Entités JPA & Modèle"]
        [Account Entity]
        [Transaction Entity]
    end

    [BankingController] -->|Injecte| [BankingService]
    [BankingService] -->|Injecte| [AccountRepository]
    [BankingService] -->|Injecte| [TransactionRepository]
    [BankingService] -->|Injecte| [TransactionAuditService]
    [TransactionAuditService] -->|Injecte| [TransactionRepository]
    
    [AccountRepository] ..->|Gère| [Account Entity]
    [TransactionRepository] ..->|Gère| [Transaction Entity]
    [GlobalExceptionHandler] ..->|Intercepte| [BankingController]
```

---

### 5. ERD (Entity Relationship Diagram)

Le diagramme Entity-Relationship (ERD) montre le schéma relationnel de la base de données H2 généré par Hibernate à partir des annotations Jakarta Persistence (`@Entity`, `@Table`). La table `accounts` contient les informations de solde et de découvert autorisé. La table `transactions` enregistre chaque mouvement financier de manière immuable avec le type (`DEPOSIT`, `WITHDRAWAL`, `TRANSFER`), le statut (`SUCCESS`, `FAILED`) et les numéros de comptes source et cible sous forme de références logiques (`source_account_number`, `target_account_number`).

```mermaid
erDiagram
    accounts {
        BIGINT id PK "IDENTITY"
        VARCHAR account_number UK "NOT NULL"
        VARCHAR owner_name "NOT NULL"
        DECIMAL balance "19, 4, NOT NULL"
        DECIMAL overdraft_limit "19, 4, NOT NULL"
        TIMESTAMP created_at "NOT NULL"
    }

    transactions {
        BIGINT id PK "IDENTITY"
        VARCHAR source_account_number "NULLABLE"
        VARCHAR target_account_number "NULLABLE"
        DECIMAL amount "19, 4, NOT NULL"
        VARCHAR type "NOT NULL (DEPOSIT|WITHDRAWAL|TRANSFER)"
        VARCHAR status "NOT NULL (SUCCESS|FAILED)"
        VARCHAR failure_reason "NULLABLE"
        TIMESTAMP timestamp "NOT NULL"
    }

    accounts ||--o{ transactions : "source_account_number / target_account_number (Logical Ref)"
```

---

### 6. Class Diagram UML

Le diagramme de classes UML présente la structure des entités métier principales (`Account`, `Transaction`), leurs énumérations associées (`TransactionType`, `TransactionStatus`), ainsi que les services et dépôts qui manipulent ce domaine. La méthode métier `canWithdraw(amount)` centralise la vérification du découvert autorisé directement dans l'entité `Account`.

```mermaid
classDiagram
    class Account {
        -Long id
        -String accountNumber
        -String ownerName
        -BigDecimal balance
        -BigDecimal overdraftLimit
        -LocalDateTime createdAt
        +Account()
        +Account(accountNumber, ownerName, balance, overdraftLimit)
        +canWithdraw(amount: BigDecimal) boolean
        +getId() Long
        +getAccountNumber() String
        +setAccountNumber(accountNumber: String) void
        +getOwnerName() String
        +setOwnerName(ownerName: String) void
        +getBalance() BigDecimal
        +setBalance(balance: BigDecimal) void
        +getOverdraftLimit() BigDecimal
        +setOverdraftLimit(overdraftLimit: BigDecimal) void
        +getCreatedAt() LocalDateTime
    }

    class Transaction {
        -Long id
        -String sourceAccountNumber
        -String targetAccountNumber
        -BigDecimal amount
        -TransactionType type
        -TransactionStatus status
        -String failureReason
        -LocalDateTime timestamp
        +Transaction()
        +Transaction(source, target, amount, type, status, failureReason)
        +getId() Long
        +getSourceAccountNumber() String
        +getTargetAccountNumber() String
        +getAmount() BigDecimal
        +getType() TransactionType
        +getStatus() TransactionStatus
        +getFailureReason() String
        +getTimestamp() LocalDateTime
    }

    class TransactionType {
        <<enumeration>>
        DEPOSIT
        WITHDRAWAL
        TRANSFER
    }

    class TransactionStatus {
        <<enumeration>>
        SUCCESS
        FAILED
    }

    class BankingService {
        -AccountRepository accountRepository
        -TransactionRepository transactionRepository
        -TransactionAuditService auditService
        +createAccount(req: CreateAccountRequest) AccountResponse
        +getAccount(accountNumber: String) AccountResponse
        +deposit(req: TransactionRequest) TransactionResponse
        +withdraw(req: TransactionRequest) TransactionResponse
        +transfer(req: TransferRequest) TransactionResponse
        +getAccountHistory(accountNumber: String) List~TransactionResponse~
        -findAccountOrThrow(accountNumber: String) Account
    }

    class TransactionAuditService {
        -TransactionRepository transactionRepository
        +logTransaction(source, target, amount, type, status, failureReason) Transaction
    }

    class AccountRepository {
        <<interface>>
        +findByAccountNumber(accountNumber: String) Optional~Account~
        +existsByAccountNumber(accountNumber: String) boolean
    }

    class TransactionRepository {
        <<interface>>
        +findByAccountNumber(accNum: String) List~Transaction~
    }

    Transaction --> TransactionType : uses
    Transaction --> TransactionStatus : uses
    BankingService --> AccountRepository : relies on
    BankingService --> TransactionRepository : relies on
    BankingService --> TransactionAuditService : relies on
    TransactionAuditService --> TransactionRepository : relies on
    AccountRepository ..> Account : manages
    TransactionRepository ..> Transaction : manages
```

---

### 7. Sequence Diagram (POST /api/transactions/transfer)

Le diagramme de séquence retrace le flux d'exécution complet lors de l'appel au service de virement atomique (`POST /api/transactions/transfer`). Il illustre la double branche : en cas de solde insuffisant (incluant le découvert autorisée), l'opération est auditée avec le statut `FAILED` au moyen de la transaction autonome `TransactionAuditService` (`REQUIRES_NEW`), puis une exception `InsufficientBalanceException` est levée et convertie en réponse HTTP `402 Payment Required` par le `GlobalExceptionHandler`. En cas de succès, les deux comptes sont mis à jour au sein de la transaction courante et l'audit enregistre `SUCCESS`.

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Controller as BankingController
    participant Service as BankingService
    participant AccRepo as AccountRepository
    participant Audit as TransactionAuditService
    participant TxRepo as TransactionRepository
    participant ExceptionHandler as GlobalExceptionHandler

    Client->>Controller: POST /api/transactions/transfer (TransferRequest)
    Note over Controller: Validation Bean Jakarta (@Valid)
    
    alt Validation Echouée
        Controller-->>ExceptionHandler: MethodArgumentNotValidException
        ExceptionHandler-->>Client: 400 Bad Request (JSON Error)
    else Validation Réussie
        Controller->>Service: transfer(req) [@Transactional]
        
        alt Source == Target
            Service-->>ExceptionHandler: InvalidTransactionException
            ExceptionHandler-->>Client: 400 Bad Request
        end
        
        Service->>AccRepo: findByAccountNumber(source)
        AccRepo-->>Service: Account source
        Service->>AccRepo: findByAccountNumber(target)
        AccRepo-->>Service: Account target
        
        Service->>Service: source.canWithdraw(amount)
        
        alt Solde + Overdraft Insuffisant
            Service->>Audit: logTransaction(source, target, amount, TRANSFER, FAILED, "Solde source insuffisant")
            Note over Audit: [@Transactional(propagation = REQUIRES_NEW)]
            Audit->>TxRepo: save(Transaction FAILED)
            TxRepo-->>Audit: Transaction saved
            Audit-->>Service: Transaction logged
            Service-->>ExceptionHandler: throw InsufficientBalanceException
            ExceptionHandler-->>Client: 402 Payment Required (JSON Error)
        else Solde Suffisant
            Service->>Service: source.setBalance(balance - amount)
            Service->>Service: target.setBalance(balance + amount)
            Service->>AccRepo: save(source)
            Service->>AccRepo: save(target)
            Service->>Audit: logTransaction(source, target, amount, TRANSFER, SUCCESS, null)
            Audit->>TxRepo: save(Transaction SUCCESS)
            TxRepo-->>Audit: Transaction saved
            Audit-->>Service: Transaction logged
            Service-->>Controller: TransactionResponse
            Controller-->>Client: 200 OK (TransactionResponse JSON)
        end
    end
```

---

### 8. State Diagram (Transaction Status)

Le diagramme d'états décrit le cycle de vie d'une transaction dans l'application. Une transaction est instanciée lors de la réception d'une demande de dépôt, retrait ou virement, puis passe directement à un état final persistant immuable (`SUCCESS` ou `FAILED`) selon les résultats de la validation des règles de solde et de découvert.

```mermaid
stateDiagram-v2
    [*] --> Submitted : Envoi requête (Deposit / Withdraw / Transfer)
    
    state Submitted {
        [*] --> ValidatingAccount : Vérification existence comptes
        ValidatingAccount --> CheckingRules : Comptes valides
        CheckingRules --> EvaluatingBalance : Contrôle solde + overdraftLimit
    }

    Submitted --> FAILED : Solde ou Overdraft Insuffisant / Erreur Métier
    Submitted --> SUCCESS : Solde suffisant & Mise à jour effectuée

    state FAILED {
        [*] --> LoggedFailed : Enregistrement immuable avec failureReason\n(via REQUIRES_NEW)
    }

    state SUCCESS {
        [*] --> LoggedSuccess : Enregistrement immuable dans DB
    }

    FAILED --> [*]
    SUCCESS --> [*]
```

---

### 9. Security & Exception Handling Flow

Le diagramme de flux de sécurité et de gestion des erreurs illustre l'absence de Spring Security explicite (selon le `pom.xml`) et se concentre sur le pipeline de validation de sécurité applicative, la gestion fine des exceptions et l'isolation transactionnelle. Les erreurs de validation d'entrée ou de solde sont interceptées par le `GlobalExceptionHandler` pour garantir une réponse REST standardisée.

```mermaid
flowchart TD
    Req["Requête HTTP Entrante"] --> ValFilter{"Validation DTO (@Valid)"}
    
    ValFilter -- "Invalide (ex: Montant négatif)" --> ErrVal["MethodArgumentNotValidException"]
    ErrVal --> GEH["GlobalExceptionHandler"]
    GEH --> Resp400["400 Bad Request (JSON)"]

    ValFilter -- "Valide" --> ExecSvc["Exécution BankingService"]
    
    ExecSvc --> ChkAcc{"Compte Existant ?"}
    ChkAcc -- "Non" --> ErrNotFound["AccountNotFoundException"]
    ErrNotFound --> GEH
    GEH --> Resp404["404 Not Found (JSON)"]

    ChkAcc -- "Oui" --> ChkBal{"canWithdraw() == true ?"}
    ChkBal -- "Non" --> AuditFail["TransactionAuditService.logTransaction(FAILED)"]
    AuditFail --> ErrBal["InsufficientBalanceException"]
    ErrBal --> GEH
    GEH --> Resp402["402 Payment Required (JSON)"]

    ChkBal -- "Oui" --> ExecTx["Mise à jour Solde + Audit SUCCESS"]
    ExecTx --> Resp200["200 OK / 201 Created (JSON Response)"]
```

---

### 10. Deployment Diagram

Le diagramme de déploiement montre l'environnement d'exécution physique et logique de l'application `mini-banking-system`. L'application Spring Boot s'exécute dans un processus JVM Java 17 embarquant un serveur Web Tomcat sur le port 8080 et une base de données H2 `in-memory`.

```mermaid
deployment
    node "Machine Hôte / Serveur Applicatif" {
        node "JVM 17 Environment" {
            artifact "mini-banking-system-1.0.0-SNAPSHOT.jar" {
                component "Spring Boot 3.2.5 Application Engine"
                component "Tomcat Embedded Web Server (Port 8080)"
                component "H2 Engine (In-Memory JDBC)"
            }
        }
    }

    node "Clients Externes" {
        component "Postman / Client HTTP"
        component "Navigateur Web (Console H2)"
    }

    "Postman / Client HTTP" -- "HTTP / REST (Port 8080)" --> "Tomcat Embedded Web Server (Port 8080)"
    "Navigateur Web (Console H2)" -- "HTTP / /h2-console" --> "Tomcat Embedded Web Server (Port 8080)"
    "Spring Boot 3.2.5 Application Engine" -- "JDBC In-Memory" --> "H2 Engine (In-Memory JDBC)"
```
