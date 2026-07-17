def snake_to_camel(s: str) -> str:
    parts = s.split("_")
    return parts[0] + "".join(p.capitalize() for p in parts[1:])


if __name__ == "__main__":
    s = input("snake_case : ")
    print(snake_to_camel(s))
