import os
import re
import sys
from pathlib import Path

ROOT_DIR = Path(__file__).parent.parent.resolve()

KNOWLEDGE_H2 = ["Context", "Architecture", "Pattern", "Trade-offs (Cost/Latency)"]
AGENT_SKILL_H2 = ["Prerequisites", "Usage", "Inputs", "Outputs"]

def parse_frontmatter(content):
    match = re.match(r"^---\n(.*?)\n---\n(.*)", content, re.DOTALL)
    if not match:
        return None, content
    
    frontmatter = {}
    for line in match.group(1).splitlines():
        if ":" in line:
            k, v = line.split(":", 1)
            frontmatter[k.strip()] = v.strip().strip('"').strip("'")
            
    return frontmatter, match.group(2)

def extract_h2(body):
    return re.findall(r"^##\s+(.*)$", body, re.MULTILINE)

def lint_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
    except Exception as e:
        return [f"Cannot read file: {e}"]
        
    errors = []
    frontmatter, body = parse_frontmatter(content)
    
    if not frontmatter:
        # Ignore files without frontmatter
        return []
        
    # Grandfather clause
    if frontmatter.get("format") != "v2":
        return []
        
    is_agent_skill = "06-agent-hooks-and-skills/skills" in filepath.as_posix()
    
    # Check frontmatter keys
    if is_agent_skill:
        if "name" not in frontmatter:
            errors.append("Missing 'name' in frontmatter")
        if "description" not in frontmatter:
            errors.append("Missing 'description' in frontmatter")
    else:
        if "title" not in frontmatter:
            errors.append("Missing 'title' in frontmatter")
        if "description" not in frontmatter:
            errors.append("Missing 'description' in frontmatter")
            
    # Check description length
    desc = frontmatter.get("description", "")
    if len(desc) == 0:
        errors.append("Description is empty")
    elif len(desc) > 200:
        errors.append(f"Description is too long ({len(desc)} > 200 chars)")
        
    # Check H2s
    h2s = [h.strip() for h in extract_h2(body)]
    expected_h2s = AGENT_SKILL_H2 if is_agent_skill else KNOWLEDGE_H2
    
    for expected in expected_h2s:
        if expected not in h2s:
            errors.append(f"Missing mandatory H2: '## {expected}'")
            
    return errors

def main():
    has_errors = False
    count = 0
    
    for filepath in ROOT_DIR.rglob("*.md"):
        if ".agents" in filepath.parts or "node_modules" in filepath.parts:
            continue
            
        if filepath.name.startswith("_TEMPLATE"):
            continue
            
        errors = lint_file(filepath)
        if errors:
            print(f"[FAIL] {filepath.relative_to(ROOT_DIR)}")
            for e in errors:
                print(f"   - {e}")
            has_errors = True
        else:
            # We don't print anything for passing files to reduce noise, unless we want to
            pass
            
    if has_errors:
        sys.exit(1)
    else:
        print("[PASS] All v2 files passed linting.")
        sys.exit(0)

if __name__ == "__main__":
    main()
