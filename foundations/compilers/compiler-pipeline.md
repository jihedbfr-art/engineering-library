# The Compiler Pipeline

Every compiler — from a tiny toy language to GCC to the JIT inside your JavaScript engine — moves source code through roughly the same sequence of stages. Once you can name each stage, a huge amount of "why did my code do that" and "why is this error message so weird" stops being mysterious.

## The pipeline, end to end

```
Source code (text)
      ↓  Lexical Analysis (Lexer / Tokenizer)
Tokens
      ↓  Syntax Analysis (Parser)
Abstract Syntax Tree (AST)
      ↓  Semantic Analysis (type checking, scope resolution)
Annotated AST
      ↓  Intermediate Representation (IR) generation
IR (a lower-level, still portable representation)
      ↓  Optimization passes
Optimized IR
      ↓  Code Generation
Machine code / bytecode
```

## Lexing — turning text into tokens

```
Source:  x = 3 + 5;

Tokens:  [IDENT "x"] [ASSIGN "="] [NUMBER "3"] [PLUS "+"] [NUMBER "5"] [SEMICOLON ";"]
```
The lexer's whole job is recognizing the smallest meaningful chunks — identifiers, numbers, operators, keywords — and discarding what doesn't matter structurally (whitespace, comments, in most languages). This is genuinely the same underlying technique as [regex pattern matching](../resources/cheatsheets/regex.md): a lexer is, at its core, a set of regex-like rules applied in sequence, scanning left to right.

## Parsing — turning tokens into a tree

```
Tokens: [IDENT "x"] [ASSIGN "="] [NUMBER "3"] [PLUS "+"] [NUMBER "5"] [SEMICOLON ";"]

AST:
        Assignment
        /        \
      "x"        Add
                 /   \
                3     5
```
The parser applies the language's **grammar** — its formal rules for what a valid sentence in the language looks like — to organize a flat token stream into a tree that reflects the code's actual structure and precedence. This is exactly why `2 + 3 * 4` correctly produces `14` and not `20`: the grammar encodes operator precedence, so the parser builds `Add(2, Multiply(3, 4))`, not `Multiply(Add(2, 3), 4)` — the tree shape *is* the precedence rule, made concrete.

## "Syntax error" vs "semantic error" — a distinction worth actually knowing

```
Syntax error:    if (x = 5    →  the PARSER can't build a valid tree at all
                                  (missing closing paren) — this is a
                                  grammar violation, full stop

Semantic error:  x + "hello"   →  parses FINE, valid tree — but semantic
                                  analysis catches that you can't add a
                                  number and a string in this language
```
A syntax error means the parser literally cannot make sense of the token sequence according to the grammar. A semantic error means the code is grammatically valid — it parses into a legitimate tree — but violates some rule that requires actually understanding *meaning* (types, scope, declared-before-use) rather than just structure. Recognizing which category an error message falls into is often the fastest way to know where to actually look for the bug.

## Semantic analysis — where type checking and scope resolution happen

This is the phase that walks the AST and asks the deeper questions structure alone can't answer: is this variable actually declared before it's used? Do these two types being added together actually make sense together? Is this function being called with the right number and type of arguments? [Type systems](type-systems.md) live almost entirely in this phase — a type checker is, concretely, a specific kind of semantic analysis pass walking the same tree the parser just built.

## Intermediate Representation (IR) — the part that makes cross-platform compilers possible

```
Same IR, multiple front-ends:        Same IR, multiple back-ends:
  C source    ──┐                          ┌── x86 machine code
  C++ source  ──┼──► shared IR ──────────►──┼── ARM machine code
  Rust source ──┘                          └── WebAssembly
```
This is the actual architectural reason a compiler infrastructure like **LLVM** can support dozens of source languages *and* dozens of target architectures without an unmanageable combinatorial explosion of direct language-to-architecture compilers: every front-end language compiles down to one shared IR, and every back-end target compiles up from that same shared IR — N languages plus M targets needs N+M translators total, not N×M.

## Optimization passes — genuinely clever, mostly invisible transformations

```c
// Before optimization
int x = 5;
int y = x * 2;
return y;

// After constant folding + propagation — computed entirely at compile time
return 10;
```
```
Common optimization passes:
  Constant folding      — compute constant expressions at compile time, not runtime
  Dead code elimination — remove code whose result is provably never used
  Inlining               — replace a function call with the function's actual body,
                            avoiding call overhead, and often unlocking further
                            optimizations across what used to be a function boundary
  Loop unrolling         — reduce loop overhead by expanding iterations explicitly
```
These operate on the IR precisely because it's a much simpler, more uniform representation than either the original source syntax or the final target-specific machine code — genuinely easier to reason about and transform safely at that intermediate level.

## Code generation — the final, target-specific step

The optimized IR gets translated into actual machine instructions (x86, ARM, or a virtual machine's bytecode for interpreted/JIT languages) — this stage is where target-specific concerns finally enter: register allocation, instruction selection, calling conventions specific to that exact platform.

## Compiled vs interpreted vs JIT — where this pipeline actually runs

```
Compiled (C, Rust, Go):        entire pipeline runs BEFORE execution,
                                producing a standalone machine-code binary

Interpreted (classic Python,
Ruby):                          the AST (or a simple bytecode) is walked
                                and executed directly, step by step, at
                                runtime — no separate machine-code output

JIT — Just-In-Time (JVM,
V8/JavaScript, modern
Python/PyPy):                   starts interpreting immediately for fast
                                startup, but profiles the running code and
                                compiles HOT paths (frequently-executed code)
                                to real machine code on the fly, often
                                achieving near-compiled speed for the parts
                                of a program that actually matter for
                                performance, without paying the full
                                compiled language's toolchain overhead
```
JIT compilation is genuinely one of the cleverer engineering compromises in this space: get an interpreter's fast startup and flexibility, but earn a compiled language's runtime speed specifically on the code paths that are actually hot — deciding *which* paths are hot, at runtime, based on real observed execution, is the whole trick.

## Where this connects

[Type systems](type-systems.md) is the deep dive into exactly what happens during the semantic analysis phase above — arguably the single most consequential design decision in any programming language, and the direct cause of a huge share of the differences you experience day to day between languages.
