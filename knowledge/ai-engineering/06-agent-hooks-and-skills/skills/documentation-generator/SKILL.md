---
name: documentation-generator
description: Generate or update documentation (README, API reference, module doc) from code, prioritizing what a new reader actually needs to get productive over exhaustive restatement of the code. Use when asked to "write docs for this", "generate a README", "document this module", or "update the docs to match this change".
---

# Documentation generator

The failure mode this skill exists to avoid: documentation that restates the code in prose
("this function takes a string and returns a boolean") instead of explaining what a reader
actually needs and can't get from reading the code themselves — why it exists, when to use it,
what the non-obvious constraints are.

## What good documentation answers, in order

1. **What is this, in one sentence?** Not a feature list — what problem does it solve, for whom.
2. **How do I use it, fastest path?** A copy-pasteable example that works, before any conceptual
   explanation — most readers want to try it before they want to understand it deeply.
3. **What are the non-obvious constraints or gotchas?** The things that aren't visible from the
   function signature or class name: side effects, thread-safety, what happens on invalid input,
   what it deliberately doesn't handle.
4. **Where does this fit in the bigger picture?** Links to related modules/concepts, not an
   isolated island of documentation nobody can navigate to or from.

## Process

1. Read the actual code, not just its name/signature — the goal is documenting real behavior,
   including edge cases and error handling actually implemented, not the behavior implied by a
   well-named function that might not fully live up to its name.
2. Identify what's genuinely non-obvious: a well-named, simple function needs almost no prose; a
   function with subtle preconditions, side effects, or surprising behavior needs more.
3. Write the quick-start example first, verify it would actually work as written (correct
   imports, correct parameter names, no invented API).
4. Add the "why" and gotchas section only where there's real substance — an empty "Notes" section
   with nothing in it is worse than no section at all.

## Format guidance

- Lead with usage, not architecture — architecture/design rationale belongs after the reader
  already knows how to use the thing, or in a separate design-doc-style section.
- Prefer a working code example over a paragraph describing what the code example would show.
- Match the existing documentation style/voice already used in the codebase if there is one —
  introducing a new formatting convention for one module creates inconsistency, not clarity.
- State explicitly what the code does NOT do, when that's a common point of confusion — an
  explicit non-goal often saves more reader time than another paragraph of what it does do.

## What NOT to do

- Don't generate a docstring or README section for every single function/class uniformly —
  trivial, self-explanatory code (a simple getter, an obvious constructor) doesn't need
  documentation; reserve real effort for what actually needs explaining.
- Don't invent behavior, parameters, or return values not present in the actual code — if
  something is ambiguous from the code alone, say so rather than guessing confidently.
- Don't write documentation as a wall of prose when a table or short code example would answer
  the same question faster.
