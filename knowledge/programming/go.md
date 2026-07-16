# Go (Golang)

Simple by design, compiles to a single static binary, first-class concurrency. The language of cloud infrastructure (Docker, Kubernetes, Terraform are all Go).

## The philosophy

Small language, few keywords, one obvious way to do things. Readability and simplicity over cleverness. You can learn the whole language in a weekend — that's the point.

## Essentials

```go
package main

import "fmt"

func main() {
    // Explicit types, short declaration
    name := "Ada"          // inferred
    var count int = 0

    // Slices (dynamic arrays) and maps
    nums := []int{1, 2, 3}
    nums = append(nums, 4)
    ages := map[string]int{"ada": 36}

    for i, n := range nums {
        fmt.Println(i, n)
    }
}

// Multiple return values — the idiom for errors
func divide(a, b float64) (float64, error) {
    if b == 0 {
        return 0, fmt.Errorf("divide by zero")
    }
    return a / b, nil
}
```

## Error handling — explicit, everywhere

```go
result, err := divide(10, 0)
if err != nil {
    return err        // handle or bubble up — no exceptions
}
use(result)
```
No try/catch. Errors are values you check. Verbose, but the control flow is always visible.

## Concurrency — goroutines & channels

```go
// Goroutine: a function running concurrently (cheap — thousands are fine)
go doWork()

// Channels: typed pipes for communicating between goroutines
ch := make(chan int)
go func() { ch <- 42 }()   // send
value := <-ch              // receive (blocks until ready)

// Wait for many
var wg sync.WaitGroup
for _, job := range jobs {
    wg.Add(1)
    go func(j Job) { defer wg.Done(); process(j) }(job)
}
wg.Wait()
```
> "Don't communicate by sharing memory; share memory by communicating." Channels over locks where you can.

## Structs & interfaces (implicit)

```go
type Animal interface { Sound() string }

type Dog struct{ Name string }
func (d Dog) Sound() string { return "Woof" }   // Dog satisfies Animal automatically
```
No `implements` keyword — if it has the methods, it fits the interface. Encourages small interfaces.

## Tooling (all built-in)

```bash
go run .        go build       go test ./...
go fmt ./...    go vet ./...   go mod tidy
```
One formatter, no debates. Static binary output → trivial Docker images (`FROM scratch`).

## Where Go fits

✅ APIs/microservices, CLIs, cloud/devops tooling, networking, high-concurrency servers.
⚠️ Heavy generics-based abstraction, data science, GUI apps. (Generics exist since 1.18 but the culture stays simple.)
