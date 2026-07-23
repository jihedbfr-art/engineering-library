# Flutter — One Codebase, Two (or More) Platforms

Dart + a full rendering engine that draws every pixel itself, instead of wrapping native UI components. That single decision explains most of Flutter's strengths and quirks.

## Why it renders its own pixels (and why that matters)

React Native talks to real native components under the hood; Flutter draws its own via the **Skia** (now increasingly **Impeller**) graphics engine. Consequences:
- ✅ **Pixel-perfect consistency** across Android/iOS — no per-platform rendering differences to fight.
- ✅ Very smooth custom animations — you're not fighting a native component's constraints.
- ⚠️ Doesn't automatically feel "native" — you often want to consciously adapt to platform conventions (Material vs Cupertino) rather than get them for free.

## Widgets — everything is a widget

```dart
class NoteCard extends StatelessWidget {
  final Note note;
  final void Function(String id) onArchive;
  const NoteCard({required this.note, required this.onArchive, super.key});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(note.title, style: Theme.of(context).textTheme.titleMedium),
            Text(note.createdAt.toString(), style: Theme.of(context).textTheme.bodySmall),
            ElevatedButton(onPressed: () => onArchive(note.id), child: const Text('Archive')),
          ],
        ),
      ),
    );
  }
}
```
Layout, styling, even padding — all widgets, composed as a tree. Once this clicks, Flutter's whole API surface makes sense.

## State management — the one real decision to make early

Flutter doesn't prescribe a single state approach; the ecosystem converged on a few solid options:

| Approach | Fits |
|---|---|
| `setState` / `StatefulWidget` | Small, local, screen-only state |
| **Provider** | Simple app-wide state, gentle learning curve |
| **Riverpod** | Provider's successor — compile-safe, testable, current recommendation for most new apps |
| **Bloc/Cubit** | Explicit event→state streams, favored in larger teams wanting strict structure |

Pick one, be consistent — mixing three state approaches in one app is a maintenance headache waiting to happen.

## Async

```dart
Future<List<Note>> fetchNotes() async {
  final response = await http.get(Uri.parse('$apiBase/notes'));
  return (jsonDecode(response.body) as List).map((n) => Note.fromJson(n)).toList();
}

// In a widget:
FutureBuilder<List<Note>>(
  future: fetchNotes(),
  builder: (context, snapshot) {
    if (!snapshot.hasData) return const CircularProgressIndicator();
    return ListView(children: snapshot.data!.map((n) => NoteCard(note: n, onArchive: archive)).toList());
  },
)
```
`Stream`/`StreamBuilder` for continuously-updating data (websockets, reactive DB queries via e.g. Drift).

## Local storage

- **sqflite** or **Drift** (type-safe SQL) for structured data.
- **shared_preferences** for small settings only.
- **flutter_secure_storage** for tokens — backed by Keychain/Keystore under the hood, same rule as native ([mobile-fundamentals](mobile-fundamentals.md)).

## Platform channels — when you need real native code

```dart
static const platform = MethodChannel('com.example/battery');
final level = await platform.invokeMethod<int>('getBatteryLevel');
```
Drop to native Kotlin/Swift for anything Flutter doesn't expose — camera pipelines, specific sensors, platform-only SDKs. Most apps rarely need this; the plugin ecosystem covers the common cases.

## Testing

- **Unit**: plain Dart tests for business logic.
- **Widget tests**: `testWidgets()` — render a widget in-memory, assert on it, no emulator needed. Fast.
- **Integration tests**: `integration_test` package, runs on a real device/emulator end-to-end.

## When Flutter is the right call

- ✅ Team wants one codebase for Android + iOS (+ web/desktop as a bonus, quality varies).
- ✅ Custom, brand-heavy UI where native look-and-feel isn't the priority.
- ⚠️ Reconsider if you need the absolute newest platform APIs on day one, or a UI that must feel 100% platform-native without extra work.
