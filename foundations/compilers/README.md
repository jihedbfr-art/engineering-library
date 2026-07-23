# 🧠 Compilers & Programming Language Theory

How the code you write becomes the machine instructions that actually run — and the underlying ideas that shape every programming language you'll ever use, whether you notice them or not.

- [compiler-pipeline.md](compiler-pipeline.md) — lexing, parsing, AST, optimization, code generation
- [type-systems.md](type-systems.md) — static vs dynamic typing, type inference, what type safety actually buys you

## Why this matters even if you'll never write a compiler

You'll almost certainly never build a production compiler. You will, constantly, use the mental models this section teaches: reading a confusing type error becomes tractable once you understand what a type checker is actually doing; understanding why one language catches a bug at compile time and another only at 3am in production traces directly back to type system design; even debugging a gnarly parser or config-file bug benefits from recognizing "oh, this is a grammar ambiguity" instead of just poking at it randomly. This section exists to make the rest of your programming life make more sense, not to prepare you to write LLVM.
