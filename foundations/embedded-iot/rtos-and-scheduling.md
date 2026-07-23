# RTOS & Task Scheduling

Once a system needs to juggle multiple time-sensitive activities at once (read a sensor every 10ms, update a display every 100ms, handle a network stack, respond to a button press instantly), a hand-rolled `while(1)` main loop stops scaling cleanly. A **Real-Time Operating System** (FreeRTOS, Zephyr, VxWorks, and others) provides task scheduling, timing guarantees, and inter-task communication — while staying dramatically smaller and more deterministic than a general-purpose OS like Linux.

## What "real-time" actually means here (and what it doesn't)

A common misconception: real-time means "fast." It doesn't. **Real-time means predictable and bounded** — a hard real-time system guarantees a task completes within a known deadline, even if that deadline is generous, whereas a general-purpose OS makes no such guarantee no matter how fast the hardware is. A Linux desktop can be extremely fast on average and still occasionally miss a timing deadline by 200ms because of scheduler decisions outside your control — for many embedded applications, that occasional 200ms miss is an actual system failure, not just annoying.

## Tasks, priorities, and preemption

```c
// FreeRTOS-style task definition — each task is essentially its own tiny "thread"
void SensorTask(void *params) {
    for (;;) {
        read_sensor();
        vTaskDelay(pdMS_TO_TICKS(10));   // yield for 10ms, let other tasks run
    }
}

xTaskCreate(SensorTask, "Sensor", STACK_SIZE, NULL, PRIORITY_HIGH, NULL);
xTaskCreate(DisplayTask, "Display", STACK_SIZE, NULL, PRIORITY_LOW, NULL);
```
The scheduler runs the **highest-priority ready task** at any given moment; a lower-priority task gets preempted (paused) the instant a higher-priority one becomes ready to run. This is deliberate and different from typical general-purpose OS scheduling, which usually optimizes for fairness across many processes — an RTOS optimizes for **meeting deadlines**, which sometimes means starving a low-priority task entirely if higher-priority work keeps arriving, and that's considered correct behavior, not a bug, as long as the priority assignment itself was the right call.

## Priority inversion — the classic RTOS bug, explained properly

```
Low-priority task L holds a mutex protecting a shared resource.
High-priority task H needs that same mutex — blocks, waiting for L to release it.
Medium-priority task M, unrelated to the mutex, preempts L (fair game — M
   outranks L) and runs for a while.

Result: H, the HIGHEST priority task in the system, is effectively blocked
by M, a MEDIUM priority task — because M is keeping L from running long
enough to finish and release the mutex H actually needs.
```
This is priority inversion, and it's a real, historically famous class of bug — it's what caused watchdog-triggered resets on the Mars Pathfinder mission in 1997, diagnosed and fixed remotely, tens of millions of kilometers away, via a software patch. The standard fix is **priority inheritance**: while L holds a mutex that H is waiting on, the scheduler temporarily boosts L's priority to H's level, so M can no longer preempt it — L finishes and releases the mutex as fast as if it always ran at H's priority, then drops back to its normal priority. Most production RTOSes implement priority-inheritance mutexes specifically because of this well-documented failure mode; if you're writing RTOS code and using mutexes across tasks of different priorities without understanding this, you're carrying real risk of the exact bug that took down a Mars mission's diagnostics.

## Inter-task communication — the primitives that make this safe

```c
// Queue — the standard way tasks pass data to each other safely
QueueHandle_t sensorQueue = xQueueCreate(10, sizeof(SensorReading));

// Producer task
SensorReading r = read_sensor();
xQueueSend(sensorQueue, &r, portMAX_DELAY);

// Consumer task
SensorReading r;
if (xQueueReceive(sensorQueue, &r, portMAX_DELAY)) {
    process(r);
}
```
```
Semaphores    — signal "an event happened" between tasks or from an ISR to a task
Mutexes       — protect a shared resource from being accessed by two tasks at once
                (with priority inheritance, as above, in a properly built RTOS)
Queues        — pass actual data between tasks safely, without shared-memory races
```
These exist specifically because naive shared-memory access between concurrently-running tasks is exactly as dangerous here as [race conditions in concurrent database access](../databases/transactions-concurrency.md) — same underlying category of bug (unsynchronized concurrent access to shared state), different domain, same fundamental fix (proper synchronization primitives, not hope).

## Stack size — a constraint that bites in a very specific, very embedded way

Each RTOS task gets its own, typically small, fixed-size stack, carved out of the device's genuinely limited total RAM. Get it wrong in either direction: too small and a deeply-nested function call or a large local array **silently corrupts adjacent memory** with no OS-level protection catching it the way a desktop OS's virtual memory system would — a notoriously hard bug to trace, since the corruption's symptom often shows up somewhere completely unrelated to its actual cause. Too large, multiplied across many tasks, and you simply run out of the device's total RAM before you run out of features to implement. Sizing task stacks correctly is unglamorous, easy to get wrong, and a genuinely common source of real embedded bugs.

## Bare-metal vs RTOS — when to actually reach for one

```
Bare-metal (no OS, just a main loop + interrupts):
  ✅ Simplest, smallest footprint, most deterministic, easiest to fully understand
  ✅ Right choice for genuinely simple, single-purpose devices
  ⚠️ Gets unwieldy fast once you need several independent, differently-timed activities

RTOS:
  ✅ Clean task separation, proper priority-based scheduling, real timing guarantees
  ✅ Right choice once bare-metal's single loop starts juggling too much
  ⚠️ Real added complexity (priority inversion, stack sizing, task interaction bugs)
      — a cost that has to be worth paying, not a default reach
```
The honest heuristic: start bare-metal for anything genuinely simple. Reach for an RTOS specifically when the main loop is starting to accumulate multiple independent, differently-timed responsibilities that are becoming hard to reason about together — not by default, and not because it "sounds more professional."

## Where this connects

This is the scheduling layer sitting on top of [embedded fundamentals](embedded-fundamentals.md)'s interrupts and memory constraints — an RTOS task is, underneath, still built from the same ISR and register-level primitives covered there. The priority-inversion discussion above is the embedded-systems sibling of the [database transaction/locking](../databases/transactions-concurrency.md) concurrency discipline elsewhere in this library — recognizing the pattern once means recognizing it everywhere it recurs.
