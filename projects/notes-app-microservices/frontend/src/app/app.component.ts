import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from './services/api.service';
import { KeycloakService } from './services/keycloak.service';
import { SidebarComponent } from './components/sidebar.component';
import { NoteListComponent } from './components/note-list.component';
import { EditorComponent } from './components/editor.component';
import { Attachment, Note, Notebook, NotePayload, Tag, ViewMode } from './models/models';
import { Subject, debounceTime } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, SidebarComponent, NoteListComponent, EditorComponent],
  template: `
    <div class="shell">
      <app-sidebar
        [fullName]="keycloak.fullName"
        [notebooks]="notebooks"
        [tags]="tags"
        [view]="view"
        (select)="setView($event)"
        (newNote)="newNote()"
        (createNotebook)="createNotebook($event)"
        (deleteNotebook)="deleteNotebook($event)"
        (deleteTag)="deleteTag($event)"
        (logout)="keycloak.logout()" />

      <app-note-list
        [notes]="notes"
        [selectedId]="selected?.id ?? null"
        [view]="view"
        [searchQuery]="searchQuery"
        (open)="selected = $event"
        (search)="onSearch($event)" />

      <app-editor
        [note]="selected"
        [notebooks]="notebooks"
        (save)="saveNote($event)"
        (trash)="trashNote($event)"
        (restore)="restoreNote($event)"
        (purge)="purgeNote($event)"
        (upload)="uploadFile($event)"
        (download)="downloadFile($event)"
        (deleteAttachment)="removeAttachment($event)" />
    </div>
  `,
  styles: [`
    .shell { display: flex; height: 100vh; }
  `]
})
export class AppComponent implements OnInit {

  private api = inject(ApiService);
  keycloak = inject(KeycloakService);

  notes: Note[] = [];
  notebooks: Notebook[] = [];
  tags: Tag[] = [];
  view: ViewMode = { kind: 'all' };
  selected: Note | null = null;
  searchQuery = '';

  private search$ = new Subject<string>();

  ngOnInit(): void {
    this.search$.pipe(debounceTime(300)).subscribe(q => {
      this.view = q ? { kind: 'search', query: q } : { kind: 'all' };
      this.refreshNotes();
    });
    this.refreshAll();
  }

  refreshAll(): void {
    this.refreshNotes();
    this.api.listNotebooks().subscribe(nbs => this.notebooks = nbs);
    this.api.listTags().subscribe(tags => this.tags = tags);
  }

  refreshNotes(keepSelection = false): void {
    const done = (notes: Note[]) => {
      this.notes = notes;
      if (keepSelection && this.selected) {
        this.selected = notes.find(n => n.id === this.selected!.id) ?? null;
      } else if (!keepSelection) {
        this.selected = null;
      }
    };
    switch (this.view.kind) {
      case 'trash':
        this.api.listTrash().subscribe(done);
        break;
      case 'notebook':
        this.api.listNotes({ notebookId: this.view.id }).subscribe(done);
        break;
      case 'tag':
        this.api.listNotes({ tagId: this.view.id }).subscribe(done);
        break;
      case 'search':
        this.api.listNotes({ q: this.view.query }).subscribe(done);
        break;
      default:
        this.api.listNotes().subscribe(done);
    }
  }

  setView(view: ViewMode): void {
    this.view = view;
    this.searchQuery = '';
    this.refreshNotes();
  }

  onSearch(query: string): void {
    this.searchQuery = query;
    this.search$.next(query.trim());
  }

  newNote(): void {
    const notebookId = this.view.kind === 'notebook' ? this.view.id : null;
    this.api.createNote({ title: 'Sans titre', content: '', notebookId, pinned: false, tags: [] })
      .subscribe(note => {
        if (this.view.kind === 'trash') {
          this.view = { kind: 'all' };
        }
        this.refreshAll();
        this.selected = note;
      });
  }

  saveNote(payload: NotePayload): void {
    if (!this.selected) return;
    this.api.updateNote(this.selected.id, payload).subscribe(note => {
      this.selected = note;
      this.refreshNotes(true);
      this.api.listTags().subscribe(tags => this.tags = tags);
      this.api.listNotebooks().subscribe(nbs => this.notebooks = nbs);
    });
  }

  trashNote(note: Note): void {
    this.api.trashNote(note.id).subscribe(() => {
      this.selected = null;
      this.refreshAll();
    });
  }

  restoreNote(note: Note): void {
    this.api.restoreNote(note.id).subscribe(() => {
      this.selected = null;
      this.refreshAll();
    });
  }

  purgeNote(note: Note): void {
    if (confirm(`Supprimer définitivement « ${note.title} » ? Cette action est irréversible.`)) {
      this.api.purgeNote(note.id).subscribe(() => {
        this.selected = null;
        this.refreshAll();
      });
    }
  }

  createNotebook(name: string): void {
    this.api.createNotebook(name).subscribe(() => this.refreshAll());
  }

  deleteNotebook(notebook: Notebook): void {
    if (confirm(`Supprimer le carnet « ${notebook.name} » ? Les notes seront conservées.`)) {
      this.api.deleteNotebook(notebook.id).subscribe(() => {
        if (this.view.kind === 'notebook' && this.view.id === notebook.id) {
          this.view = { kind: 'all' };
        }
        this.refreshAll();
      });
    }
  }

  deleteTag(tag: Tag): void {
    if (confirm(`Supprimer l'étiquette « ${tag.name} » ?`)) {
      this.api.deleteTag(tag.id).subscribe(() => {
        if (this.view.kind === 'tag' && this.view.id === tag.id) {
          this.view = { kind: 'all' };
        }
        this.refreshAll();
      });
    }
  }

  uploadFile(file: File): void {
    if (!this.selected) return;
    this.api.uploadAttachment(this.selected.id, file).subscribe(att => {
      this.selected = { ...this.selected!, attachments: [...this.selected!.attachments, att] };
      this.refreshNotes(true);
    });
  }

  downloadFile(att: Attachment): void {
    this.api.downloadAttachment(att.id).subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = att.filename;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  removeAttachment(att: Attachment): void {
    this.api.deleteAttachment(att.id).subscribe(() => {
      if (this.selected) {
        this.selected = {
          ...this.selected,
          attachments: this.selected.attachments.filter(a => a.id !== att.id)
        };
      }
      this.refreshNotes(true);
    });
  }
}
