# Revenue Assurance & Fraud Management

Two disciplines that make sure the operator actually **collects the money it earned** and **doesn't pay for money it never earned**. At telecom scale, small leak percentages are enormous absolute sums.

## Revenue Assurance (RA)

RA answers: *"Did every unit of service we delivered get correctly rated, charged, billed and collected?"*

### Where revenue leaks

| Leak point | Example |
|---|---|
| **Mediation** | CDRs dropped, duplicated, or mis-parsed → usage never billed |
| **Provisioning gaps** | Service active in the network but not in billing → free service |
| **Rating errors** | Wrong tariff, rounding, expired promo still applied |
| **Reference data** | Product catalog and network out of sync |
| **Interconnect** | Traffic exchanged with other operators mis-counted |
| **Collection** | Invoiced but never chased/paid |

### How RA works

Reconciliation across the chain — compare what *should* have happened against what *did*:

```
Network usage counters  ──┐
Mediation record counts ──┼──► reconcile ──► discrepancies ──► investigate ──► fix + recover
Rated/billed amounts    ──┤
Payments collected      ──┘
```
It's fundamentally a **data-engineering + analytics** problem: ingest from every system, match records, flag gaps, quantify leakage, drive corrections. Increasingly ML-assisted for anomaly detection.

## Fraud Management

Fraud answers: *"Is someone using our network without intending to pay, or exploiting it to steal?"*

### Common telecom fraud types

| Fraud | How it works |
|---|---|
| **SIM box / interconnect bypass** | Terminate international calls as local via banks of SIMs, dodging interconnect fees |
| **Subscription fraud** | Sign up with no intent to pay (fake/stolen identity) |
| **IRSF** (International Revenue Share Fraud) | Pump traffic to premium-rate numbers the fraudster profits from |
| **Wangiri** ("one ring") | Missed-call bait → victim calls back a premium number |
| **SIM swap** | Hijack a number to steal OTPs and take over accounts ([auth link](../../cybersecurity/web-security.md)) |
| **PBX/Wholesale hacking** | Compromise a business phone system to route fraudulent calls |

### How fraud systems detect it

- **Rules/thresholds**: velocity, unusual destinations, sudden spend spikes.
- **Pattern/behaviour analysis**: deviation from a subscriber's normal profile.
- **ML anomaly detection**: catch novel patterns rules miss.
- **Real-time action**: block, throttle, or flag for review before the loss grows — ties directly into the [OCS](ocs.md).

## The security crossover

Telecom fraud is where **network engineering meets cybersecurity**:
- **SIM Swap** and **Number Verification** APIs ([CAMARA/Open Gateway](../oss-bss.md)) let banks detect account-takeover — telecom data defending fintech.
- **SS7/Diameter attacks** ([protocols](../protocols.md)) enable interception and fraud; signaling firewalls are a defensive control.
- Detection pipelines share DNA with SIEM/blue-team work → [../../cybersecurity/blue-team.md](../../cybersecurity/blue-team.md).

## For engineers

RA and Fraud are **streaming analytics at scale** with a money-and-security objective:
- ingest everything, correlate across sources, detect anomalies fast, act in real time
- measurable KPIs: leakage %, fraud loss, detection latency, false-positive rate
- the same discipline as [observability](../../devsecops/monitoring/observability.md) and [LLM evals](../../ai/machine-learning/evals-and-testing.md) — you cannot fix what you cannot measure.
