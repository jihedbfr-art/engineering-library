import json
import sqlite3
import uuid


def setup(conn: sqlite3.Connection):
    conn.execute("CREATE TABLE orders (id TEXT PRIMARY KEY, status TEXT)")
    conn.execute("CREATE TABLE outbox (id TEXT PRIMARY KEY, event_type TEXT, payload TEXT, published INTEGER DEFAULT 0)")


def place_order(conn: sqlite3.Connection, order_id: str):
    with conn:
        conn.execute("INSERT INTO orders VALUES (?, 'CREATED')", (order_id,))
        event = {"order_id": order_id, "status": "CREATED"}
        conn.execute(
            "INSERT INTO outbox VALUES (?, 'OrderCreated', ?, 0)",
            (str(uuid.uuid4()), json.dumps(event)),
        )


def relay_outbox(conn: sqlite3.Connection):
    rows = conn.execute("SELECT id, event_type, payload FROM outbox WHERE published = 0").fetchall()
    for row_id, event_type, payload in rows:
        print(f"[relay] publishing {event_type}: {payload}")
        conn.execute("UPDATE outbox SET published = 1 WHERE id = ?", (row_id,))
    conn.commit()


if __name__ == "__main__":
    conn = sqlite3.connect(":memory:")
    setup(conn)
    place_order(conn, "order-42")
    relay_outbox(conn)
