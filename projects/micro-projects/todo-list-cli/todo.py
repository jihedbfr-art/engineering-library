#!/usr/bin/env python3
"""Gestionnaire de todo-list en ligne de commande, persistance dans todos.json."""
import argparse
import json
import os
import sys

TODO_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "todos.json")


def load_todos() -> list:
    if not os.path.exists(TODO_FILE):
        return []
    with open(TODO_FILE, "r", encoding="utf-8") as f:
        try:
            return json.load(f)
        except json.JSONDecodeError:
            return []


def save_todos(todos: list) -> None:
    with open(TODO_FILE, "w", encoding="utf-8") as f:
        json.dump(todos, f, indent=2, ensure_ascii=False)


def next_id(todos: list) -> int:
    return max((t["id"] for t in todos), default=0) + 1


def cmd_add(args) -> None:
    todos = load_todos()
    todo = {"id": next_id(todos), "text": args.text, "done": False}
    todos.append(todo)
    save_todos(todos)
    print(f"Ajouté #{todo['id']}: {todo['text']}")


def cmd_list(args) -> None:
    todos = load_todos()
    if not todos:
        print("Aucune tâche.")
        return
    for t in todos:
        if args.pending_only and t["done"]:
            continue
        status = "[x]" if t["done"] else "[ ]"
        print(f"{status} #{t['id']}: {t['text']}")


def cmd_done(args) -> None:
    todos = load_todos()
    for t in todos:
        if t["id"] == args.id:
            t["done"] = True
            save_todos(todos)
            print(f"Terminé #{t['id']}: {t['text']}")
            return
    print(f"Erreur: tâche #{args.id} introuvable.", file=sys.stderr)
    sys.exit(1)


def cmd_remove(args) -> None:
    todos = load_todos()
    filtered = [t for t in todos if t["id"] != args.id]
    if len(filtered) == len(todos):
        print(f"Erreur: tâche #{args.id} introuvable.", file=sys.stderr)
        sys.exit(1)
    save_todos(filtered)
    print(f"Supprimé #{args.id}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Gestionnaire de todo-list simple (persistance JSON locale).")
    subparsers = parser.add_subparsers(dest="command", required=True)

    add_parser = subparsers.add_parser("add", help="Ajouter une tâche")
    add_parser.add_argument("text", help="Description de la tâche")
    add_parser.set_defaults(func=cmd_add)

    list_parser = subparsers.add_parser("list", help="Lister les tâches")
    list_parser.add_argument("--pending-only", action="store_true", help="N'afficher que les tâches non terminées")
    list_parser.set_defaults(func=cmd_list)

    done_parser = subparsers.add_parser("done", help="Marquer une tâche comme terminée")
    done_parser.add_argument("id", type=int, help="ID de la tâche")
    done_parser.set_defaults(func=cmd_done)

    remove_parser = subparsers.add_parser("remove", help="Supprimer une tâche")
    remove_parser.add_argument("id", type=int, help="ID de la tâche")
    remove_parser.set_defaults(func=cmd_remove)

    args = parser.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
