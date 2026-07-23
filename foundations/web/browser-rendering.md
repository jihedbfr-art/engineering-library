# Browser Rendering & Web Performance

How a browser turns bytes into pixels — and where performance is won or lost.

## The critical rendering path

```
HTML  → parse → DOM  ─┐
                      ├→ Render Tree → Layout (positions/sizes) → Paint → Composite
CSS   → parse → CSSOM ┘
JS    → can modify DOM/CSSOM (and block parsing)
```

1. **DOM** — the tree of elements from HTML.
2. **CSSOM** — the tree of styles from CSS.
3. **Render tree** — visible nodes with their styles.
4. **Layout (reflow)** — compute exact geometry.
5. **Paint** — fill pixels (text, colors, images).
6. **Composite** — layer everything to the screen (GPU).

## Why JS and CSS block

- **CSS is render-blocking** — the browser won't paint until it has the CSSOM (avoids flash of unstyled content). Keep critical CSS small/inline.
- **Synchronous JS is parser-blocking** — a `<script>` stops HTML parsing. Use `defer` (run after parse, in order) or `async` (run whenever ready).

```html
<script src="app.js" defer></script>   <!-- best default for app scripts -->
```

## Core Web Vitals (what Google measures, what users feel)

| Metric | Measures | Good |
|---|---|---|
| **LCP** (Largest Contentful Paint) | loading — main content visible | < 2.5s |
| **INP** (Interaction to Next Paint) | responsiveness to input | < 200ms |
| **CLS** (Cumulative Layout Shift) | visual stability (no jumping) | < 0.1 |

## Performance wins, ranked by impact

1. **Ship less** — smaller JS bundles (code-split, tree-shake, lazy-load). JS is the most expensive byte: it downloads, parses, and executes.
2. **Optimize images** — modern formats (WebP/AVIF), correct sizes, `loading="lazy"`, explicit width/height (stops layout shift).
3. **Cache & CDN** — serve static assets from the edge with long cache headers → see [HTTP caching](http.md).
4. **Reduce render-blocking** — inline critical CSS, defer non-critical JS/CSS.
5. **Avoid layout thrash** — batch DOM reads/writes; animating `transform`/`opacity` skips layout & paint (GPU-composited).
6. **Minimize main-thread work** — long JS tasks freeze interaction; break them up, move heavy work to Web Workers.

## Measuring (don't guess)

- **Lighthouse** (in Chrome DevTools) — audit performance, accessibility, SEO.
- **DevTools Performance tab** — flame chart of what the main thread does.
- **Network tab** — waterfall of what loads when, and what blocks.
- Field data (real users) via the Web Vitals library beats lab data alone.

> Rule: measure first. The bottleneck is rarely where you assume — usually it's an oversized image, a giant JS bundle, or a blocking third-party script.
