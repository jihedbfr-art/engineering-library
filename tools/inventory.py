import os
import json
import re

ROOT_DIR = r"e:\jihedapps\GitHub"
OUTPUT_JSON = os.path.join(ROOT_DIR, "inventory.json")
OUTPUT_MD = os.path.join(ROOT_DIR, "INVENTORY.md")

GOVERNANCE_FILES = ["LICENSE", "SECURITY.md", "CONTRIBUTING.md", "CODE_OF_CONDUCT.md", "CHANGELOG.md"]

def analyze_repo(repo_path):
    repo_name = os.path.basename(repo_path)
    if not os.path.isdir(repo_path) or repo_name.startswith('.'):
        return None
    
    # Check governance files
    gov_status = {}
    for gf in GOVERNANCE_FILES:
        gov_status[gf] = os.path.exists(os.path.join(repo_path, gf))
    
    has_readme_fr = os.path.exists(os.path.join(repo_path, "README.fr.md"))
    has_readme = os.path.exists(os.path.join(repo_path, "README.md"))
    
    # Find skills
    skills_count = 0
    v1_count = 0
    v2_count = 0
    
    for root, dirs, files in os.walk(repo_path):
        if "SKILL.md" in files:
            skills_count += 1
            skill_filepath = os.path.join(root, "SKILL.md")
            try:
                with open(skill_filepath, "r", encoding="utf-8", errors="ignore") as f:
                    content = f.read(1024)
                    if 'format: "v2"' in content or "format: 'v2'" in content:
                        v2_count += 1
                    else:
                        v1_count += 1
            except Exception:
                v1_count += 1

    # Check workflows
    workflows_dir = os.path.join(repo_path, ".github", "workflows")
    workflows = []
    if os.path.exists(workflows_dir):
        workflows = [f for f in os.listdir(workflows_dir) if f.endswith('.yml') or f.endswith('.yaml')]
    
    return {
        "name": repo_name,
        "path": repo_path,
        "has_readme": has_readme,
        "has_readme_fr": has_readme_fr,
        "governance": gov_status,
        "skills": {
            "total": skills_count,
            "v1": v1_count,
            "v2": v2_count
        },
        "workflows": workflows
    }

def main():
    repos = []
    for item in os.listdir(ROOT_DIR):
        full_path = os.path.join(ROOT_DIR, item)
        if os.path.isdir(full_path) and not item.startswith('.'):
            repo_info = analyze_repo(full_path)
            if repo_info:
                repos.append(repo_info)
    
    with open(OUTPUT_JSON, "w", encoding="utf-8") as f:
        json.dump(repos, f, indent=2)
    
    # Write Markdown summary
    with open(OUTPUT_MD, "w", encoding="utf-8") as f:
        f.write("# Inventaire de l'Écosystème JihedAiLabs\n\n")
        f.write(f"**Date d'analyse :** 2026-08-08\n\n")
        f.write("| Dépôt | Skills Total | Skills v1 | Skills v2 | README.fr.md | Gouvernance Complète | Workflows CI |\n")
        f.write("| :--- | :---: | :---: | :---: | :---: | :---: | :--- |\n")
        
        for r in repos:
            gov_ok = all(r["governance"].values())
            gov_str = "✅ Complète" if gov_ok else f"⚠️ ({sum(r['governance'].values())}/{len(GOVERNANCE_FILES)})"
            fr_str = "✅ Oui" if r["has_readme_fr"] else "❌ Non"
            wf_str = ", ".join(r["workflows"]) if r["workflows"] else "❌ Aucun"
            
            f.write(f"| `{r['name']}` | {r['skills']['total']} | {r['skills']['v1']} | {r['skills']['v2']} | {fr_str} | {gov_str} | {wf_str} |\n")
        
        f.write("\n\n## Détail de la Gouvernance par Dépôt\n\n")
        f.write("| Dépôt | " + " | ".join(GOVERNANCE_FILES) + " |\n")
        f.write("| :--- | " + " | ".join([":---:"] * len(GOVERNANCE_FILES)) + " |\n")
        for r in repos:
            row = [f"`{r['name']}`"]
            for gf in GOVERNANCE_FILES:
                row.append("✅" if r["governance"][gf] else "❌")
            f.write("| " + " | ".join(row) + " |\n")

    print(f"Inventaire généré avec succès dans {OUTPUT_JSON} et {OUTPUT_MD}")

if __name__ == "__main__":
    main()
