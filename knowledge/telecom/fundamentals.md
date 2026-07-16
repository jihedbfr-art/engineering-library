# Telecom Fundamentals

## The big picture — a call/data session in one glance

```
 Your phone ──radio──► Base station ──transport──► Core network ──► Internet / other phone
   (UE)                  (RAN)          (backhaul)     (packet/circuit)
                                                          │
                                                    OSS/BSS + Billing
                                             (who you are, what you owe)
```

Three worlds, always:
1. **RAN** (Radio Access Network) — the antennas and radios that talk to your device.
2. **Core** — the brain: authenticates you, routes traffic, enforces policy.
3. **OSS/BSS** — the software running the business: provisioning, billing, care.

## The generations (what actually changed)

| Gen | Era | Headline | Key tech |
|---|---|---|---|
| **1G** | 1980s | Analog voice | AMPS, NMT |
| **2G** | 1990s | Digital voice + **SMS** | GSM, TDMA, CDMA; GPRS/EDGE add data |
| **3G** | 2000s | Mobile **internet** | UMTS/WCDMA, HSPA |
| **4G** | 2010s | All-**IP**, real broadband | LTE, VoLTE (voice over IP) |
| **5G** | 2020s | Low latency, massive IoT, slicing | NR, cloud-native core (SBA) |
| **6G** | ~2030 | Research: sensing, AI-native, THz | not standardized yet |

Key shift: from **circuit-switched** (a dedicated line per call, 1G–3G voice) to **packet-switched** (everything is IP packets, 4G onward). Voice became just another app (VoLTE).

## How a phone attaches to a network (the 4 steps every gen shares)

1. **Search & sync** — the device finds a cell and locks onto its signal.
2. **Authenticate** — the SIM proves identity using a secret key shared with the operator (never transmitted).
3. **Register** — the network records where you are (for incoming calls/data).
4. **Bearer setup** — a data path is established; now packets flow.

## The SIM — tiny but central

- Stores the **IMSI** (who you are) and a secret key **Ki**, plus operator profile.
- Does the cryptographic challenge-response that authenticates you — the key never leaves the card.
- **eSIM**: same logic, but a soldered chip reprogrammed over the air (see [iot-m2m](iot-m2m.md)).

## Key identifiers (you'll see these everywhere)

| ID | What it identifies |
|---|---|
| **IMSI** | The subscriber (SIM) — international, secret-ish |
| **MSISDN** | The phone number (what people dial) |
| **IMEI** | The physical device |
| **ICCID** | The SIM card itself |
| **MCC/MNC** | Country + operator codes (e.g. 208-01 = France, Orange) |

## Spectrum — the scarce resource

Radio spectrum is finite and licensed by governments (often auctioned for billions).
- **Low band** (< 1 GHz): travels far, penetrates walls, low capacity → rural coverage.
- **Mid band** (1–6 GHz): the sweet spot, most 4G/5G lives here.
- **High band / mmWave** (24 GHz+): huge capacity, tiny range → dense urban hotspots.

That physics tradeoff — *coverage vs capacity* — explains almost every operator deployment decision.
