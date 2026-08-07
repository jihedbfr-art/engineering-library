# Task Management Platform

API REST de gestion de projets et de taches d'equipe, avec deux roles distincts (ADMIN, MEMBER)
et des regles d'autorisation appliquees au niveau service.

## But

Simuler un outil de suivi de taches type Jira simplifie : un ADMIN cree les projets et pilote
l'ensemble des taches, un MEMBER ne peut agir que sur les taches qui lui sont assignees. Le
systeme expose aussi une detection explicite des taches en retard.

## Architecture

Monolithe Spring Boot en couches, module Maven unique :

| Couche | Package | Role |
|---|---|---|
| Controller | `com.jihedapps.taskmanagement.controller` | Endpoints REST, mapping DTO <-> service, codes HTTP |
| Service | `com.jihedapps.taskmanagement.service` | Regles metier et autorisations, transactions |
| Repository | `com.jihedapps.taskmanagement.repository` | Acces donnees via Spring Data JPA |
| Entity | `com.jihedapps.taskmanagement.entity` | Modele de domaine (User, Project, Task, Comment) |
| DTO | `com.jihedapps.taskmanagement.dto` | Contrats d'entree/sortie de l'API (records) |
| Exception | `com.jihedapps.taskmanagement.exception` | Exceptions metier + `@RestControllerAdvice` |
| Config | `com.jihedapps.taskmanagement.config` | Bean `Clock`, jeu de donnees de demonstration |

Modele de domaine : `User(role)` -> `Project` -> `Task(assignee, status, priority, deadline)` -> `Comment`.

### Authentification simplifiee

Pas de Spring Security dans cette demonstration : l'identite de l'appelant est transmise via
l'en-tete HTTP `X-User-Id` (id d'un `User` existant). Chaque service verifie le role et la
relation avec la ressource a partir de cet id. C'est un choix assume pour rester dans le
perimetre "macro-project" sans y consacrer un chantier d'authentification complet — voir
limitations ci-dessous.

## Lancer le projet

```bash
mvn spring-boot:run
```

L'application demarre sur `http://localhost:8081`. Une base H2 fichier est creee dans
`./data/taskdb.mv.db` au premier lancement, avec deux utilisateurs de demonstration
(`admin`, id 1, role ADMIN ; `jihed`, id 2, role MEMBER).

Console H2 : `http://localhost:8081/h2-console` (JDBC URL `jdbc:h2:file:./data/taskdb`, user `sa`,
mot de passe vide).

## Endpoints principaux

| Methode | Endpoint | Regle |
|---|---|---|
| `GET /api/users` | Liste des utilisateurs | libre |
| `POST /api/projects` | Creer un projet | `X-User-Id` doit etre un ADMIN |
| `GET /api/projects` | Lister les projets | libre |
| `POST /api/tasks` | Creer une tache | ADMIN ou MEMBER |
| `PUT /api/tasks/{id}` | Modifier une tache | ADMIN, ou MEMBER si `assignee == X-User-Id` |
| `DELETE /api/tasks/{id}` | Supprimer une tache | ADMIN uniquement |
| `GET /api/tasks/overdue` | Taches en retard (deadline depassee, statut != DONE) | libre |
| `POST /api/tasks/{taskId}/comments` | Commenter une tache | utilisateur existant |

Exemple :

```bash
curl -X POST http://localhost:8081/api/projects \
  -H "X-User-Id: 1" -H "Content-Type: application/json" \
  -d '{"name":"Migration cloud","description":"Refonte infra"}'
```

## Regles metier couvertes par les tests

- `TaskServiceTest` : un ADMIN peut modifier/supprimer n'importe quelle tache ; un MEMBER ne peut
  modifier que les taches qui lui sont assignees (et ne peut jamais supprimer) ; detection des
  taches en retard (`deadline < now && status != DONE`), y compris le cas DONE jamais en retard.
- `ProjectServiceTest` : seul un ADMIN peut creer un projet.

## Limitations connues

- Pas d'authentification reelle (JWT/session) : `X-User-Id` est un en-tete de confiance, a ne
  jamais reproduire tel quel en production.
- Pas de pagination sur les listes (`GET /api/tasks`, `/api/projects`) : acceptable au volume de
  demonstration, a ajouter avant un usage reel.
- Pas de suppression en cascade documentee au-dela de JPA par defaut : supprimer un projet avec
  des taches actives n'est pas gere explicitement.
- Frontend : page statique d'information uniquement (`src/main/resources/static/index.html`),
  pas d'UI de gestion — usage prevu via API/HTTP client, conformement au choix de stack du
  palier "macro-projects".


# Architecture & Design Diagrams - Task Management Platform

## 1. Architecture Overview (C4 Container / Context)

The Architecture Overview diagram presents the macro-level system context and container view for the `task-management-platform`. HTTP clients (such as web browsers or API testing tools) interact via REST over HTTP on port 8081, passing user identity in the custom `X-User-Id` request header. The core application runs as a Spring Boot application container hosting Spring MVC controllers, domain services, and Spring Data JPA repositories, which read/write data to an embedded file-based H2 database stored at `./data/taskdb`.

```mermaid
graph TD
    subgraph External["Client Tier"]
        Client["HTTP Client / API Consumer<br/>(Web / Postman / cURL)"]
    end

    subgraph AppServer["Spring Boot Runtime Container (Port 8081)"]
        subgraph WebLayer["Web / Controller Layer"]
            Controllers["REST Controllers<br/>(TaskController, ProjectController,<br/>UserController, CommentController)"]
        end
        
        subgraph ServiceLayer["Service & Business Logic Layer"]
            Services["Spring Services<br/>(TaskService, ProjectService,<br/>UserService, CommentService)"]
            Security["Security & Authorization Logic<br/>(X-User-Id header extraction & RBAC)"]
        end

        subgraph DataLayer["Persistence Layer"]
            Repositories["Spring Data JPA Repositories<br/>(TaskRepository, ProjectRepository,<br/>UserRepository, CommentRepository)"]
        end
    end

    subgraph DatabaseTier["Data Tier"]
        H2DB[(Embedded H2 Database<br/>'jdbc:h2:file:./data/taskdb')]
    end

    Client -->|HTTP REST Requests + Header 'X-User-Id'| Controllers
    Controllers --> Services
    Services --> Security
    Services --> Repositories
    Repositories -->|JDBC / JPA Hibernate| H2DB
```

---

## 2. Package Diagram

The Package Diagram illustrates the package structure under `com.jihedapps.taskmanagement`. The application adheres to clean package-by-layer organization: `controller` (REST endpoints), `dto` (Java records for request/response bodies), `service` (business rules and authorization checks), `repository` (Spring Data JPA interfaces), `entity` (JPA models and enums), `config` (Spring bean configurations and seed data initializer), `exception` (global exception handling), and the main entry point `TaskManagementApplication`.

```mermaid
graph TD
    subgraph com_jihedapps_taskmanagement["com.jihedapps.taskmanagement"]
        App["TaskManagementApplication"]

        subgraph config["config"]
            AppConfig["AppConfig"]
            DemoData["DemoDataInitializer"]
        end

        subgraph controller["controller"]
            TaskCtrl["TaskController"]
            ProjCtrl["ProjectController"]
            UserCtrl["UserController"]
            CommCtrl["CommentController"]
        end

        subgraph dto["dto"]
            TaskDtos["TaskDtos"]
            ProjDtos["ProjectDtos"]
            UserDtos["UserDtos"]
            CommDtos["CommentDtos"]
        end

        subgraph service["service"]
            TaskSvc["TaskService"]
            ProjSvc["ProjectService"]
            UserSvc["UserService"]
            CommSvc["CommentService"]
        end

        subgraph repository["repository"]
            TaskRepo["TaskRepository"]
            ProjRepo["ProjectRepository"]
            UserRepo["UserRepository"]
            CommRepo["CommentRepository"]
        end

        subgraph entity["entity"]
            UserEnt["User"]
            ProjEnt["Project"]
            TaskEnt["Task"]
            CommEnt["Comment"]
            RoleEnum["Role"]
            StatusEnum["TaskStatus"]
            PrioEnum["TaskPriority"]
        end

        subgraph exception["exception"]
            GlobalExHandler["GlobalExceptionHandler"]
            ForbiddenEx["ForbiddenOperationException"]
            NotFoundEx["ResourceNotFoundException"]
        end
    end

    controller --> dto
    controller --> service
    controller --> entity
    service --> repository
    service --> entity
    service --> exception
    repository --> entity
    dto --> entity
    config --> service
    config --> repository
    config --> entity
```

---

## 3. Layer Diagram

The Layer Diagram highlights the separation of concerns across architectural layers. Requests enter the Presentation Layer, flow down to the Service/Domain Layer for validation and RBAC checks (`checkCanModify`), proceed to the Data Access Layer (Repositories), and are executed against the Persistence Layer (H2 DB). Cross-cutting components like `GlobalExceptionHandler` and `AppConfig` supply error handling and shared infrastructure beans across layers.

```mermaid
graph TB
    subgraph PresentationLayer["Presentation Layer (REST Controllers & DTO Records)"]
        Controllers["TaskController | ProjectController | UserController | CommentController"]
        DTOs["TaskDtos | ProjectDtos | UserDtos | CommentDtos"]
    end

    subgraph ServiceLayer["Business Logic & Domain Layer (Services & Entities)"]
        Services["TaskService | ProjectService | UserService | CommentService"]
        Entities["Task | Project | User | Comment | Role | TaskStatus | TaskPriority"]
    end

    subgraph DataAccessLayer["Data Access Layer (Spring Data JPA Repositories)"]
        Repositories["TaskRepository | ProjectRepository | UserRepository | CommentRepository"]
    end

    subgraph InfrastructureLayer["Persistence Layer"]
        Database[("H2 File Database (./data/taskdb)")]
    end

    subgraph CrossCutting["Cross-Cutting Concerns"]
        ExceptionHandling["GlobalExceptionHandler (ResourceNotFoundException, ForbiddenOperationException)"]
        Config["AppConfig (Clock) | DemoDataInitializer (CommandLineRunner)"]
    end

    Controllers --> DTOs
    Controllers --> Services
    Services --> Entities
    Services --> Repositories
    Repositories --> Database
    ExceptionHandling -.-> Controllers
    Config -.-> Services
```

---

## 4. Component Diagram

The Component Diagram details the internal Spring bean wiring and service interdependencies. `TaskController`, `ProjectController`, `UserController`, and `CommentController` process endpoint mapping and input validation. `TaskService` collaborates with `UserService` and `ProjectService` to validate user rights and project existences. `DemoDataInitializer` seeds default data on startup via `UserService` and `UserRepository`.

```mermaid
graph LR
    subgraph Controllers["Controller Components"]
        UC["UserController"]
        PC["ProjectController"]
        TC["TaskController"]
        CC["CommentController"]
    end

    subgraph Services["Service Components"]
        US["UserService"]
        PS["ProjectService"]
        TS["TaskService"]
        CS["CommentService"]
    end

    subgraph Repositories["Repository Components"]
        UR["UserRepository"]
        PR["ProjectRepository"]
        TR["TaskRepository"]
        CR["CommentRepository"]
    end

    subgraph Infrastructure["Infrastructure Components"]
        GEH["GlobalExceptionHandler"]
        CLK["Clock Bean"]
        DDI["DemoDataInitializer"]
    end

    UC --> US
    PC --> PS
    TC --> TS
    TC --> CLK
    CC --> CS

    PS --> US
    PS --> PR

    TS --> UR
    TS --> US
    TS --> PS
    TS --> TR
    TS --> CLK

    CS --> US
    CS --> TS
    CS --> CR

    US --> UR

    DDI --> UR
    DDI --> US
```

---

## 5. ERD (Entity Relationship Diagram)

The ERD shows the database schema derived from the JPA mapping annotations `@Entity`, `@Table`, and `@ManyToOne`. Four main tables are defined: `app_user`, `project`, `task`, and `task_comment`. Multiplicities reflect domain constraints (e.g., a project is created by 1 user; a task belongs to 1 project and optionally 1 assigned user; a task can have 0 to many comments).

```mermaid
erDiagram
    APP_USER {
        bigint id PK
        varchar username UK
        varchar display_name
        varchar role "ADMIN | MEMBER"
    }

    PROJECT {
        bigint id PK
        varchar name
        varchar description
        bigint created_by_id FK
        timestamp created_at
    }

    TASK {
        bigint id PK
        varchar title
        varchar description
        bigint project_id FK
        bigint assignee_id FK "nullable"
        varchar status "TODO | IN_PROGRESS | DONE"
        varchar priority "LOW | MEDIUM | HIGH | CRITICAL"
        timestamp deadline
        timestamp created_at
    }

    TASK_COMMENT {
        bigint id PK
        bigint task_id FK
        bigint author_id FK
        varchar body
        timestamp created_at
    }

    APP_USER ||--o{ PROJECT : "creates"
    APP_USER ||--o{ TASK : "is assigned to"
    PROJECT ||--o{ TASK : "contains"
    TASK ||--o{ TASK_COMMENT : "has"
    APP_USER ||--o{ TASK_COMMENT : "authors"
```

---

## 6. Class Diagram UML (Main Business Entities)

The UML Class Diagram models the core entity domain, enums, attributes, visibility, and operations. Key entity helper methods like `Task.isOverdue(LocalDateTime now)` and `User.isAdmin()` implement domain-specific calculations. Relationships model composition and direct references between entities.

```mermaid
classDiagram
    class Role {
        <<enumeration>>
        ADMIN
        MEMBER
    }

    class TaskStatus {
        <<enumeration>>
        TODO
        IN_PROGRESS
        DONE
    }

    class TaskPriority {
        <<enumeration>>
        LOW
        MEDIUM
        HIGH
        CRITICAL
    }

    class User {
        -Long id
        -String username
        -String displayName
        -Role role
        +getId() Long
        +getUsername() String
        +setUsername(String) void
        +getDisplayName() String
        +setDisplayName(String) void
        +getRole() Role
        +setRole(Role) void
        +isAdmin() boolean
    }

    class Project {
        -Long id
        -String name
        -String description
        -User createdBy
        -LocalDateTime createdAt
        +getId() Long
        +getName() String
        +setName(String) void
        +getDescription() String
        +setDescription(String) void
        +getCreatedBy() User
        +getCreatedAt() LocalDateTime
    }

    class Task {
        -Long id
        -String title
        -String description
        -Project project
        -User assignee
        -TaskStatus status
        -TaskPriority priority
        -LocalDateTime deadline
        -LocalDateTime createdAt
        +getId() Long
        +getTitle() String
        +setTitle(String) void
        +getDescription() String
        +setDescription(String) void
        +getProject() Project
        +getAssignee() User
        +setAssignee(User) void
        +getStatus() TaskStatus
        +setStatus(TaskStatus) void
        +getPriority() TaskPriority
        +setPriority(TaskPriority) void
        +getDeadline() LocalDateTime
        +setDeadline(LocalDateTime) void
        +getCreatedAt() LocalDateTime
        +isOverdue(LocalDateTime now) boolean
    }

    class Comment {
        -Long id
        -Task task
        -User author
        -String body
        -LocalDateTime createdAt
        +getId() Long
        +getTask() Task
        +getAuthor() User
        +getBody() String
        +getCreatedAt() LocalDateTime
    }

    User "1" -- "1" Role : has
    Project "1" -- "1" User : createdBy
    Task "1" -- "1" Project : belongsTo
    Task "0..1" -- "1" User : assignedTo
    Task "1" -- "1" TaskStatus : hasStatus
    Task "1" -- "1" TaskPriority : hasPriority
    Comment "1" -- "1" Task : onTask
    Comment "1" -- "1" User : authoredBy
```

---

## 7. Sequence Diagram (Updating a Task)

The Sequence Diagram details the interaction flow when a client submits a `PUT /api/tasks/{id}` request with the `X-User-Id` header. The request moves through `TaskController`, `TaskService`, `UserService`, and `TaskRepository`. Authorization checks verify whether the user has sufficient permissions (`ADMIN` role or assigned `MEMBER`); if unauthorized or missing, exceptions are intercepted by `GlobalExceptionHandler` to return appropriate HTTP status codes (403 or 404).

```mermaid
sequenceDiagram
    autonumber
    actor Client as HTTP Client
    participant Ctrl as TaskController
    participant TSvc as TaskService
    participant USvc as UserService
    participant TRepo as TaskRepository
    participant GEH as GlobalExceptionHandler

    Client->>Ctrl: PUT /api/tasks/{id} Header [X-User-Id: 2] Body [UpdateTaskRequest]
    Ctrl->>TSvc: updateTask(requesterId=2, taskId={id}, title, description, assigneeId, status, priority, deadline)
    TSvc->>USvc: requireById(2)
    USvc-->>TSvc: User requester (Role: MEMBER)
    TSvc->>TRepo: findById(id)
    TRepo-->>TSvc: Optional<Task> task
    
    alt Task Not Found
        TSvc-->>Ctrl: throw ResourceNotFoundException
        Ctrl-->>GEH: catch ResourceNotFoundException
        GEH-->>Client: 404 NOT_FOUND { "message": "Tache introuvable : id=..." }
    else Task Found
        TSvc->>TSvc: checkCanModify(requester, task)
        alt Requester is MEMBER and NOT assignee
            TSvc-->>Ctrl: throw ForbiddenOperationException
            Ctrl-->>GEH: catch ForbiddenOperationException
            GEH-->>Client: 403 FORBIDDEN { "message": "Un MEMBER ne peut modifier..." }
        else Requester is ADMIN or is Task Assignee
            TSvc->>TSvc: Apply field updates (status, priority, deadline, etc.)
            TSvc-->>Ctrl: Task updatedTask
            Ctrl->>Ctrl: TaskResponse.from(updatedTask, now)
            Ctrl-->>Client: 200 OK (TaskResponse JSON)
        end
    end
```

---

## 8. State Diagram (Task Status Lifecycle)

The State Diagram illustrates the discrete status states and transitions for a `Task`. Tasks start in `TODO` by default upon instantiation. A task can transition between `TODO`, `IN_PROGRESS`, and `DONE`. In addition to explicit status state changes, the domain evaluates whether a task is overdue dynamically using `isOverdue(now)` (when deadline < current timestamp and status != DONE).

```mermaid
stateDiagram-v2
    [*] --> TODO : Task Created (Default Status = TODO)
    
    state TODO {
        [*] --> Pending
        Pending --> Overdue_TODO : deadline < now
    }

    TODO --> IN_PROGRESS : updateTask(status = IN_PROGRESS)
    IN_PROGRESS --> TODO : updateTask(status = TODO)

    state IN_PROGRESS {
        [*] --> InWork
        InWork --> Overdue_IN_PROGRESS : deadline < now
    }

    IN_PROGRESS --> DONE : updateTask(status = DONE)
    TODO --> DONE : updateTask(status = DONE)

    state DONE {
        [*] --> Completed : isOverdue() returns false
    }

    DONE --> IN_PROGRESS : Re-opened
    DONE --> TODO : Re-opened
```

---

## 9. Security Flow (X-User-Id Interception & RBAC)

The Security Flow flowchart describes how pseudo-authentication and Role-Based Access Control (RBAC) operate. Requests supply `X-User-Id` in headers. Controllers forward `requesterId` to service layer methods. Services look up the `User` and check role requirements (e.g., project creation and task deletion require `ADMIN`; task modification requires `ADMIN` or being the assigned `MEMBER`). Unauthorized operations trigger `ForbiddenOperationException`, producing HTTP 403 Forbidden responses.

```mermaid
flowchart TD
    Start([Incoming Request with HTTP Header X-User-Id]) --> ExtractHeader[Controller extracts X-User-Id parameter]
    ExtractHeader --> ServiceCall[Controller invokes Service method]
    ServiceCall --> LoadUser[UserService.requireById requesterId]
    
    LoadUser --> UserCheck{User Exists in app_user?}
    UserCheck -- No --> 404[Throw ResourceNotFoundException -> 404 Not Found]
    UserCheck -- Yes --> OpType{Operation Type?}

    OpType -- "POST /api/projects" --> CheckAdmin1{Is User ADMIN?}
    CheckAdmin1 -- No --> 403_1[Throw ForbiddenOperationException -> 403 Forbidden]
    CheckAdmin1 -- Yes --> ExecProj[Create Project & Save]

    OpType -- "DELETE /api/tasks/{id}" --> CheckAdmin2{Is User ADMIN?}
    CheckAdmin2 -- No --> 403_2[Throw ForbiddenOperationException -> 403 Forbidden]
    CheckAdmin2 -- Yes --> ExecDel[Delete Task & Return 204]

    OpType -- "PUT /api/tasks/{id}" --> CheckModify{Is User ADMIN?}
    CheckModify -- Yes --> ExecUpdate[Update Task Fields & Return 200]
    CheckModify -- No --> CheckAssignee{Is Task Assigned to User?}
    CheckAssignee -- No --> 403_3[Throw ForbiddenOperationException -> 403 Forbidden]
    CheckAssignee -- Yes --> ExecUpdate

    OpType -- "POST /api/tasks or /comments" --> ExecCreate[Execute Creation & Return 201]

    ExecProj --> End[201 Created Response]
    ExecDel --> End2[204 No Content Response]
    ExecUpdate --> End3[200 OK Response]
    ExecCreate --> End
```

---

## 10. Deployment Diagram (Simplified Spring Boot + H2)

The Deployment Diagram models the runtime physical environment. The application executes inside a Java 17 Runtime Environment as a standalone Spring Boot JAR file (`task-management-platform-0.1.0.jar`) on server port 8081. It hosts an embedded Tomcat web server and uses the embedded H2 driver to persist binary database files locally in `./data/taskdb.mv.db`.

```mermaid
node "Host Server (OS: Windows / Linux)" {
    node "Java 17 Runtime Environment (JRE)" {
        artifact "task-management-platform-0.1.0.jar" {
            component "Embedded Tomcat Web Server\n(Port 8081)" as Tomcat
            component "Spring Boot Application\n(Spring MVC + Spring Data JPA)" as SpringApp
            component "Embedded H2 Engine\n(org.h2.Driver)" as H2Engine
        }
    }

    folder "Local Storage Directory (./data)" {
        database "H2 File Database\n(taskdb.mv.db / taskdb.trace.db)" as H2Files
    }
}

node "Client Workstation" {
    component "Web Browser / REST Client\n(cURL / Postman / Frontend App)" as ClientApp
}

ClientApp --> Tomcat : "HTTP / REST Requests (Port 8081)"
Tomcat --> SpringApp : "Internal Servlet Routing"
SpringApp --> H2Engine : "JDBC Connection (jdbc:h2:file:./data/taskdb)"
H2Engine --> H2Files : "Read / Write DB files"
```
