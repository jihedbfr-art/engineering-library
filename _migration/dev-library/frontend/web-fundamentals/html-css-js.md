# Web Fundamentals — HTML, CSS, JavaScript

Frameworks come and go; the platform underneath is forever. Master these and every framework is easier.

## HTML — structure & semantics

Use elements for their **meaning**, not their looks:
```html
<header> <nav> <main> <article> <section> <aside> <footer>
<button> for actions · <a> for navigation · <label> for inputs
```
- **Semantic HTML is free accessibility & SEO** — a `<button>` is focusable, keyboard-operable, and announced correctly; a clickable `<div>` is none of those.
- Every input has a `<label>`. Every image has meaningful `alt` (or `alt=""` if decorative).
- One `<h1>` per page; headings nest in order (don't skip levels for size).

## CSS — layout & styling

### The box model
Every element is a box: `content → padding → border → margin`. `box-sizing: border-box` makes width include padding+border (set it globally — it's saner).

### Modern layout (stop using floats/tables)
```css
/* Flexbox — one dimension (a row or column) */
.toolbar { display: flex; gap: 1rem; align-items: center; justify-content: space-between; }

/* Grid — two dimensions (rows AND columns) */
.gallery { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1rem; }
```
Flexbox for a line of things; Grid for a real 2D layout. Together they replace nearly all old hacks.

### Specificity & the cascade
Which rule wins: inline > id > class > element. Keep specificity low and flat (prefer classes); high-specificity wars end in `!important`, which you'll regret. Modern approaches: utility classes, CSS modules, or scoped styles.

### Responsive design
```css
/* Mobile-first: base styles for small, enhance for larger */
.card { width: 100%; }
@media (min-width: 768px) { .card { width: 50%; } }
```
Use relative units (`rem`, `%`, `fr`, `vh`), `max-width: 100%` on media, and test at real breakpoints.

## JavaScript — behavior

The essentials of the language are in [JS/TS](../../programming/javascript-typescript.md). On the page specifically:

```js
// Select & manipulate
const btn = document.querySelector("#save");
btn.addEventListener("click", () => { /* ... */ });

// Event delegation — one listener for many children
list.addEventListener("click", e => {
  if (e.target.matches(".delete")) remove(e.target.dataset.id);
});
```
- Manipulating the DOM directly is fine for small things; for apps, a framework ([React](../react/react-essentials.md)/[Angular](../angular/angular-modern.md)) manages it for you.
- Keep logic out of the markup; separate structure (HTML), presentation (CSS), behavior (JS).

## Accessibility (a11y) — not optional

- Keyboard-navigable (everything reachable and operable without a mouse).
- Sufficient color contrast (WCAG AA: 4.5:1 for text).
- Semantic HTML first; ARIA only to fill genuine gaps, never as a substitute.
- Respects `prefers-reduced-motion` and works with screen readers.

Accessible sites are better for *everyone* — and required by law in many places.

## Performance basics

Covered in depth in [browser rendering](../../web/browser-rendering.md): ship less JS, optimize images, avoid layout shift, defer non-critical work. The fastest code is the code you don't send.
