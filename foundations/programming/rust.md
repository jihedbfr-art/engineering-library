# Rust

Memory safety **without** a garbage collector, and fearless concurrency — enforced at compile time. Steep learning curve, then you trust your code like no other language.

## The big idea: ownership

Rust's compiler tracks who owns each piece of memory, so it can free it automatically and prevent whole classes of bugs (use-after-free, data races) — with zero runtime cost.

```rust
fn main() {
    let s = String::from("hello");   // s owns the string
    let s2 = s;                      // ownership MOVES to s2
    // println!("{}", s);            // ❌ compile error: s no longer valid

    let s3 = s2.clone();             // explicit copy if you want both
    takes_ref(&s3);                  // borrow — lend without giving ownership
    println!("{}", s3);              // ✅ still valid
}

fn takes_ref(s: &String) { println!("{}", s); }
```

## Borrowing rules (the borrow checker)

At any time you can have **either**:
- one mutable reference (`&mut`), **or**
- any number of immutable references (`&`)

...but not both. This single rule eliminates data races at compile time.

## No null, no exceptions — Option & Result

```rust
// Option: maybe a value
fn find(id: u32) -> Option<User> { ... }
match find(5) {
    Some(user) => println!("{}", user.name),
    None => println!("not found"),
}

// Result: success or error
fn parse(s: &str) -> Result<i32, ParseError> { ... }
let n = parse("42")?;   // ? propagates the error up, like a clean early-return
```
The compiler forces you to handle the missing/error case. No surprise nulls.

## Structs, enums, traits

```rust
struct User { id: u32, name: String }

enum Status { Active, Trial { days_left: u32 }, Cancelled }

trait Summary { fn summarize(&self) -> String; }
impl Summary for User {
    fn summarize(&self) -> String { format!("User {}", self.name) }
}
```
Enums are powerful (they carry data); pattern matching on them is exhaustive — miss a case and it won't compile.

## Tooling (excellent)

```bash
cargo new myapp        cargo build --release
cargo run              cargo test
cargo clippy           # superb linter
cargo fmt
```
`cargo` is the gold standard: build, test, deps, docs in one tool.

## Where Rust wins / costs

✅ Systems programming, performance-critical services, CLIs, WebAssembly, embedded, anything where a crash or memory bug is unacceptable.
⚠️ The learning curve is real — the borrow checker fights you for weeks. Slower to prototype than Python/Go. Worth it when correctness and speed both matter.
