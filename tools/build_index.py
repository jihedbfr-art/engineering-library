import os
import sys
import json
import argparse

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')


try:
    import yaml
except ImportError:
    yaml = None

def parse_frontmatter(content):
    if not content.startswith("---"):
        return {}
    parts = content.split("---", 2)
    if len(parts) < 3:
        return {}
    yaml_text = parts[1]
    
    data = {}
    if yaml is not None:
        try:
            data = yaml.safe_load(yaml_text) or {}
        except Exception:
            pass
    if not data:
        for line in yaml_text.strip().splitlines():
            if ":" in line:
                k, v = line.split(":", 1)
                data[k.strip()] = v.strip().strip('"').strip("'")
    return data

def build_index(root_path):
    skills_index = []
    llms_txt_lines = [
        "# JihedAiLabs Engineering & Skill Index",
        "> Canonical catalog of engineering knowledge, skills, and labs for AI agents and developers.\n",
        "## Repositories & Active Skills\n"
    ]

    for root, dirs, files in os.walk(root_path):
        if "SKILL.md" in files:
            skill_path = os.path.join(root, "SKILL.md")
            try:
                with open(skill_path, "r", encoding="utf-8", errors="ignore") as f:
                    content = f.read()
                fm = parse_frontmatter(content)
                if not fm:
                    continue

                rel_dir = os.path.relpath(root, root_path).replace("\\", "/")
                
                skill_entry = {
                    "name": fm.get("name", os.path.basename(root)),
                    "title": fm.get("title", ""),
                    "title_fr": fm.get("title_fr", ""),
                    "description": fm.get("description", ""),
                    "description_fr": fm.get("description_fr", ""),
                    "domain": fm.get("domain", ""),
                    "tags": fm.get("tags", []),
                    "maturity": fm.get("maturity", "stable"),
                    "path": f"{rel_dir}/SKILL.md"
                }
                skills_index.append(skill_entry)

                llms_txt_lines.append(f"- [{skill_entry['name']}]({skill_entry['path']}): {skill_entry['description']}")
            except Exception as e:
                print(f"Error indexing {skill_path}: {e}")

    index_json_path = os.path.join(root_path, "skills-index.json")
    llms_txt_path = os.path.join(root_path, "llms.txt")

    # Check drift if requested
    json_str = json.dumps(skills_index, indent=2, ensure_ascii=False)
    llms_str = "\n".join(llms_txt_lines) + "\n"

    return skills_index, json_str, llms_str, index_json_path, llms_txt_path

def main():
    parser = argparse.ArgumentParser(description="Generate skills-index.json and llms.txt")
    parser.add_argument("path", nargs="?", default=r"e:\jihedapps\GitHub", help="Root directory")
    parser.add_argument("--check", action="store_true", help="Check if generated index files are up to date")
    args = parser.parse_args()

    skills_index, json_str, llms_str, json_path, llms_path = build_index(args.path)

    if args.check:
        drift = False
        if os.path.exists(json_path):
            with open(json_path, "r", encoding="utf-8", errors="ignore") as f:
                if f.read().strip() != json_str.strip():
                    print("❌ DRIFT: skills-index.json is out of date.")
                    drift = True
        else:
            print("❌ MISSING: skills-index.json")
            drift = True

        if os.path.exists(llms_path):
            with open(llms_path, "r", encoding="utf-8", errors="ignore") as f:
                if f.read().strip() != llms_str.strip():
                    print("❌ DRIFT: llms.txt is out of date.")
                    drift = True
        else:
            print("❌ MISSING: llms.txt")
            drift = True

        if drift:
            sys.exit(1)
        else:
            print(f"✅ Index files (skills-index.json, llms.txt) are up to date for {len(skills_index)} skill(s).")
            sys.exit(0)
    else:
        with open(json_path, "w", encoding="utf-8") as f:
            f.write(json_str)
        with open(llms_path, "w", encoding="utf-8") as f:
            f.write(llms_str)
        print(f"✅ Successfully built skills-index.json ({len(skills_index)} skills) and llms.txt in '{args.path}'.")

if __name__ == "__main__":
    main()
