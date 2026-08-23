import os
import sys
import re
import argparse

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

try:
    import yaml
except ImportError:
    yaml = None

REQUIRED_FRONTMATTER_KEYS = [
    "format", "name", "title", "title_fr", "description", "description_fr",
    "domain", "tags", "maturity", "audience", "requires", "updated"
]

REQUIRED_H2_SECTIONS = [
    "Prerequisites", "Usage", "Inputs", "Outputs"
]

def parse_frontmatter(content):
    if not content.startswith("---"):
        return None, "Missing starting '---' frontmatter delimiter"
    parts = content.split("---", 2)
    if len(parts) < 3:
        return None, "Invalid frontmatter block structure"
    yaml_text = parts[1]
    body = parts[2]
    
    data = {}
    if yaml is not None:
        try:
            data = yaml.safe_load(yaml_text) or {}
        except Exception as e:
            return None, f"YAML parse error: {e}"
    else:
        # Fallback basic parser if pyyaml is not installed
        for line in yaml_text.strip().splitlines():
            if ":" in line:
                key, val = line.split(":", 1)
                data[key.strip()] = val.strip().strip('"').strip("'")
    
    return (data, body), None

def lint_skill_file(filepath, strict=False):
    errors = []
    warnings = []
    
    try:
        with open(filepath, "r", encoding="utf-8", errors="ignore") as f:
            content = f.read()
    except Exception as e:
        return [f"Could not read file: {e}"], []

    res, parse_err = parse_frontmatter(content)
    if parse_err:
        return [parse_err], []
    
    fm, body = res
    
    # Rule 1: Format v2 check
    if str(fm.get("format")) != "v2":
        errors.append("Frontmatter 'format' must be 'v2'")
        
    # Rule 2: Mandatory keys
    for key in REQUIRED_FRONTMATTER_KEYS:
        if key not in fm or not fm[key]:
            errors.append(f"Missing required frontmatter key: '{key}'")
            
    # Rule 3: Description length <= 200 chars
    desc = str(fm.get("description", ""))
    if len(desc) > 200:
        errors.append(f"'description' length ({len(desc)}) exceeds maximum 200 characters")
        
    # Rule 4: Body checks - No HTML tags (img, div)
    if re.search(r"<(img|div)[^>]*>", body, re.IGNORECASE):
        errors.append("SKILL.md body contains forbidden HTML tags (<img>, <div>)")
        
    # Rule 5: Cross-language navigation links check
    if re.search(r"\[(French|English|Version française|English version)\]\(", body, re.IGNORECASE):
        errors.append("SKILL.md body contains forbidden cross-language navigation links")

    # Rule 6: H2 Section Structure
    h2_matches = [line.strip() for line in body.splitlines() if line.startswith("## ")]
    h2_titles = [re.sub(r"^##\s+", "", h).strip() for h in h2_matches]
    
    if h2_titles != REQUIRED_H2_SECTIONS:
        errors.append(f"H2 sections mismatch. Found: {h2_titles}. Expected exactly: {REQUIRED_H2_SECTIONS}")
        
    return errors, warnings

def main():
    parser = argparse.ArgumentParser(description="Lint SKILL.md files for v2 format compliance.")
    parser.add_argument("path", nargs="?", default=r"e:\jihedapps\GitHub", help="Root directory or file to lint")
    parser.add_argument("--strict", action="store_true", help="Treat warnings as errors")
    args = parser.parse_args()
    
    target_path = args.path
    skill_files = []
    
    if os.path.isfile(target_path):
        if target_path.endswith("SKILL.md"):
            skill_files.append(target_path)
    else:
        for root, _, files in os.walk(target_path):
            if "SKILL.md" in files:
                skill_files.append(os.path.join(root, "SKILL.md"))
                
    total_files = len(skill_files)
    failed_files = 0
    
    print(f"--- Linting {total_files} SKILL.md file(s) under '{target_path}' ---")
    
    for sf in skill_files:
        rel_path = os.path.relpath(sf, target_path)
        errs, warns = lint_skill_file(sf, strict=args.strict)
        if errs:
            failed_files += 1
            print(f"❌ FAIL: {rel_path}")
            for e in errs:
                print(f"    - {e}")
        else:
            print(f"✅ PASS: {rel_path}")
            
    print(f"\nSummary: {total_files - failed_files}/{total_files} passed.")
    if failed_files > 0:
        sys.exit(1)
    else:
        sys.exit(0)

if __name__ == "__main__":
    main()
