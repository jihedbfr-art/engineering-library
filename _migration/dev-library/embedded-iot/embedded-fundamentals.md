# Embedded Fundamentals

## Microcontroller vs the computer you're used to

```
Your laptop:          GBs of RAM, GHz multi-core CPU, full OS, virtual memory,
                       storage measured in hundreds of GBs

Typical microcontroller: KBs to low MBs of RAM, tens to a few hundred MHz,
                       often no OS at all (or a tiny RTOS — see rtos-and-scheduling.md),
                       flash storage measured in KBs to a few MBs
```
This isn't a "smaller version of the same thing" — it's a different set of constraints that changes how you write code at a fundamental level. A memory leak that's a minor annoyance on a server with 32GB of RAM is a device that crashes in production, in the field, inside a customer's wall or car, on a microcontroller with 64KB.

## No dynamic memory allocation (often, deliberately)

```c
// Common in real embedded code — genuinely avoided, not just a style preference:
malloc(size);   // avoided or banned outright in many embedded codebases

// Instead: static allocation, fixed-size buffers, and object pools
static uint8_t buffer[256];
static SensorReading readings[MAX_READINGS];  // fixed capacity, known at compile time
```
Why this isn't just paranoia: `malloc`/`free` on a memory-constrained device can fragment the tiny available heap until an allocation fails — often at the worst possible moment, in the field, with no operator around to intervene. Many embedded coding standards (including safety-critical ones like MISRA C, used in automotive/aerospace) restrict or outright ban dynamic allocation after initialization specifically to make memory behavior fully deterministic and analyzable ahead of time, not just "faster."

## Interrupts — the mechanism, and the discipline they demand

```c
// An Interrupt Service Routine (ISR) — runs when a hardware event fires,
// INTERRUPTING whatever the main program was doing, immediately
void TIMER0_IRQHandler(void) {
    sensor_flag = 1;      // set a flag — do the absolute minimum here
    TIMER0->SR = 0;        // clear the interrupt flag (hardware-specific)
}

// The main loop checks the flag and does the REAL work outside the ISR
while (1) {
    if (sensor_flag) {
        sensor_flag = 0;
        process_sensor_reading();   // the actual work happens HERE, not in the ISR
    }
}
```
**The iron rule: keep ISRs as short as humanly possible.** An interrupt handler that takes too long blocks other interrupts (or, depending on priority configuration, gets interrupted itself in ways that are genuinely hard to reason about) — a classic, hard-to-debug embedded bug is an ISR quietly doing too much work and causing a system that's technically "running" but missing real-time deadlines it can't visibly report as missed.

## Registers and memory-mapped I/O — how software actually touches hardware

```c
// Reading/writing hardware registers directly — no OS abstraction layer between you and the silicon
#define GPIO_BASE  0x40020000
#define GPIO_ODR   (*(volatile uint32_t*)(GPIO_BASE + 0x14))

GPIO_ODR |= (1 << 5);    // set bit 5 — physically turns on an LED wired to that pin
```
The `volatile` keyword here isn't decoration — without it, a compiler optimizing normally might assume this memory address never changes outside the program's own writes and cache/reorder the access incorrectly, silently breaking hardware interaction in a way that's invisible in the C code and only shows up as "the hardware doesn't do what the code says it should." This is one of the sharpest, most specific gotchas that separates embedded C from application-level C.

## Power consumption — a first-class design constraint, not an afterthought

```
Active mode:    full power, CPU running
Sleep mode:     CPU halted, peripherals/RAM retained, wakes on interrupt
Deep sleep:     most peripherals off too, much lower power, slower wake-up
```
For a battery-powered device expected to run for years on a coin cell (a huge share of real IoT sensors), power management isn't an optimization pass at the end — it's a core architectural decision from day one: how often does the device actually need to wake up, transmit, and go back to sleep? A firmware design that wakes every second instead of every minute can be the entire difference between a 2-year battery life and a 2-month one, for otherwise identical functionality.

## Real-time constraints — hard vs soft, and why the distinction matters

```
Hard real-time:  missing a deadline is a SYSTEM FAILURE, full stop.
                 (airbag deployment timing, anti-lock braking control loop)

Soft real-time:  missing a deadline degrades quality but isn't catastrophic.
                 (a video frame arriving slightly late — visibly worse, not dangerous)
```
This distinction is what actually determines architecture choices (bare-metal vs [RTOS](rtos-and-scheduling.md), how aggressively interrupts are prioritized, how much margin is engineered into every timing budget) — knowing which category a given system falls into should come before any other embedded design decision, not after.

## Debugging without a console — the reality of the discipline

No `console.log`. No attached monitor, most of the time. Real embedded debugging tools:
- **JTAG/SWD debuggers** — a physical hardware connection letting you single-step code and inspect memory/registers directly on the actual chip, the closest embedded equivalent to a debugger breakpoint.
- **Logic analyzers/oscilloscopes** — literally watching electrical signals on specific pins to verify timing and protocol behavior at the hardware level, not the software level.
- **UART/serial debug output** — the humble, still-common fallback: a wired serial connection printing debug text to a terminal on another machine, embedded's closest analog to `console.log`.

## Where this connects

[RTOS and scheduling](rtos-and-scheduling.md) is what happens when a project's timing/complexity outgrows a simple bare-metal main loop and needs real task scheduling. [IoT protocols](iot-protocols.md) is how these constrained devices, once running, actually communicate — and connects directly to [NB-IoT/LTE-M](../telecom/iot-m2m.md) for the cellular connectivity layer underneath.
