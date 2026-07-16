# Data Modeling

The schema is the foundation everything else stands on. A good model makes features easy; a bad one makes every query a fight.

## Entities, relationships, keys

- **Entity** → a table (User, Note, Notebook).
- **Attribute** → a column (name, created_at).
- **Primary key** → unique row identifier. Prefer a surrogate key (auto `id` / UUID) over natural keys that can change.
- **Foreign key** → a reference to another table's PK. Enforce it in the DB for integrity.

## Relationships

```
One-to-many  (a notebook has many notes)
  notes.notebook_id → notebooks.id

Many-to-many (notes ↔ tags)  → needs a join table
  note_tags(note_id, tag_id)

One-to-one   (user ↔ profile) → FK with a unique constraint
```

## Normalization (organize to avoid duplication)

Progressive rules to eliminate redundant data:

- **1NF** — atomic values, no repeating groups (no comma-lists in a column).
- **2NF** — no partial dependency on part of a composite key.
- **3NF** — no column depends on a non-key column (no derived/duplicated facts).

Practical version: **every fact lives in exactly one place.** A customer's email is stored once, referenced by id everywhere else. Change it once, correct everywhere.

## Denormalization (deliberate duplication for speed)

Sometimes you *copy* data to avoid expensive joins on hot read paths — e.g. store `comment_count` on a post instead of counting every time. 

Rule: **normalize first, denormalize only when measured performance demands it** — and then own the cost of keeping copies in sync.

## Every table should have

```sql
id           BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY
created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
-- FKs with ON DELETE behavior chosen on purpose (CASCADE / RESTRICT / SET NULL)
```

## Choices that bite later

| Decision | Guidance |
|---|---|
| **id: int vs UUID** | int = smaller/faster/ordered; UUID = no coordination, safe to expose, distributable. Pick per need. |
| **Money** | `NUMERIC/DECIMAL`, never float. |
| **Timestamps** | `timestamptz`, store UTC, convert on display. |
| **Enums** | a lookup table or a constrained column — not free-text strings. |
| **Nullable?** | be deliberate; `NULL` means "unknown", not "zero" or "empty". |
| **Soft delete** | `deleted_at` only if you truly need history — it complicates every query. |

## Indexing follows access patterns

Model the data, then index for how you actually **query** it (the `WHERE`/`JOIN`/`ORDER BY` columns) → [SQL essentials: indexes](sql-essentials.md). Don't index blindly; each index taxes writes.

## Design process

1. List the **entities** and their **relationships** (draw them — an ER diagram).
2. Define keys and constraints (PKs, FKs, unique, not-null).
3. Normalize to 3NF.
4. Add indexes for the real query patterns.
5. Denormalize only where profiling proves you must.
6. Version every change as a **migration** in git → [migrations](sql-essentials.md).
