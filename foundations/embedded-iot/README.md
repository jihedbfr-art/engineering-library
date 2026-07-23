# 🔩 Embedded Systems & Firmware

Software that runs directly on hardware with no operating system underneath it (or a tiny one), fixed and often brutal memory/power/timing constraints, and no `console.log` to save you when something goes wrong. A different discipline from almost everything else in this library.

- [embedded-fundamentals.md](embedded-fundamentals.md) — microcontrollers, memory constraints, interrupts, the bare-metal mindset
- [rtos-and-scheduling.md](rtos-and-scheduling.md) — real-time operating systems, task scheduling, priority inversion
- [iot-protocols.md](iot-protocols.md) — MQTT, CoAP, and how constrained devices actually talk to the cloud

## The mental shift coming from application development

A web server that crashes restarts and you check the logs. A firmware bug in a pacemaker, a car's braking controller, or a factory's safety system doesn't get that luxury — there may be no logs, no restart, and a real physical consequence. This section is written with that seriousness in mind: embedded engineering trades away almost every convenience (garbage collection, dynamic memory, abundant RAM, a full OS) specifically because those conveniences cost determinism, and determinism is often the actual requirement.
