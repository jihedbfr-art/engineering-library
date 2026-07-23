# iOS — Swift & SwiftUI

## The modern stack (2024+)

- **Language**: Swift (Objective-C is legacy-supported, not where new code should go)
- **UI**: SwiftUI — declarative, replaces UIKit for new screens (UIKit still very much alive for complex/legacy cases)
- **Architecture**: MVVM with `@Observable`/`ObservableObject`
- **Async**: Swift Concurrency (`async/await`, actors) — replaced completion-handler soup

## SwiftUI in one example

```swift
struct NoteCard: View {
    let note: Note
    let onArchive: (String) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(note.title).font(.headline)
            Text(note.createdAt.formatted()).font(.caption).foregroundStyle(.secondary)
            Button("Archive") { onArchive(note.id) }
        }
        .padding()
    }
}
```
Same declarative idea as Compose: the view is a function of state, re-rendered when that state changes.

## State management

```swift
@Observable
class NotesViewModel {
    var notes: [Note] = []
    var isLoading = false
    private let repo: NotesRepository

    init(repo: NotesRepository) { self.repo = repo }

    func load() async {
        isLoading = true
        notes = await repo.fetchNotes()
        isLoading = false
    }
}

struct NotesScreen: View {
    @State private var viewModel: NotesViewModel

    var body: some View {
        List(viewModel.notes) { note in NoteCard(note: note) { id in /* archive */ } }
            .task { await viewModel.load() }        // runs when the view appears
    }
}
```

## Swift Concurrency — the modern async model

```swift
func fetchNotes() async throws -> [Note] {
    let (data, _) = try await URLSession.shared.data(from: notesURL)
    return try JSONDecoder().decode([Note].self, from: data)
}
```
- `async/await` replaces nested completion handlers — same readability win Kotlin coroutines brought to Android.
- **Actors** protect mutable state from data races across concurrent tasks — the compiler enforces it (`Sendable` checking), which catches a whole class of bugs at compile time instead of in a crash report.

## The app lifecycle

```
Not running → Foreground active ⇄ Background (suspended quickly by default)
```
iOS is stricter than Android about background execution — apps get very limited background time unless they declare specific background modes (audio, location, VoIP, background fetch). Design around "we might get frozen any second," not "we can keep working after backgrounding."

## Local storage

- **SwiftData** (modern) or **Core Data** (mature, more control) for structured data.
- `UserDefaults` for small settings/flags only — never for anything sensitive.
- **Keychain** for tokens/credentials — encrypted, survives app deletion in some configurations (be deliberate about that).

## Permissions — request contextually, explain in Info.plist

```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>We use your location to show notes created nearby.</string>
```
Apple **requires** a human-readable justification string for every sensitive permission — missing or vague ones are a common App Store rejection reason.

## Testing

- **Unit**: XCTest for ViewModels/business logic.
- **UI**: XCUITest, or SwiftUI's `Preview` + snapshot testing for visual regressions.
- Test on the **oldest iOS version you still support** — SwiftUI behavior has shifted meaningfully across versions.

## Publishing checklist

- [ ] App Store Connect metadata + screenshots for every required device size
- [ ] Privacy manifest (`PrivacyInfo.xcprivacy`) — Apple now requires declaring *why* you use certain APIs (e.g. disk space checks), not just what data you collect
- [ ] TestFlight beta before public release — catches device-specific crashes early
- [ ] Review Apple's Human Interface Guidelines before submission — deviating too far is a common soft-rejection reason
