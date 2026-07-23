# React Essentials

The most popular UI library. Component-based, declarative: you describe what the UI should look like for a given state, React updates the DOM to match.

## Components & JSX

```jsx
function NoteCard({ note, onArchive }) {
  return (
    <article>
      <h3>{note.title}</h3>
      <button onClick={() => onArchive(note.id)}>Archive</button>
    </article>
  );
}
```
- Components are functions returning JSX (HTML-like syntax).
- Data flows **down** via props; events flow **up** via callbacks.
- Component names are Capitalized; props are read-only.

## State & the core hooks

```jsx
import { useState, useEffect } from "react";

function Notes() {
  const [notes, setNotes] = useState([]);      // state
  const [loading, setLoading] = useState(true);

  useEffect(() => {                             // side effects (fetch, subscriptions)
    fetch("/api/notes")
      .then(r => r.json())
      .then(data => { setNotes(data); setLoading(false); });
  }, []);                                       // [] = run once on mount

  if (loading) return <p>Loading…</p>;
  return notes.map(n => <NoteCard key={n.id} note={n} />);
}
```

| Hook | Purpose |
|---|---|
| `useState` | local component state |
| `useEffect` | side effects (fetch, timers, subscriptions) — with a dependency array |
| `useContext` | read shared context without prop-drilling |
| `useMemo` / `useCallback` | memoize expensive values / stable function identities |
| `useRef` | mutable value / DOM node that doesn't trigger re-render |

## The rules that prevent 90% of bugs

1. **Never mutate state directly.** Create new objects/arrays:
```jsx
setNotes([...notes, newNote]);            // ✅
setNotes(prev => prev.filter(n => n.id !== id));  // ✅ functional update
notes.push(newNote);                      // ❌ React won't re-render
```
2. **`key` on every list item** — a stable unique id, not the array index (index breaks on reorder/insert).
3. **Hooks only at the top level** — never inside conditions or loops; same order every render.
4. **Dependency arrays are truth** — list every value the effect uses, or you get stale closures.

## Data fetching — don't roll your own

For anything real, use **TanStack Query (React Query)** or the framework's loader (Next.js/Remix): caching, refetching, loading/error states, and deduplication solved for you. Manual `useEffect` fetching is fine for learning, painful in production.

## State management ladder (don't over-reach)

1. `useState` — local state (most cases).
2. Lift state up / `useContext` — shared across a subtree.
3. Server-state library (React Query) — data from the server (this is most "global state").
4. A store (Zustand, Redux Toolkit) — genuinely global client state, only when needed.

Most apps need far less global state than people think — server data belongs in a query cache, not Redux.

## React vs Angular (one line)

**React** is a library — flexible, you assemble your stack. **[Angular](../angular/angular-modern.md)** is a full framework — batteries included, more opinionated. Both are component-based and excellent; the choice is ecosystem/team preference, not capability.
