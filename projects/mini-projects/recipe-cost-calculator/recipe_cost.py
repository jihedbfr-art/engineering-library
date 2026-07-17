import argparse
import json


def compute_cost(recipe: dict, servings: int) -> dict:
    total = sum(ing["quantity"] * ing["unit_price"] for ing in recipe["ingredients"])
    per_serving = total / recipe["servings"]
    return {
        "total_recipe": round(total, 2),
        "per_serving_original": round(per_serving, 2),
        "cost_for_requested_servings": round(per_serving * servings, 2),
    }


def main():
    parser = argparse.ArgumentParser(description="Calcule le cout d'une recette a partir d'un JSON d'ingredients")
    parser.add_argument("recipe_file")
    parser.add_argument("-s", "--servings", type=int, help="nombre de portions souhaitees")
    args = parser.parse_args()

    with open(args.recipe_file) as f:
        recipe = json.load(f)

    servings = args.servings or recipe["servings"]
    result = compute_cost(recipe, servings)
    for key, value in result.items():
        print(f"{key}: {value}")


if __name__ == "__main__":
    main()
