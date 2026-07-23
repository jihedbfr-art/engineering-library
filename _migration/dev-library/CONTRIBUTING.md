# Contributing

Thanks for wanting to make this library better. The bar is simple: **practical, correct, concise.**

## What belongs here

- Concepts explained with a working example.
- Hard-won practical advice (the "gotcha" that cost someone a day).
- Cheatsheets you'd actually keep open in a second tab.

## What doesn't

- Copy-pasted vendor marketing.
- Walls of theory with no example.
- Anything you can't verify or wouldn't run yourself.

## Style guide

1. **Lead with the useful thing.** No long intros — the reader is busy.
2. **Show code, then explain it** — not the reverse.
3. **Tables for comparisons**, code blocks for commands, prose only when it earns its place.
4. **One file = one topic.** If it needs a scroll bar the size of a novel, split it.
5. Prefer **"do this / not that"** over abstract principles.
6. Link related pages with relative links so navigation stays intact.

## Adding a page

1. Find the right section (`devsecops/`, `cybersecurity/`, `ai/`, `backend/`, `frontend/`, `databases/`, `resources/`).
2. Add your `.md` file; link it from that section's `README.md`.
3. Keep commit messages conventional: `section: short imperative summary`.

## Commit message format

```
devsecops: add ArgoCD GitOps guide
cheatsheet: add jq recipes
fix: correct JWT audience validation example
```

## Ground rules

- No secrets, no real credentials, ever — not even in examples (use obvious placeholders).
- Security content is **defensive and for authorized use only**. No "how to attack someone" material.
- If you're unsure whether something is correct, say so in the PR rather than presenting a guess as fact.
