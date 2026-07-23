# Android — Kotlin & Jetpack Compose

## The modern stack (2024+)

- **Language**: Kotlin (Java is legacy-supported, not where new code should go)
- **UI**: Jetpack Compose — declarative, replaces the old XML+View system
- **Architecture**: MVVM with Compose, backed by `ViewModel` + `StateFlow`
- **DI**: Hilt (built on Dagger)
- **Async**: Kotlin Coroutines + Flow

## Compose in one example

```kotlin
@Composable
fun NoteCard(note: Note, onArchive: (String) -> Unit) {
    Card(modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(note.title, style = MaterialTheme.typography.titleMedium)
            Text(note.createdAt.toFormattedString(), style = MaterialTheme.typography.bodySmall)
            Button(onClick = { onArchive(note.id) }) { Text("Archive") }
        }
    }
}
```
No XML, no `findViewById`. The UI is a pure function of state — re-run automatically ("recomposition") when that state changes.

## State management

```kotlin
class NotesViewModel(private val repo: NotesRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(loading = true) }
        val notes = repo.fetchNotes()               // suspend fun, safe to call
        _uiState.update { it.copy(notes = notes, loading = false) }
    }
}
```
`viewModelScope` auto-cancels in-flight coroutines when the ViewModel is cleared — you don't leak work when the user navigates away mid-request.

## The Activity/Fragment lifecycle (still underneath Compose)

```
onCreate → onStart → onResume → [foreground] → onPause → onStop → onDestroy
                                                    ↑___________________|
                                                   (can restart from onStart)
```
`ViewModel` survives configuration changes (screen rotation) — that's precisely why business/UI state belongs there, not in the Composable itself.

## Coroutines — Android's answer to async

```kotlin
suspend fun fetchNotes(): List<Note> = withContext(Dispatchers.IO) {
    api.getNotes()          // runs off the main thread, doesn't block UI
}
```
- `Dispatchers.Main` for UI updates, `Dispatchers.IO` for network/disk, `Dispatchers.Default` for CPU-heavy work.
- Structured concurrency: a coroutine's children are cancelled when their scope is — no orphaned background work by default (unlike raw threads/callbacks).

## Local storage

- **Room** (SQLite wrapper) for structured data — type-safe queries, works great with Flow for reactive UI updates.
- **DataStore** replaces `SharedPreferences` for key-value/settings (async, type-safe).

## Permissions — request contextually

```kotlin
val locationPermission = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted -> if (granted) startLocationUpdates() }

// Only call this when the user actually triggers a location-needing action
Button(onClick = { locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
    Text("Find nearby notes")
}
```

## Testing

- **Unit**: JUnit + Kotlin test for ViewModels/use cases (no Android framework needed if logic is properly separated).
- **UI**: Compose testing APIs (`composeTestRule`) — assert on semantics, not pixels.
- **Instrumented**: run on a real device/emulator for anything touching platform APIs.

## Publishing checklist

- [ ] Signed release build (App Bundle `.aab`, not raw APK, for Play Store)
- [ ] ProGuard/R8 minification — also strips debug info attackers could use
- [ ] Target the latest `targetSdkVersion` Google requires (they enforce a rolling minimum)
- [ ] Privacy policy + data safety form filled honestly — mismatches get apps pulled
