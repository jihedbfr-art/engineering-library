# React Native — JS/React for Native Apps

For teams that already know React: same component model, same hooks, real native UI components underneath instead of a custom-rendered canvas (that's the key difference from Flutter).

## The architecture (New Architecture / Fabric)

```
Your JS (React components) ──► JSI (JS Interface) ──► Native modules/components
                                                              │
                                                  actual native UIView / android.view.View
```
Modern RN (Fabric + TurboModules) talks to native code more directly than the old bridge — less serialization overhead, closer to real-time native calls. Under the hood, your `<View>` becomes a real `UIView` (iOS) or `android.view.View` (Android), not a canvas-drawn shape like Flutter.

## Components in one example

```jsx
function NoteCard({ note, onArchive }) {
  return (
    <View style={styles.card}>
      <Text style={styles.title}>{note.title}</Text>
      <Text style={styles.date}>{formatDate(note.createdAt)}</Text>
      <Pressable onPress={() => onArchive(note.id)} style={styles.button}>
        <Text>Archive</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { padding: 16, borderRadius: 8, backgroundColor: '#fff' },
  title: { fontSize: 16, fontWeight: '600' },
  date: { fontSize: 12, color: '#666' },
  button: { marginTop: 8, padding: 8 },
});
```
If you know React, this needs zero new mental model — `View`/`Text`/`Pressable` instead of `div`/`span`/`button`, styles as JS objects instead of CSS.

## State & data fetching

Same React ecosystem you already use:
```jsx
function useNotes() {
  const [notes, setNotes] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchNotes().then(setNotes).finally(() => setLoading(false));
  }, []);

  return { notes, loading };
}
```
**React Query / TanStack Query** for server state (caching, retries, refetch) is just as valuable here as on web — arguably more, given flaky mobile networks.

## Navigation — the thing that's genuinely different from web

No URLs/history API by default. **React Navigation** is the standard:
```jsx
<Stack.Navigator>
  <Stack.Screen name="Notes" component={NotesScreen} />
  <Stack.Screen name="NoteDetail" component={NoteDetailScreen} />
</Stack.Navigator>
```
Stack, tab, and drawer navigators compose to match the native navigation patterns users expect on each platform.

## Native modules — dropping to platform code

```js
import { NativeModules } from 'react-native';
const { BatteryModule } = NativeModules;
const level = await BatteryModule.getBatteryLevel();
```
Same escape hatch as Flutter's platform channels. Expo (see below) covers most common native needs without you writing any native code yourself.

## Expo vs bare React Native

| | **Expo** | **Bare RN** |
|---|---|---|
| Setup | Fast, managed, huge library of pre-built native modules | You configure native projects yourself |
| Native code | Avoided unless you eject/use dev builds | Full control from day one |
| Good for | Most apps, especially starting out | Apps needing deep custom native integration |

Modern Expo (with **dev builds**) closed most of the old "Expo can't do custom native code" gap — start there unless you have a concrete reason not to.

## Storage

- **AsyncStorage** for simple key-value (unencrypted — not for secrets).
- **react-native-keychain** or **expo-secure-store** for tokens (Keychain/Keystore-backed).
- **WatermelonDB** or **op-sqlite** for real local databases with sync needs.

## Testing

- **Jest** + **React Native Testing Library** for components/logic — same tools as web React.
- **Detox** or **Maestro** for end-to-end tests on real devices/simulators.

## When React Native is the right call

- ✅ Existing React/web team, want to reuse skills and even some logic.
- ✅ App is mostly forms, lists, standard UI — not animation-heavy custom graphics.
- ⚠️ Very animation-heavy or graphics-intensive UI may favor Flutter's rendering model or native.
