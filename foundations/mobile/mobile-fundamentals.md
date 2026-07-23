# Mobile Fundamentals — What Every Stack Shares

Concepts that transfer across Android, iOS, Flutter and React Native. Learn these once.

## The lifecycle problem

Unlike a web page (open → close), a mobile app is **constantly interrupted**: backgrounded, killed for memory, resumed, rotated. Every platform models this as a lifecycle you must respect.

```
Not running → Launching → Foreground (active) ⇄ Background → Suspended/Killed
                              │
                          your screen is
                          visible & interactive
```
The #1 beginner mobile bug: assuming your screen's state survives a backgrounding. It often doesn't — the OS can kill the process to reclaim memory and just "restore" you later. **Persist what matters, don't trust in-memory state to survive.**

## Offline-first — design for no connection, not just slow connection

Mobile networks drop constantly (elevators, tunnels, planes, bad coverage). The apps that feel solid treat network as optional:

```
User action → write to LOCAL store immediately → show it as done
                     │
              sync queue in background → server (retry with backoff)
                     │
              conflict? → resolve (last-write-wins, or merge, or ask user)
```
This is the same idea as [event-driven architecture](../devsecops/ci-cd/gitops-argocd.md)'s eventual consistency, just on-device. Local-first databases (SQLite, Realm, WatermelonDB) plus a sync layer are the standard pattern.

## Push notifications — how "the app talks to you while closed"

1. Device registers with the platform's push service (FCM for Android, APNs for iOS) → gets a token.
2. Your backend sends that token + payload to FCM/APNs.
3. The OS wakes the app (or shows a notification) — your code didn't do this directly; the OS did, on the platform's terms.

Design implication: **never rely on push for critical delivery** — pushes can be delayed, batched, or dropped by the OS to save battery. Use them as a hint to sync, not as the sync itself.

## App store review — the gate you don't control

Both Apple and Google review every submission. Common rejection reasons:
- Crashes on launch (test on a low-end device, not just your dev phone)
- Broken core functionality in the reviewer's flow
- Missing privacy disclosures (what data you collect, why)
- Misleading permissions requests (asking for location before explaining why)

Practical habit: request permissions **contextually**, right when the feature needs them, with a one-line explanation — not all upfront at launch. Reviewers and users both react badly to upfront permission dumps.

## Performance constraints unique to mobile

- **Battery**: background work, GPS, network polling all drain it — the OS actively throttles apps that abuse this.
- **Memory**: far tighter than desktop; the OS kills backgrounded apps under pressure.
- **Startup time**: users judge an app in the first 2 seconds — lazy-load everything not needed for the first screen.
- **Variable hardware**: your test device is not your median user's device. Budget phones exist and are common.

## Security specifics

- **Never trust the client** — decompiling an APK/IPA is trivial; don't embed API secrets in the app ([why](../devsecops/security/secrets-management.md)).
- Use the platform's secure storage for tokens (Keystore/Keychain), not plain SharedPreferences/UserDefaults.
- Certificate pinning for high-value apps (banking) — resists MITM even on a compromised network.
- Deep links/intents are an attack surface — validate everything that comes in through them.

## Architecture pattern that transfers everywhere

```
View (dumb, renders state)
   ⇅
ViewModel/Presenter (holds UI state, calls use cases)
   ⇅
Domain/Use cases (business logic, platform-agnostic)
   ⇅
Data layer (local DB + remote API, behind a repository interface)
```
Same idea as MVVM, MVI, or Redux-style unidirectional flow — the names differ per platform, the shape doesn't. Keep business logic out of the View; it's the only part you can't unit-test easily.
