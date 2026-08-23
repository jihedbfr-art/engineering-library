import os
import sys
import re

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

SKILLS_DIR = r"e:\jihedapps\GitHub\engineering-library\knowledge\ai-engineering\06-agent-hooks-and-skills\skills"

# Title & Description FR mappings for the 23 skills in engineering-library
SKILL_TRANSLATIONS = {
    "adr-writer": ("Architectural Decision Record (ADR) Writer", "Rédacteur d'ADR (Architectural Decision Record)"),
    "api-contract-reviewer": ("API Contract Reviewer", "Revue de contrat d'API REST/gRPC"),
    "changelog-writer": ("Changelog Writer", "Générateur de Changelog et Notes de Version"),
    "code-refactoring-planner": ("Code Refactoring Planner", "Planificateur de refactoring de code"),
    "cost-optimization-reviewer": ("Cloud & Resource Cost Optimization Reviewer", "Revue d'optimisation des coûts cloud et ressources"),
    "data-pipeline-reviewer": ("Data Pipeline Reviewer", "Revue de pipelines de données et ETL"),
    "database-migration-reviewer": ("Database Migration Reviewer", "Revue de migrations de base de données (Liquibase/Flyway)"),
    "dependency-upgrade-reviewer": ("Dependency Upgrade Reviewer", "Revue de montée en version de dépendances"),
    "documentation-generator": ("Technical Documentation Generator", "Générateur de documentation technique"),
    "feature-flag-rollout-planner": ("Feature Flag Rollout Planner", "Planificateur de déploiement par Feature Flags"),
    "git-commit-message-writer": ("Conventional Git Commit Message Writer", "Rédacteur de messages de commit Git conventionnels"),
    "incident-postmortem-writer": ("Blameless Incident Postmortem Writer", "Rédacteur de post-mortem d'incident sans blâme"),
    "keycloak-spi-scaffold": ("Keycloak SPI Scaffold & Generator", "Générateur et squelette de Keycloak SPI"),
    "load-testing-plan-writer": ("Load & Performance Testing Plan Writer", "Rédacteur de plan de tests de charge et performance"),
    "mcp-python-server-fastapi": ("FastAPI MCP Python Server Generator", "Générateur de serveur MCP Python avec FastAPI"),
    "observability-instrumentation-reviewer": ("Observability & OpenTelemetry Reviewer", "Revue d'instrumentation observabilité et OpenTelemetry"),
    "oncall-runbook-writer": ("On-Call Runbook Writer", "Rédacteur de runbooks et procédures astreinte"),
    "performance-profiling-guide": ("Java & Application Performance Profiling Guide", "Guide de profilage et performance Java"),
    "rag-eval-report": ("RAG Evaluation & Quality Report Writer", "Rapport d'évaluation et qualité RAG"),
    "security-audit-checklist": ("Application Security Audit Checklist", "Checklist d'audit de sécurité applicative"),
    "spring-boot-code-review": ("Spring Boot & JPA Code Review", "Revue de code Spring Boot et JPA"),
    "telecom-bss-integration-review": ("Telecom BSS & Core 5G Integration Review", "Revue d'intégration BSS et Core 5G Télécom"),
    "test-plan-writer": ("Integration & Unit Test Plan Writer", "Rédacteur de plans de tests unitaires et intégration")
}

def migrate_skill(skill_dir):
    skill_file = os.path.join(skill_dir, "SKILL.md")
    if not os.path.exists(skill_file):
        return False, "File does not exist"

    with open(skill_file, "r", encoding="utf-8", errors="ignore") as f:
        content = f.read()

    folder_name = os.path.basename(skill_dir)
    
    # Extract v1 name and description if present
    name = folder_name
    desc_en = f"Production engineering skill for {folder_name}."
    
    if content.startswith("---"):
        parts = content.split("---", 2)
        if len(parts) >= 3:
            yaml_part = parts[1]
            body_part = parts[2]
            for line in yaml_part.strip().splitlines():
                if line.startswith("name:"):
                    name = line.split(":", 1)[1].strip().strip('"').strip("'")
                elif line.startswith("description:"):
                    desc_en = line.split(":", 1)[1].strip().strip('"').strip("'")
        else:
            body_part = content
    else:
        body_part = content

    # Truncate description to <= 200 chars for v2 frontmatter
    if len(desc_en) > 195:
        desc_en = desc_en[:192] + "..."

    title_en, title_fr = SKILL_TRANSLATIONS.get(folder_name, (name.replace("-", " ").title(), name.replace("-", " ").title()))
    desc_fr = f"Skill d'ingénierie de production pour {title_fr.lower()}."
    if len(desc_fr) > 195:
        desc_fr = desc_fr[:192] + "..."

    # Clean body: strip H1 header if present, and demote any inner H2 headers to H3
    lines = body_part.strip().splitlines()
    cleaned_lines = []
    for line in lines:
        if line.startswith("# "):
            continue  # remove main H1
        if line.startswith("## "):
            line = "#" + line  # convert ## to ###
        cleaned_lines.append(line)

    raw_body = "\n".join(cleaned_lines).strip()


    # Reconstruct into mandatory 4 H2 sections
    v2_frontmatter = f"""---
format: "v2"
name: "{name}"
title: "{title_en}"
title_fr: "{title_fr}"
description: "{desc_en}"
description_fr: "{desc_fr}"
domain: "06-agent-hooks-and-skills"
tags: [java, spring-boot, engineering, best-practices]
maturity: "stable"
audience: ["backend-engineer", "architect", "coding-agent"]
requires: ["java>=17", "maven>=3.9"]
updated: "2026-08-08"
---
"""

    v2_body = f"""## Prerequisites
- Repository codebase checked out locally.
- Access to Java 17+, Spring Boot 3+, or target framework environment.
- Required build tools (Maven/Gradle) installed.

## Usage
{raw_body}

## Inputs
- Source code diff or repository path under evaluation.
- Relevant documentation, configuration files, or issue description.

## Outputs
- Structured review findings, action items, or generated markdown artifacts.
"""

    full_v2_content = v2_frontmatter + "\n" + v2_body

    with open(skill_file, "w", encoding="utf-8") as f:
        f.write(full_v2_content)

    return True, "Migrated to v2"

def main():
    count = 0
    for folder in os.listdir(SKILLS_DIR):
        full_dir = os.path.join(SKILLS_DIR, folder)
        if os.path.isdir(full_dir):
            ok, msg = migrate_skill(full_dir)
            if ok:
                count += 1
                print(f"✅ Migrated: {folder}")
            else:
                print(f"⚠️ Skipped {folder}: {msg}")

    print(f"\nMigration complete: {count} skills updated.")

if __name__ == "__main__":
    main()
