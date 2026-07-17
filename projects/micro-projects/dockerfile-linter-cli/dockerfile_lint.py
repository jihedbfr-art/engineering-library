import argparse


def lint(lines: list[str]) -> list[str]:
    warnings = []
    has_user = any(l.strip().upper().startswith("USER") for l in lines)
    if not has_user:
        warnings.append("Aucune instruction USER : le conteneur tournera en root par defaut")
    for i, line in enumerate(lines, 1):
        stripped = line.strip()
        if stripped.upper().startswith("FROM") and ":latest" in stripped:
            warnings.append(f"Ligne {i}: tag ':latest' - preferer une version epinglee")
        if stripped.upper().startswith("ADD") and not stripped.startswith("ADD --"):
            warnings.append(f"Ligne {i}: ADD utilise - preferer COPY sauf besoin reel (extraction/URL)")
    return warnings


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Lint basique d'un Dockerfile (bonnes pratiques securite/taille)")
    parser.add_argument("dockerfile")
    args = parser.parse_args()
    with open(args.dockerfile) as f:
        lines = f.readlines()
    warnings = lint(lines)
    if warnings:
        for w in warnings:
            print(f"WARN: {w}")
    else:
        print("Aucun avertissement.")
