# Working With Legacy Code (No Tests, No Docs, Still in Production)

Not every legacy engagement is a full migration. Often the job is smaller and more immediate: **change this 10-year-old code, correctly, without a test suite, without full documentation, without breaking the production system it's part of right now.** A specific, learnable skill — not just "be careful."

## The first rule: don't refactor and fix at the same time

The single most common way to turn a small legacy change into an incident: fixing a bug *and* cleaning up the surrounding code in the same change. If something breaks, you now can't tell whether it was the fix or the refactor — and with no test suite to isolate the question, you're debugging blind, in production, with two changes tangled together instead of one. **Fix the bug first, ship it, refactor separately afterward if it's still worth doing.**

## Characterization tests — the tool that unlocks everything else

You can't safely change code you don't understand, and you can't build understanding by reading alone when the logic is genuinely tangled. A **characterization test** doesn't test that the code is *correct* — it tests that the code does *exactly what it currently does*, as a safety net for the change you're about to make:

```java
// I don't fully understand this PL/SQL-adjacent business rule yet.
// I'm capturing its CURRENT behavior before I touch anything near it.
@Test
void characterization_currentTaxCalculationBehavior() {
    var result = legacyTaxCalculator.calculate(sampleDeclaration());
    // Whatever it currently outputs — not what I think is "correct" —
    // becomes the value I assert here:
    assertThat(result.getAmount()).isEqualTo(new BigDecimal("1234.56"));
}
```
Once this test exists, you can refactor or extend the surrounding code with actual confidence — if the characterization test still passes, you haven't silently changed a behavior that some subscriber, client, or tax office segment quietly depends on, whether or not you ever fully understood *why* that behavior exists.

## Finding seams — where you can safely insert a test without a rewrite

A "seam" (Michael Feathers' term, and still the right one) is a place where you can alter behavior *without editing the code at that exact spot* — usually by extracting an interface around an external dependency:

```java
// Before: impossible to test without a live Oracle connection
public class TaxCalculator {
    BigDecimal calculate(Declaration d) {
        Connection conn = DriverManager.getConnection(ORACLE_URL);
        // ... tightly coupled to the live database
    }
}

// After: a seam — same behavior, now testable in isolation
public class TaxCalculator {
    private final DeclarationRepository repo;   // interface — can be faked in a test
    TaxCalculator(DeclarationRepository repo) { this.repo = repo; }
    BigDecimal calculate(Declaration d) { /* same logic, now testable */ }
}
```
This single extraction — introducing an interface around the hard-to-test dependency — is usually the highest-leverage first move in legacy code: it doesn't change behavior, and it's what makes every subsequent characterization test actually possible to write.

## Reading legacy code strategically, not linearly

Reading a 15-year-old, undocumented module top-to-bottom rarely builds real understanding efficiently. What works better:

1. **Trace one real transaction end-to-end** through the system, following the actual data — not the code structure — for a concrete, real input.
2. **Diff against known-correct outputs** — feed known inputs, compare outputs against what the system is documented (or known from experience) to currently produce.
3. **Talk to whoever's touched it most recently or most often** — a five-minute conversation with someone who fixed a bug in this module last year routinely surfaces more real understanding than an hour of solo code reading, because it captures tribal knowledge that was never written down anywhere.

## The "don't touch it" instinct — when it's right, and when it's an excuse

Legacy code that's stable, poorly understood, and rarely needs changes is often genuinely fine left alone — "if it ain't broke" has real, defensible merit for code you rarely touch and don't need to extend. It stops being valid the moment you actually need to change that code — at that point, "we're scared to touch it" isn't a strategy, it's a description of technical debt with real, present cost, and the characterization-test approach above exists specifically to make touching it safe.

## Practical checklist before changing unfamiliar legacy code

- [ ] Can I trace at least one real transaction through this code, end to end?
- [ ] Do I have (or can I quickly build) a characterization test for the specific behavior I'm about to touch?
- [ ] Have I separated "fix the bug" from "clean up the code" into two distinct changes?
- [ ] Is there a person who's touched this recently I should talk to before I start, not after I'm stuck?
- [ ] Do I have a fast, realistic rollback path if this change behaves unexpectedly in production?

## Where this connects

This is the tactical, code-level companion to [legacy-migration-playbook](legacy-migration-playbook.md)'s project-level discipline — that page is about migrating a whole system safely; this one is about safely changing a piece of it today, whether or not a larger migration is even happening. The underlying instinct is identical: **assume the existing behavior is meaningful until proven otherwise, and validate before you trust your own understanding of "correct."**
