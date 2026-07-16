# Coding Against Network APIs (CAMARA / Open Gateway)

The developer's entry point into telecom. You don't need to know SS7 or run a core network — you call **REST APIs** that expose network capabilities, standardized so the same code works across operators.

## What & who

- **CAMARA**: open-source project (Linux Foundation) defining standardized network-capability APIs.
- **GSMA Open Gateway**: the operator-side initiative that publishes those APIs commercially across networks.
- Goal: **write once, run across operators** — no per-carrier custom integration.

## The APIs you'll actually use

| API | What it answers | Killer use case |
|---|---|---|
| **SIM Swap** | "Was this number's SIM changed recently?" | Bank blocks a transfer if the SIM swapped hours ago (anti-account-takeover) |
| **Number Verification** | "Does this user control this number?" | Silent login — no SMS OTP needed |
| **Device Location / Location Verification** | "Is the device near location X?" | Fraud checks, compliance, "verify you're in-country" |
| **Quality on Demand (QoD)** | "Give this session low latency / high bandwidth" | Cloud gaming, live video, telesurgery boost |
| **Device Status** | "Is the device reachable / roaming?" | IoT fleet monitoring, delivery logic |
| **Carrier Billing** | "Charge this to the phone bill" | Frictionless micro-purchases |

## How the auth works (OIDC — familiar territory)

Network APIs use **OAuth2 / OpenID Connect**, often with **CIBA** (Client-Initiated Backchannel Authentication) so the user's device is authenticated by the network without a redirect. If you've done [OIDC](../cybersecurity/web-security.md), you're home.

```
Your backend ──(OAuth2 client creds / CIBA)──► Operator token endpoint ──► access token
Your backend ──(Bearer token)──► CAMARA API endpoint ──► result
```

## Example — SIM Swap check (anti-fraud)

```http
POST /sim-swap/v0/check
Host: opengateway.operator.com
Authorization: Bearer {access_token}
Content-Type: application/json

{ "phoneNumber": "+21612345678", "maxAge": 240 }
```

```json
// Response
{ "swapped": true }   // SIM changed within the last 240 hours → high fraud risk
```

Backend logic:
```python
def is_transfer_risky(phone: str) -> bool:
    r = camara.post("/sim-swap/v0/check",
                    json={"phoneNumber": phone, "maxAge": 240})
    return r.json()["swapped"]     # if True → step up auth or block
```

## Example — Number Verification (silent auth)

```http
POST /number-verification/v0/verify
Authorization: Bearer {access_token}

{ "phoneNumber": "+21612345678" }
```
```json
{ "devicePhoneNumberVerified": true }
```
The network confirms the device on the mobile data connection owns that number — **no SMS, no OTP, no user friction**. Phishing-resistant because it's tied to the SIM, not a code a user can be tricked into revealing.

## Example — Quality on Demand

```http
POST /qod/v0/sessions
Authorization: Bearer {access_token}

{
  "duration": 3600,
  "device": { "phoneNumber": "+21612345678" },
  "applicationServer": { "ipv4Address": "203.0.113.10" },
  "qosProfile": "QOS_L"          // low latency
}
```
Your app just asked the network for a latency boost for one session. That's programmable connectivity.

## Why this is a big deal

- **Fraud/identity**: SIM-Swap + Number-Verification give fintechs a network-grade signal that apps alone can't fake — telecom defending banking ([auth security](../cybersecurity/web-security.md)).
- **New revenue**: operators monetize the network as a platform, not just a data pipe.
- **For you**: telecom features become ordinary API integrations — rate limits, OAuth2, JSON, webhooks. Apply your normal [REST](../backend/apis/rest-api-design.md) and [secrets](../devsecops/security/secrets-management.md) discipline.

## Getting started (practically)

1. Find an operator/aggregator sandbox (many publish Open Gateway sandboxes).
2. Register an app → get OAuth2 client credentials.
3. Call the sandbox endpoints with test numbers.
4. Treat credentials like any secret; never in client-side code.
5. Design for **graceful degradation** — if the API is down or the operator unsupported, fall back (e.g. to SMS OTP).
