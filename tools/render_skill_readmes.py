import os
import sys
import argparse
import re

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')


try:
    import yaml
except ImportError:
    # There used to be a naive line-based fallback here. It parsed
    # `audience: ["a", "b"]` as the literal string instead of a list, so the
    # renderer emitted `["a", "b"]` where the PyYAML path emits `a, b`. Every
    # generated README then looked stale to --check, which is exactly what
    # happened on CI once this script started running there without PyYAML
    # installed. A fallback that changes the output is not a fallback.
    sys.exit(
        "PyYAML is required to render skill READMEs (pip install pyyaml).\n"
        "Refusing to run without it: the output would differ from the committed files."
    )

def parse_frontmatter(content):
    if not content.startswith("---"):
        return {}
    parts = content.split("---", 2)
    if len(parts) < 3:
        return {}
    yaml_text = parts[1]

    try:
        return yaml.safe_load(yaml_text) or {}
    except Exception:
        return {}

def find_logo_relative_path(skill_dir):
    # Ascend until assets/brand/jihedailabs-logo.{svg,png} is found, or compute a
    # relative fallback pointing at the repo root (however many levels up that is).
    curr = skill_dir
    depth = 0
    while curr and depth < 10:
        for ext in ("svg", "png"):
            target = os.path.join(curr, "assets", "brand", f"jihedailabs-logo.{ext}")
            if os.path.exists(target):
                rel = os.path.relpath(target, skill_dir).replace("\\", "/")
                return rel
        parent = os.path.dirname(curr)
        if parent == curr:
            break
        curr = parent
        depth += 1

    # Fallback: assume assets/ lives at the walked root, computed from actual depth.
    up = "/".join([".."] * max(depth, 1))
    return f"{up}/assets/brand/jihedailabs-logo.svg"

def render_readme_content(fm, lang, skill_dir):
    logo_rel = find_logo_relative_path(skill_dir)
    title = fm.get("title", fm.get("name", "Skill"))
    title_fr = fm.get("title_fr", title)
    desc = fm.get("description", "")
    desc_fr = fm.get("description_fr", desc)
    domain = fm.get("domain", "N/A")
    maturity = fm.get("maturity", "stable")
    audience = fm.get("audience", [])
    if isinstance(audience, list):
        audience_str = ", ".join(audience)
    else:
        audience_str = str(audience)
        
    requires = fm.get("requires", [])
    if isinstance(requires, list):
        requires_str = ", ".join(requires)
    else:
        requires_str = str(requires)
        
    updated = fm.get("updated", "2026-08-08")
    
    if lang == "fr":
        return f"""<!-- GENERATED — do not edit -->
<div align="center">
  <img src="{logo_rel}" alt="JihedAiLabs" width="120"/>
</div>

# {title_fr}

<div align="center">

**Un projet <a href="https://github.com/jihedbfr-art">JihedAiLabs</a>** — {desc_fr}

<a href="./README.md">English version</a>

</div>

---

## Description
{desc_fr}

## Domaine & Metadata
- **Domaine :** `{domain}`
- **Maturité :** `{maturity}`
- **Public visé :** `{audience_str}`
- **Prérequis techniques :** `{requires_str}`
- **Dernière mise à jour :** {updated}

## Instructions Agent
Le fichier canonique consommable par un agent IA (`Claude Code`, `Antigravity`, `Cursor`) est disponible dans [SKILL.md](./SKILL.md).
"""
    else:
        return f"""<!-- GENERATED — do not edit -->
<div align="center">
  <img src="{logo_rel}" alt="JihedAiLabs" width="120"/>
</div>

# {title}

<div align="center">

**A <a href="https://github.com/jihedbfr-art">JihedAiLabs</a> project** — {desc}

<a href="./README.fr.md">Version française</a>

</div>

---

## Description
{desc}

## Domain & Metadata
- **Domain:** `{domain}`
- **Maturity:** `{maturity}`
- **Audience:** `{audience_str}`
- **Requirements:** `{requires_str}`
- **Last Updated:** {updated}

## Agent Instructions
The canonical agent-executable specification is available in [SKILL.md](./SKILL.md).
"""

def main():
    parser = argparse.ArgumentParser(description="Render human README.md and README.fr.md from SKILL.md")
    parser.add_argument("path", nargs="?", default=r"e:\jihedapps\GitHub", help="Root directory")
    parser.add_argument("--check", action="store_true", help="Check for drift without writing")
    args = parser.parse_args()

    drift_detected = False
    rendered_count = 0

    for root, dirs, files in os.walk(args.path):
        if "SKILL.md" in files:
            skill_file = os.path.join(root, "SKILL.md")
            try:
                with open(skill_file, "r", encoding="utf-8", errors="ignore") as f:
                    content = f.read()
                fm = parse_frontmatter(content)
                if not fm:
                    continue
                
                # Render EN & FR
                readme_en = render_readme_content(fm, "en", root)
                readme_fr = render_readme_content(fm, "fr", root)

                target_en_path = os.path.join(root, "README.md")
                target_fr_path = os.path.join(root, "README.fr.md")

                for t_path, content_to_write in [(target_en_path, readme_en), (target_fr_path, readme_fr)]:
                    if os.path.exists(t_path):
                        with open(t_path, "r", encoding="utf-8", errors="ignore") as f_ex:
                            existing = f_ex.read()
                        if existing.strip() != content_to_write.strip():
                            if args.check:
                                print(f"❌ DRIFT: {os.path.relpath(t_path, args.path)} is out of date.")
                                drift_detected = True
                            else:
                                with open(t_path, "w", encoding="utf-8") as f_out:
                                    f_out.write(content_to_write)
                                print(f"📝 UPDATED: {os.path.relpath(t_path, args.path)}")
                    else:
                        if args.check:
                            print(f"❌ MISSING: {os.path.relpath(t_path, args.path)}")
                            drift_detected = True
                        else:
                            with open(t_path, "w", encoding="utf-8") as f_out:
                                f_out.write(content_to_write)
                            print(f"✨ CREATED: {os.path.relpath(t_path, args.path)}")
                            
                rendered_count += 1
            except Exception as e:
                print(f"Error processing {skill_file}: {e}")

    if args.check:
        if drift_detected:
            print("\n❌ README drift detected! Run 'python tools/render_skill_readmes.py' to fix.")
            sys.exit(1)
        else:
            print(f"\n✅ All {rendered_count} skill READMEs are up to date.")
            sys.exit(0)
    else:
        print(f"\n✅ Successfully processed READMEs for {rendered_count} skill(s).")

if __name__ == "__main__":
    main()
