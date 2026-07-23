# Transactions & Concurrency — Deep Dive

[SQL essentials](sql-essentials.md) covers ACID and isolation levels at a glance. This is where the actual bugs live.

## ACID, but with the part that bites people

- **Atomicity**: all-or-nothing. Easy to understand, rarely the source of bugs.
- **Consistency**: the DB's own invariants (constraints, FKs) always hold. Also rarely the issue.
- **Isolation**: how much concurrent transactions can see of each other's in-progress work. **This is where 90% of concurrency bugs live.**
- **Durability**: once committed, survives a crash. An infra concern, not usually an app-code bug.

## The isolation levels, with concrete failure examples

### Dirty read (prevented by READ COMMITTED and above)
```
T1: UPDATE accounts SET balance = 0 WHERE id = 1;   -- not committed yet
T2: SELECT balance FROM accounts WHERE id = 1;       -- sees 0?! (dirty read)
T1: ROLLBACK;                                        -- balance was never actually 0
```
T2 acted on data that never really existed. Almost no modern default allows this (PostgreSQL never does, even at its lowest level).

### Non-repeatable read (prevented by REPEATABLE READ and above)
```
T1: SELECT balance FROM accounts WHERE id = 1;   -- reads 100
T2: UPDATE accounts SET balance = 50 WHERE id = 1; COMMIT;
T1: SELECT balance FROM accounts WHERE id = 1;   -- reads 50 -- different answer, same transaction!
```
Same query, same transaction, two different answers. Dangerous when your logic assumes a value hasn't changed mid-transaction.

### Phantom read (prevented by SERIALIZABLE)
```
T1: SELECT count(*) FROM orders WHERE status = 'pending';   -- 5 rows
T2: INSERT INTO orders (status) VALUES ('pending'); COMMIT;
T1: SELECT count(*) FROM orders WHERE status = 'pending';   -- 6 rows -- a "phantom" appeared
```
Not a changed row — a **new** row matching your filter appeared mid-transaction.

## Default isolation levels — know your database's default, it varies

| Database | Default level |
|---|---|
| PostgreSQL | READ COMMITTED |
| MySQL (InnoDB) | REPEATABLE READ |
| SQL Server | READ COMMITTED |
| Oracle | READ COMMITTED |

Most apps never change this — and mostly get away with it, until a race condition in a high-concurrency path (checkout, inventory, balance updates) causes a very hard-to-reproduce bug that only shows up under load.

## Locking — pessimistic vs optimistic

**Pessimistic** — lock the row, block other writers until you're done:
```sql
BEGIN;
SELECT * FROM accounts WHERE id = 1 FOR UPDATE;   -- locks this row
UPDATE accounts SET balance = balance - 100 WHERE id = 1;
COMMIT;   -- lock released
```
Correct and simple, but concurrent writers to the same row queue up — a real throughput cost under contention.

**Optimistic** — don't lock, detect conflicts at write time via a version column:
```sql
UPDATE accounts SET balance = balance - 100, version = version + 1
WHERE id = 1 AND version = 7;
-- 0 rows updated? Someone else changed it first — reload and retry.
```
Better throughput when conflicts are rare; requires your app to handle the retry path — a real design decision, not a footnote.

## The classic race condition (and why `SELECT` then `UPDATE` is a trap)

```python
# WRONG — race condition under concurrent requests
balance = db.query("SELECT balance FROM accounts WHERE id = ?", id)
if balance >= amount:
    db.execute("UPDATE accounts SET balance = balance - ? WHERE id = ?", amount, id)
# Two concurrent requests can both read balance=100, both pass the check,
# both deduct — final balance is wrong, and you didn't get an error to tell you.
```
```sql
-- RIGHT — the database enforces it atomically, in one statement
UPDATE accounts SET balance = balance - :amount
WHERE id = :id AND balance >= :amount;
-- check affected_rows == 1 to know if it actually happened
```
This single rewrite — doing the check *inside* the atomic write instead of before it — fixes an entire category of bugs that only show up under real concurrent load, which is exactly why they're so easy to ship without noticing in dev/testing.

## Deadlocks — when two transactions wait on each other forever

```
T1: locks row A, wants row B
T2: locks row B, wants row A
→ neither can proceed. The database detects this and kills one (deadlock victim).
```
**Prevention**: always acquire locks in the same, consistent order across your whole codebase (e.g. always lock the lower ID first). Your app must catch the deadlock error and retry — it's not a rare edge case in a busy system, it's an expected occasional event you design for.

## Practical rules that prevent most of this

1. Keep transactions **short** — no HTTP calls, no user-wait-time inside a transaction.
2. Prefer a single atomic `UPDATE ... WHERE` over read-then-write when possible.
3. Use `SELECT ... FOR UPDATE` deliberately when you must read-then-write, not by accident.
4. Know your database's default isolation level — don't assume it matches what you learned elsewhere.
5. Design for retry on deadlock/conflict — it's an expected event under load, not an outage.
