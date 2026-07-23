# JavaScript & TypeScript

JavaScript runs everywhere (browser, server via Node/Deno/Bun, edge). TypeScript adds a type system on top — use it for anything beyond a script.

## Modern JavaScript essentials

```js
// const by default, let when reassigning, never var
const PI = 3.14;
let count = 0;

// Arrow functions
const add = (a, b) => a + b;

// Destructuring & spread
const { name, age } = user;
const [first, ...rest] = arr;
const merged = { ...defaults, ...overrides };

// Array methods (learn these cold)
items.map(x => x * 2)
     .filter(x => x > 3)
     .reduce((sum, x) => sum + x, 0);
items.find(x => x.id === 5);
items.some(...) / items.every(...);

// Optional chaining & nullish coalescing
const city = user?.address?.city ?? "unknown";
```

## Async — the heart of JS

```js
// Promises → async/await
async function load() {
  try {
    const res = await fetch("/api/notes");
    if (!res.ok) throw new Error(res.status);
    return await res.json();
  } catch (e) {
    console.error("load failed", e);
  }
}

// Parallel, not sequential
const [a, b] = await Promise.all([fetchA(), fetchB()]);
```
The event loop is single-threaded: `await` doesn't block the thread, it yields. Never do heavy CPU work on it.

## TypeScript — the sanity layer

```ts
type User = { id: number; name: string; active?: boolean };

function greet(u: User): string {
  return `Hi ${u.name}`;
}

// Unions & narrowing
type Result = { ok: true; data: string } | { ok: false; error: string };
function handle(r: Result) {
  if (r.ok) console.log(r.data);   // TS knows data exists here
  else console.log(r.error);
}

// Generics
function first<T>(arr: T[]): T | undefined { return arr[0]; }
```
- Prefer `type`/`interface` over `any` — `any` disables the whole point.
- Enable `strict: true`. Turn on `noUncheckedIndexedAccess` for real safety.
- Types are erased at runtime — validate external data (zod) at boundaries.

## Gotchas

- **`==` vs `===`**: always use `===` (strict). `==` does surprising coercion (`0 == ""` is true).
- **`this`** depends on how a function is *called*; arrow functions capture the surrounding `this`.
- **Floating point**: `0.1 + 0.2 !== 0.3`. Use integers (cents) for money.
- **Mutating shared arrays/objects** — copy with spread before changing in reactive frameworks.

## Tooling baseline

```bash
npm ci                 # reproducible install from lockfile
npx tsc --noEmit       # typecheck
npx eslint . / npx prettier --write .
npm test               # vitest / jest
```
Runtimes: **Node** (standard), **Bun** (fast all-in-one), **Deno** (secure, TS-native).
