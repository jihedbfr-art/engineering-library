# Go Concurrency — Deep Dive

[go.md](go.md) covers the language overview. Concurrency isn't a bolted-on library in Go the way it often is elsewhere — it's a genuine first-class language feature, and it's the single biggest reason Go became the default choice for so much of modern cloud infrastructure (Docker, Kubernetes, and a huge share of the tooling underneath both are written in Go, precisely because of what's on this page).

## Goroutines — real concurrency, deliberately cheap

```go
func fetchUser(id int, results chan<- User) {
    user := callAPI(id)
    results <- user
}

func main() {
    results := make(chan User, 10)
    for i := 1; i <= 10; i++ {
        go fetchUser(i, results)   // the `go` keyword — spawns a goroutine, that's the entire syntax
    }
    for i := 0; i < 10; i++ {
        user := <-results
        fmt.Println(user)
    }
}
```
A goroutine starts with only about **2KB of stack** (growing dynamically as needed), compared to a traditional OS/platform thread's roughly 1MB default footprint — genuinely, dramatically cheaper, which is exactly why spawning tens of thousands of goroutines in a single Go program is completely unremarkable and routine, where doing the equivalent with traditional OS threads would exhaust memory outright. This directly echoes [Java's virtual threads](java-concurrency.md) motivation — both are, at their core, answers to the exact same underlying problem: traditional OS threads are too expensive to spawn freely, so give the language/runtime its own dramatically cheaper unit of concurrency instead.

## Channels — Go's actual answer to "how do goroutines talk safely"

```go
ch := make(chan int)          // unbuffered — a send BLOCKS until a receiver is ready to receive
bufferedCh := make(chan int, 5)  // buffered — a send only blocks once the buffer is genuinely full

ch <- 42        // send
value := <-ch    // receive
close(ch)         // signal "no more values will ever be sent" — receivers can detect this explicitly
```
Go's famous, defining design philosophy: **"Don't communicate by sharing memory; share memory by communicating."** Instead of multiple goroutines directly mutating the same shared variable (needing careful, error-prone external locking to stay safe), goroutines pass actual data *through* channels — ownership of that data effectively transfers with the message itself, which sidesteps an entire, genuinely large category of classic concurrency bugs by construction, simply by never creating the shared-mutable-state situation those bugs require in the first place.

## `select` — the mechanism for waiting on multiple channels at once

```go
select {
case msg := <-channelA:
    fmt.Println("from A:", msg)
case msg := <-channelB:
    fmt.Println("from B:", msg)
case <-time.After(5 * time.Second):
    fmt.Println("timeout — neither channel produced anything in time")
}
```
`select` is genuinely the concurrency equivalent of a `switch` statement, except each case is a channel operation, and Go picks whichever one becomes ready first — this is the standard, idiomatic pattern for implementing timeouts, and for a goroutine that legitimately needs to respond to whichever of several different event sources happens to fire first.

## The `sync` package — for the cases where sharing memory really is the right call

Go's channel philosophy is a strong, genuine default, but the standard library's `sync` package exists precisely for the legitimate cases where directly sharing memory actually is the more natural, more appropriate fit:

```go
var mu sync.Mutex
var counter int

func increment() {
    mu.Lock()
    defer mu.Unlock()   // idiomatic Go — guarantees the unlock runs even if a panic occurs mid-function
    counter++
}

var wg sync.WaitGroup
for i := 0; i < 5; i++ {
    wg.Add(1)
    go func(n int) {
        defer wg.Done()
        process(n)
    }(i)
}
wg.Wait()   // blocks until all 5 goroutines have called Done()
```
`sync.WaitGroup` is the idiomatic way to wait for a known, fixed number of goroutines to finish — a genuinely common, recurring pattern worth knowing well. Note the closure gotcha in the loop above: passing `i` explicitly as the parameter `n` avoids a classic, extremely common Go bug where every goroutine would otherwise capture and share the *same* outer loop variable `i` by reference, all observing its final value after the loop completes, rather than each goroutine capturing the specific value it was actually launched with.

## Race conditions — Go gives you a real tool to actually catch them

```bash
go run -race main.go
go test -race ./...
```
Go's built-in **race detector** instruments the compiled binary to actively catch real, genuine data races at runtime — unsynchronized concurrent access to the same shared memory location from multiple goroutines, at least one of which is a write. This is a genuinely significant tool: running `-race` routinely in CI catches an entire, real class of concurrency bugs that might otherwise only manifest rarely, unpredictably, and painfully in production under just the right specific timing — exactly the kind of bug that's brutal to reproduce and debug after the fact, and dramatically easier to catch proactively before it ever ships.

## Goroutine leaks — the mistake that's easy to make and easy to miss

```go
// LEAKS — this goroutine blocks forever if nobody ever reads from ch
func leaky() {
    ch := make(chan int)
    go func() {
        result := computeSomething()
        ch <- result       // blocks forever here if the channel is unbuffered and nobody ever receives
    }()
    // ... function returns without ever reading from ch — the goroutine above is now stuck, permanently
}
```
Unlike a memory leak from an unreleased object reference, a **goroutine leak** is a permanently-blocked goroutine that the garbage collector genuinely cannot reclaim, because it's still technically "alive," just eternally stuck waiting on a channel operation that will now never happen. These accumulate silently, one at a time, in long-running services — exactly the kind of slow, creeping resource growth that shows up as [gradually increasing memory/goroutine-count in production monitoring](../../devsecops/monitoring/observability.md) days or weeks after a subtly-flawed deploy, not as an immediate, obvious crash. The standard defense: always give goroutines that might otherwise block forever a real, explicit way out — a `context.Context` for cancellation, a buffered channel sized correctly for the actual expected case, or a `select` with a timeout case.

## Context — Go's standard mechanism for cancellation and deadlines

```go
ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
defer cancel()

result, err := doSlowOperation(ctx)   // the operation is expected to respect ctx.Done() internally
```
`context.Context` is how Go idiomatically propagates cancellation signals and deadlines *down* through a call chain — genuinely essential in real server code, so that when an incoming HTTP request times out or the client disconnects, every goroutine spawned to serve that specific request can be told to stop promptly, cleanly, instead of continuing to do now-pointless work indefinitely, wasting real resources on an answer nobody is ever going to receive.

## Where this connects

Goroutines and virtual threads ([Java](java-concurrency.md)) solve the same underlying "OS threads are too expensive to spawn freely" problem via genuinely different mechanisms — channels vs shared-memory-with-locks. The race detector and goroutine-leak discipline mirror the exact same underlying concerns as [database transaction/locking correctness](../databases/transactions-concurrency.md) and [JavaScript's single-thread blocking trap](javascript-event-loop.md) — recognizing this recurring pattern across languages is worth more than memorizing any one language's specific syntax for handling it.
