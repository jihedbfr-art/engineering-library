# Type Systems

Arguably the single most consequential design decision in any programming language — and the direct explanation for a huge share of the real, daily differences you feel switching between languages, whether or not you've ever consciously thought about it in these terms.

## Static vs dynamic typing — what's actually being checked, and when

```
Static typing (Java, Rust, TypeScript, Go, C):
  Types are checked at COMPILE time, before the program ever runs.
  A type error is caught before deployment — the exact "semantic
  analysis" phase from the compiler-pipeline page.

Dynamic typing (Python, Ruby, JavaScript, classic PHP):
  Types are checked at RUNTIME, as each operation actually executes.
  The same category of error only surfaces when that specific code
  path actually runs — which might be in testing, or might be a
  specific edge case in production, months later.
```
This is the real, concrete tradeoff underneath the eternal "static vs dynamic" debate: static typing catches a real class of bugs *earlier*, at the cost of more upfront ceremony and often more verbose code; dynamic typing trades that earlier safety net for faster iteration and less ceremony, at the cost of some categories of bugs only surfacing when that exact code path actually executes for the first time.

## Strong vs weak typing — a genuinely different axis, often confused with the above

```
Strong typing:  the language refuses to implicitly convert between
                incompatible types without an explicit cast.
                Python: "2" + 2  →  TypeError, refuses to guess your intent

Weak typing:    the language WILL implicitly coerce between types,
                sometimes in famously surprising ways.
                JavaScript: "2" + 2  →  "22" (string concatenation wins)
                            "2" - 1  →  1    (numeric subtraction wins — same operand, opposite type behavior)
```
Static/dynamic is about **when** types are checked; strong/weak is about **how strictly** the language enforces type compatibility once it does check. The two axes are independent — Python is dynamically but fairly strongly typed (it refuses silent coercions); JavaScript is dynamically *and* weakly typed (it eagerly coerces, sometimes unpredictably) — which is exactly why JavaScript's type-coercion quirks have their own extensive body of infamous examples, while Python's dynamic typing rarely produces the same category of surprise.

## Type inference — static safety without the ceremony

```typescript
// Explicit typing
let count: number = 5;

// Inferred typing — the compiler figures out the type from context,
// with IDENTICAL static safety guarantees to the explicit version above
let count = 5;    // TypeScript infers `number` — no less safe, just less typed
```
Modern statically-typed languages (TypeScript, Kotlin, Rust, Go, Swift) lean heavily on type inference specifically to capture *most* of static typing's safety benefit without forcing a developer to annotate every single variable explicitly — a real, substantial ergonomics improvement over older-generation statically typed languages (early Java, C) that required far more explicit annotation for the same underlying safety guarantees.

## What type safety actually buys you, concretely

```java
// A statically-typed compiler catches this BEFORE the code ever runs:
String name = getUserName();
int result = name + 5;    // compile error — caught here, never reaches production

// The dynamically-typed equivalent doesn't fail until this EXACT line
// actually executes at runtime — which might be a rare, untested code path:
name = get_user_name()
result = name + 5    # only fails when this specific line actually runs
```
The real, honest value proposition: static typing shifts a category of bugs from "found in production, possibly by a customer" to "found before the code even compiles." It does **not** catch every bug — logic errors, wrong business rules, off-by-one mistakes all sail straight through a type checker untouched, because they're not type errors at all. Type safety is a genuinely valuable, narrow tool against one specific category of mistake, not a general correctness guarantee, and treating it as the latter is a common, avoidable overconfidence.

## Null/undefined — the "billion dollar mistake," and how modern languages fix it

```
Tony Hoare, who invented the null reference in 1965, later called it his
"billion dollar mistake" — a huge share of real production crashes across
the entire software industry trace back to exactly this one design choice.

Older approach (Java pre-Optional, C, many others):
  Any reference can silently be null. The type system gives you NO
  warning — String name can be an actual string, or it can be null,
  and the type checker can't tell you which, so you find out at
  runtime, via a crash, whenever someone forgets to check.

Modern approach (Kotlin, Rust's Option<T>, TypeScript strict mode):
  Nullability becomes PART of the type itself, and the type checker
  enforces handling it. In Kotlin:
    String        — genuinely CANNOT be null, enforced by the compiler
    String?        — CAN be null, and the compiler forces you to
                     explicitly handle that case before you can use it
```
This single design change — making "can this be absent" part of the type system instead of an unstated, unenforced possibility — eliminates an entire, historically massive category of production crashes at compile time, for free, on every single reference in the codebase. It's one of the clearest, most measurable real-world wins a type system design decision has ever produced across the industry.

## Generics — types parameterized by other types

```java
List<String> names = new ArrayList<>();
names.add("Ada");
names.add(42);          // compile error — the list is explicitly typed to hold only String
```
Generics let you write one reusable, type-safe implementation (`List<T>`) instead of either duplicating the same logic per concrete type, or giving up type safety entirely with something untyped like `List<Object>` that silently accepts anything and defers all type mismatches to runtime. This is the type-system feature that makes strongly-typed collections, and much of strongly-typed functional programming generally, genuinely ergonomic instead of a constant fight against the type checker.

## Duck typing and structural typing — a real third approach worth knowing by name

```python
# Python's duck typing: "if it walks like a duck and quacks like a duck..."
def make_it_speak(animal):
    animal.speak()   # works on ANY object with a .speak() method — no shared
                      # base class or interface required, no declared relationship at all
```
```typescript
// TypeScript's structural typing: similar spirit, but STATICALLY checked
interface Speaker { speak(): void; }
function makeItSpeak(s: Speaker) { s.speak(); }
// Any object with a matching speak() method satisfies Speaker,
// with ZERO explicit "implements Speaker" declaration required —
// the shape alone is what the type checker verifies
```
This differs meaningfully from **nominal typing** (Java, C#), where two types are only compatible if one explicitly, textually declares that it implements or extends the other — shape alone is never enough, no matter how identical two classes' actual methods look. Structural typing is a genuinely different, and for many real use cases more flexible, philosophy for what "compatible type" even means — worth recognizing as a real design choice, not a a lesser or incomplete version of nominal typing.

## Where this connects

This page is the deep dive into what actually happens during the [semantic analysis phase of the compiler pipeline](compiler-pipeline.md) — a type checker is, very concretely, one specific and consequential kind of semantic analysis pass, walking the exact same AST the parser just produced.
