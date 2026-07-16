# Oracle ADF — Working With It, and Migrating Off It

Oracle Application Development Framework (ADF) powered a huge share of enterprise Java applications built through the 2010s — government ERPs, banking back-offices, utility billing systems. It's rarely taught anymore, but it's still running in production in more places than its current visibility suggests, and understanding it is directly useful for anyone doing legacy modernization work.

## What ADF actually is

ADF is a **declarative, model-driven** J2EE framework built on top of JSF (JavaServer Faces), with Oracle's own component layer on top:

```
ADF Business Components (Model)  →  data layer: Entity Objects, View Objects,
                                     Application Modules — declarative binding
                                     to Oracle database tables/PL/SQL
        ↓
ADF Faces (View)                 →  rich JSF component library (JSFF fragments,
                                     JSPX pages) — tables, forms, LOVs, all
                                     wired declaratively, minimal hand-written JS
        ↓
ADF Controller (Task Flows)      →  page navigation and process flow, defined
                                     declaratively rather than in code
```
The core design philosophy: **configure behavior through XML/declarative metadata wherever possible, write Java only for genuine custom logic.** This made it fast to build large, database-heavy enterprise CRUD applications with rich UI (tables, LOVs, validation, master-detail forms) without hand-writing the JSF plumbing — which is exactly the kind of application a tax-declaration or multi-utility billing ERP fundamentally is underneath its business rules.

## Entity Objects & View Objects — the model layer, concretely

```
Entity Object (EO)   — maps to a database table, owns validation rules and
                        business logic tied to that table (think: a smarter,
                        declarative JPA entity with built-in DB-level validation)

View Object (VO)     — a queryable view over one or more EOs, often backed by
                        custom SQL/PL/SQL for complex joins or reporting —
                        this is where a lot of real business logic actually lives

Application Module   — the transactional unit exposed to the UI; groups VOs,
                        manages the underlying database transaction
```
A huge amount of real business logic in ADF applications lives not in obvious Java classes but in **View Object query definitions, Entity Object validation rules, and underlying PL/SQL packages** — which is exactly why [requirement archaeology](legacy-migration-playbook.md) for an ADF system means reading VO SQL and EO validation XML as carefully as any Java class, not instead of it.

## Task Flows — declarative process/navigation logic

```xml
<!-- Simplified task-flow.xml shape -->
<task-flow-definition id="declaration-flow">
  <view id="enterDeclaration"/>
  <view id="validateDeclaration"/>
  <view id="confirmDeclaration"/>
  <control-flow-rule>
    <from-activity-id>enterDeclaration</from-activity-id>
    <control-flow-case>
      <from-outcome>next</from-outcome>
      <to-activity-id>validateDeclaration</to-activity-id>
    </control-flow-case>
  </control-flow-rule>
</task-flow-definition>
```
This is a declarative, XML-defined version of exactly the same idea as a [BPMN process](../backend/java-spring/bpmn-workflow-engines.md) — a multi-step flow with defined transitions — except scoped to UI navigation rather than backend business-process orchestration, and expressed in Oracle's own metadata format rather than an open standard. Recognizing this parallel is genuinely useful when modernizing: a complex ADF task flow's structure often maps cleanly onto a BPMN process if the target architecture moves that logic to a proper workflow engine.

## Where the real complexity actually lives

For a genuinely representative government/utility ERP (multi-fluid billing, tax declaration, citizen-facing portals), the hard-to-migrate complexity is rarely in the ADF plumbing itself — it's in:

- **PL/SQL packages** implementing core business calculations (tax computation rules, billing logic, tariff bands) — often the actual source of truth, with the Java/ADF layer as a thinner presentation shell around it than it first appears.
- **Custom validation rules** scattered across Entity Object definitions, accumulated over years of client-requested edge cases.
- **Report generation** (commonly via Jasper/iReport in this generation of stack) with business-specific formatting requirements baked into report templates, not just data queries.
- **Multi-tenant/multi-client customization** — the same base application often serves multiple government directorates or client organizations, each with quietly different behavior for specific screens or validation rules, discovered mostly through support tickets rather than documentation.

## Migrating off ADF — applying strangler fig to this specific stack

Direct application of [strangler-fig-pattern](strangler-fig-pattern.md) to an ADF system, in practice:

```
1. Identify self-contained modules with clean data boundaries
   (Administration/profile management is a common first target —
   see the migration-ordering example in strangler-fig-pattern.md)
2. Extract the underlying PL/SQL business logic FIRST, independent of the
   ADF UI layer — this is usually the most valuable and most portable asset;
   it can often be exposed as a service (Spring Boot + JPA/JDBC calling the
   same PL/SQL packages, or gradually reimplemented in Java) well before
   the UI layer is touched at all
3. Build the new UI (Angular/React) against the newly-exposed service layer,
   for one module, validate output against the legacy ADF screen's actual behavior
4. Route that module's URLs to the new stack (the strangler fig routing layer);
   leave every other module on ADF, completely undisturbed
5. Repeat, module by module, until nothing depends on the ADF layer anymore
```
The critical sequencing decision: **extract and validate the business logic before touching the UI.** ADF's declarative UI layer is usually the *least* risky thing to replace — a table, a form, a task flow are conceptually simple to rebuild in Angular once you know exactly what data and validation they depend on. The PL/SQL and EO/VO business rules are where the real risk and the real archaeology work is, so surface and validate those first, independent of any UI decision.

## The honest assessment for anyone inheriting an ADF system today

ADF applications still running in production are usually there because they work and the cost/risk of a full rewrite was never clearly justified — not because nobody wanted to modernize. That's a legitimate state for a system to be in, not automatically technical debt requiring urgent action. The [strangler fig](strangler-fig-pattern.md) approach above is worth applying when there's a genuine driver (a new feature that's painful to build in ADF, a scaling/hiring problem because ADF skills are increasingly rare, a UX requirement ADF genuinely can't meet) — not as a modernization project justified purely by the stack's age.

## Where this connects

This page is the concrete, stack-specific instance of [strangler-fig-pattern](strangler-fig-pattern.md) and [legacy-migration-playbook](legacy-migration-playbook.md) applied to a real, specific technology most modernization guides skip entirely because it's unfashionable to discuss — which is exactly why it's worth having here.
