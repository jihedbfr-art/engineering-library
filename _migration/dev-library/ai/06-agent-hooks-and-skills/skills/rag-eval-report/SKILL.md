---
name: rag-eval-report
description: Turn raw output from a retrieval or generation eval run (hit rate per case, pass/fail per scoring method, retrieved sources) into a structured report that separates retrieval failures from generation failures and gives a concrete next action. Use when asked to "summarize these eval results", "write an eval report", or after running rag_eval.py / eval_harness.py and needing the output turned into something shareable.
---

# RAG / LLM eval report writer

Raw eval output (a table of pass/fail per case) tells you *that* something regressed, not *what
to do about it*. This skill's job is the second part: cluster failures by root cause, and lead
with the fix that unblocks the most cases, not just list every red row.

## Report structure

1. **Headline numbers first.** Overall pass rate, and — critically for RAG — retrieval hit rate
   reported *separately* from answer-quality rate. A retrieval miss and a bad generation on a
   correctly-retrieved chunk are different bugs with different fixes; a single blended pass rate
   hides which one is actually driving the failures.

2. **Cluster the failures, don't list them.** Group failing cases by apparent root cause:
   - Retrieval misses on a specific document or topic (suggests a chunking or indexing gap for
     that content specifically, not a general retrieval problem)
   - Retrieval hits but answer still wrong (a generation/prompting problem — the right context
     reached the model and it still didn't use it correctly)
   - Consistently unanswerable questions correctly returning "I don't know" (not a failure —
     flag these as a health check passing, per rag-concepts.md's point that a correct refusal is
     the desired outcome for out-of-scope questions, not a bug)

3. **One next action per cluster, ranked by how many cases it would fix.** "Re-chunk the
   onboarding doc, it's the source for 6 of 9 retrieval misses" beats a bullet list of the 9
   individual failing questions with no synthesis.

4. **Trend line if prior runs are available.** A single run's numbers mean less than the delta
   from the last run on the same golden set — call out any case that regressed (passed before,
   fails now) explicitly, since that's the signal most likely to indicate an actual code change
   broke something, versus a golden set that was simply hard from the start.

## Format

```
## Eval Report — [date/run identifier]

**Retrieval hit rate:** X% (Y/Z cases)
**Answer quality rate:** X% (Y/Z cases)

### Failure clusters
1. [Cluster name] — N cases — [root cause] — [recommended fix]
2. ...

### Regressions vs. previous run
- [case] — was passing, now failing — [what changed]

### Recommendation
[The single highest-leverage next action, stated in one sentence.]
```

## What NOT to do

- Don't recommend "add more retries" or "increase top_k" as a generic fix without identifying
  which cluster it would actually address — those are real levers but only for specific root
  causes (top_k helps a genuine recall problem, not a chunking-quality problem).
- Don't treat a single failing run as proof of a regression without checking whether the golden
  set itself changed — a new, harder case added to the set producing one new failure isn't the
  same signal as a previously-passing case starting to fail.
