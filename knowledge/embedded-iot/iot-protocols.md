# IoT Protocols — How Constrained Devices Actually Talk

A [sensor with 64KB of RAM and years of battery life to protect](embedded-fundamentals.md) cannot afford the overhead of a typical web stack — a full HTTP/TLS/JSON request costs real, measurable bytes and CPU cycles that translate directly into battery drain. IoT protocols exist specifically to communicate reliably under those constraints.

## Why HTTP mostly doesn't fit here

```
Typical HTTP request: headers alone often exceed 200-500 bytes,
                       a full TCP handshake, TLS handshake, connection
                       overhead — all BEFORE a single byte of actual
                       sensor data is transmitted

A battery-powered sensor sending "temperature: 21.5" every 10 minutes,
for 2 years on a coin cell, cannot afford that overhead paid repeatedly.
```
This single constraint — overhead cost directly translating to battery life — is the entire reason IoT-specific protocols exist instead of everyone just using REST over HTTP everywhere.

## MQTT — publish/subscribe, the IoT default

```
Device ──publish──► "sensors/room1/temperature" = "21.5" ──► MQTT Broker
                                                                    │
                                                          ┌─────────┴─────────┐
                                                          ▼                   ▼
                                                    Subscriber A       Subscriber B
                                                    (dashboard)        (alerting system)
```
- **Publish/subscribe, not request/response** — a device publishes to a *topic*; it never needs to know who (or how many subscribers) is listening, which decouples devices from consumers cleanly, the same architectural benefit [Kafka](../../data-engineering/streaming-kafka.md) provides in a very different, much higher-throughput context.
- **Tiny, binary-efficient header** — MQTT's minimum overhead is around 2 bytes, compared to HTTP's hundreds — the entire design optimizes hard for exactly the constraint above.
- **QoS levels built into the protocol itself**:
  ```
  QoS 0: "fire and forget" — cheapest, no delivery guarantee at all
  QoS 1: "at least once" — guaranteed delivery, but possible duplicates
         (same idempotency discipline as Kafka consumers — see streaming-kafka.md)
  QoS 2: "exactly once" — guaranteed, no duplicates, highest overhead cost
  ```
- **Last Will and Testament**: a device can pre-register a message the broker automatically publishes *if that device disconnects unexpectedly* (e.g. "sensor-42 went offline") — genuinely useful for detecting silently-dead devices in the field without any polling.

## CoAP — REST's constrained-device cousin

```
GET /temperature  (CoAP, over UDP, binary-encoded, tiny header)
        vs
GET /temperature  (HTTP, over TCP, text headers, much larger overhead)
```
**CoAP (Constrained Application Protocol)** deliberately mirrors REST's verbs and resource model (GET/POST/PUT/DELETE) but runs over UDP instead of TCP with a compact binary header — familiar REST semantics, IoT-appropriate weight. It fits naturally where a request/response interaction model is genuinely the right fit (query a specific sensor's current reading on demand) rather than MQTT's continuous publish/subscribe stream.

## MQTT vs CoAP — picking the right one

| | **MQTT** | **CoAP** |
|---|---|---|
| Model | Publish/subscribe | Request/response (REST-like) |
| Transport | TCP (persistent connection) | UDP (connectionless) |
| Best fit | Continuous telemetry streams, many subscribers | On-demand queries, resource-constrained request/response |
| Needs a broker? | Yes (a central MQTT broker) | No — direct device-to-device or device-to-server |

Neither is universally "better" — the choice follows the actual interaction pattern: an always-reporting sensor fleet naturally fits MQTT's publish model; an occasionally-queried device (a smart lock checked on demand) fits CoAP's request/response model more directly.

## LwM2M — device management, not just data

Beyond moving sensor data, real IoT deployments need to **manage** thousands of deployed devices: push firmware updates, read device status, reconfigure remotely, all without physical access. **LwM2M (Lightweight M2M)**, typically running over CoAP, standardizes exactly this — a real, common need the data-transport protocols above don't address by themselves. A fleet of field-deployed sensors with no remote management story is a fleet nobody can update once it ships — LwM2M (or an equivalent proprietary device-management layer) is what turns "10,000 sensors installed in the field" from a one-time deployment into a maintainable, long-term system.

## Where the cellular layer fits underneath all of this

MQTT/CoAP/LwM2M are **application-layer** protocols — they need an actual network connection underneath, and for wide-area, non-Wi-Fi-covered deployments, that's exactly where [NB-IoT and LTE-M](../../telecom/iot-m2m.md) come in: the low-power cellular connectivity carrying these lightweight application protocols across a city or a country, all the way to a gateway or cloud backend. This is the concrete, protocol-level continuation of that telecom module's IoT coverage — the layer directly above the cellular radio link.

## Security — genuinely harder here than in typical web development

Constrained devices often can't afford full TLS's computational and bandwidth cost, so IoT-specific lightweight security schemes exist (DTLS for CoAP, MQTT over TLS where the device budget allows it) — but a large, real share of *deployed* IoT devices skip proper security entirely because of genuine resource constraints, not just developer negligence. This is precisely the gap [Mirai and similar IoT botnets](../../cybersecurity/blue-team.md) exploited at massive scale — weak or absent authentication multiplied across hundreds of thousands of always-on, rarely-patched devices. Designing IoT security properly means budgeting real resource cost for it from the start of a project, not retrofitting it once a device is already shipping — retrofitting security onto already-deployed, resource-constrained hardware in the field is often close to impossible.

## Where this connects

This page is the software/protocol layer directly above [embedded fundamentals](embedded-fundamentals.md)'s hardware constraints, and it plugs directly into [telecom's NB-IoT/LTE-M/eSIM coverage](../../telecom/iot-m2m.md) for the wide-area connectivity layer underneath. The publish/subscribe pattern connects conceptually to [Kafka's event streaming](../../data-engineering/streaming-kafka.md) — same decoupling idea, a very different scale and resource budget.
