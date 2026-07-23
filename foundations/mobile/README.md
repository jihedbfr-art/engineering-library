# 📱 Mobile

Native and cross-platform mobile development — the parts that don't show up in a web dev's mental model until they get bitten by them (app store review, background execution limits, offline-first).

- [android.md](android.md) — Kotlin, Jetpack Compose, lifecycle, architecture
- [ios.md](ios.md) — Swift, SwiftUI, lifecycle, architecture
- [flutter.md](flutter.md) — Dart, widgets, one codebase for both platforms
- [react-native.md](react-native.md) — JS/React for native apps, the bridge, when it fits
- [mobile-fundamentals.md](mobile-fundamentals.md) — concepts every mobile stack shares: lifecycle, offline, push, store review

## Which one should you learn?

| You already know | Reach for |
|---|---|
| Nothing yet, want max native performance/polish on one platform | Kotlin (Android) or Swift (iOS) |
| React/JS, want one codebase, ok with near-native | React Native |
| Nothing, want one codebase, want pixel-perfect custom UI | Flutter |
| Both platforms + real native APIs needed everywhere | Native, twice (Kotlin + Swift) |

Cross-platform (Flutter/RN) wins on speed-to-two-platforms; native wins when you need the newest platform APIs day one, the tightest performance, or the most idiomatic feel. Most real products today ship cross-platform and drop to native modules only where it matters (camera pipelines, background audio, etc.).
