# Modern Angular — The Current Way

Angular changed a lot. If your reflexes date from NgModules-everywhere, recalibrate here.

## Standalone components (the default now)

```typescript
@Component({
  selector: 'app-note-card',
  imports: [DatePipe, RouterLink],          // imports live on the component
  template: `
    <article>
      <h3><a [routerLink]="['/notes', note().id]">{{ note().title }}</a></h3>
      <time>{{ note().createdAt | date:'medium' }}</time>
    </article>
  `,
})
export class NoteCardComponent {
  note = input.required<Note>();            // signal-based input
  archived = output<string>();              // new output API
}
```
No NgModule ceremony. Routes lazy-load components directly.

## Signals — the new reactivity core

```typescript
@Injectable({ providedIn: 'root' })
export class NotesStore {
  private readonly api = inject(NotesApi);

  readonly notes = signal<Note[]>([]);
  readonly filter = signal('');
  readonly visible = computed(() =>
    this.notes().filter(n => n.title.includes(this.filter())));

  async load() {
    this.notes.set(await firstValueFrom(this.api.list()));
  }
}
```
- `signal` = writable state, `computed` = derived, `effect` = side effects (use sparingly).
- Fine-grained updates; pairs with zoneless change detection.
- **Signals for state, RxJS for events over time** (websockets, debounced search, cancellation). They compose via `toSignal`/`toObservable`.

## Control flow in templates

```html
@if (store.visible().length === 0) {
  <p>No notes yet — create your first one.</p>
} @else {
  @for (note of store.visible(); track note.id) {
    <app-note-card [note]="note" (archived)="store.archive($event)" />
  }
}
```
`@if/@for/@switch` replace `*ngIf/*ngFor`. `track` is mandatory in `@for` — and that's good (list perf).

## inject() over constructor injection

```typescript
export class NotesApi {
  private http = inject(HttpClient);
  list() { return this.http.get<Note[]>('/api/notes'); }
}
```
Works in functions too — enables functional guards/interceptors:

```typescript
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.isAuthenticated() || inject(Router).createUrlTree(['/login']);
};

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).token();
  return next(token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req);
};
```

## Performance checklist

- [ ] `ChangeDetectionStrategy.OnPush` (or zoneless) everywhere
- [ ] Lazy-load routes; `@defer` blocks for heavy below-the-fold components
- [ ] `track` correct in every `@for`
- [ ] `NgOptimizedImage` for images
- [ ] Bundle check: `ng build --stats-json` + esbuild analyzer

## Security reflexes (front side)

- Angular escapes interpolations by default — the danger is `[innerHTML]`, `bypassSecurityTrust*`: audit every use.
- Tokens: keep in memory (a service), not `localStorage`, if you can (XSS steals storage).
- The front enforces UX, the API enforces security — hiding a button is not authorization ([why](../../cybersecurity/web-security.md)).
