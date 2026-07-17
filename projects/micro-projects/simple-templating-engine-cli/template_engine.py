import re


def render(template: str, context: dict) -> str:
    def replace(match):
        key = match.group(1).strip()
        return str(context.get(key, f"{{{{{key}}}}}"))

    return re.sub(r"\{\{(.+?)\}\}", replace, template)


if __name__ == "__main__":
    template = "Bonjour {{ name }}, vous avez {{ count }} nouveaux messages."
    print(render(template, {"name": "Jihed", "count": 5}))
