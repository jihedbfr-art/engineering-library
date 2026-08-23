import os
import sys
import argparse
import re

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')


def check_bilingual_parity(root_path):
    missing_fr = []
    missing_cross_links = []
    total_checked = 0

    for root, dirs, files in os.walk(root_path):
        if "README.md" in files:
            total_checked += 1
            readme_en = os.path.join(root, "README.md")
            readme_fr = os.path.join(root, "README.fr.md")

            if not os.path.exists(readme_fr):
                missing_fr.append(os.path.relpath(readme_fr, root_path))
            else:
                # Check cross-links
                with open(readme_en, "r", encoding="utf-8", errors="ignore") as f:
                    content_en = f.read()
                with open(readme_fr, "r", encoding="utf-8", errors="ignore") as f:
                    content_fr = f.read()

                if "README.fr.md" not in content_en:
                    missing_cross_links.append(f"{os.path.relpath(readme_en, root_path)} -> missing link to README.fr.md")
                if "README.md" not in content_fr:
                    missing_cross_links.append(f"{os.path.relpath(readme_fr, root_path)} -> missing link to README.md")

    return total_checked, missing_fr, missing_cross_links

def main():
    parser = argparse.ArgumentParser(description="Check FR/EN bilingual parity and cross-links across READMEs.")
    parser.add_argument("path", nargs="?", default=r"e:\jihedapps\GitHub", help="Root directory")
    args = parser.parse_args()

    print(f"--- Checking bilingual parity under '{args.path}' ---")
    total, missing_fr, missing_links = check_bilingual_parity(args.path)

    has_errors = False

    if missing_fr:
        has_errors = True
        print(f"\n❌ Missing README.fr.md ({len(missing_fr)} file(s)):")
        for m in missing_fr:
            print(f"  - {m}")

    if missing_links:
        has_errors = True
        print(f"\n❌ Missing cross-language links ({len(missing_links)} file(s)):")
        for ml in missing_links:
            print(f"  - {ml}")

    if not has_errors:
        print(f"\n✅ All {total} README pairs have 100% bilingual parity and cross-links!")
        sys.exit(0)
    else:
        print(f"\nSummary: Parity issues found across {len(missing_fr) + len(missing_links)} check(s).")
        sys.exit(1)

if __name__ == "__main__":
    main()
