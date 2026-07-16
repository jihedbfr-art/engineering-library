import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Note, ViewMode } from '../models/models';

@Component({
  selector: 'app-note-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="list">
      <header>
        <h2>{{ title }}</h2>
        <span class="count">{{ notes.length }} note{{ notes.length > 1 ? 's' : '' }}</span>
      </header>

      <div class="search">
        <input placeholder="🔍 Rechercher dans les notes…"
               [ngModel]="searchQuery"
               (ngModelChange)="search.emit($event)" />
      </div>

      <div class="cards">
        <article *ngFor="let note of notes" class="card"
                 [class.selected]="note.id === selectedId"
                 (click)="open.emit(note)">
          <div class="card-title">
            <span *ngIf="note.pinned" class="pin" title="Épinglée">★</span>
            {{ note.title || 'Sans titre' }}
          </div>
          <div class="card-snippet">{{ snippet(note) }}</div>
          <div class="card-meta">
            <span>{{ (note.deleted ? note.deletedAt : note.updatedAt) | date:'dd/MM/yyyy HH:mm' }}</span>
            <span *ngIf="note.notebookName" class="notebook">📓 {{ note.notebookName }}</span>
          </div>
          <div class="card-tags" *ngIf="note.tags.length">
            <span class="tag" *ngFor="let tag of note.tags">{{ tag }}</span>
          </div>
        </article>

        <div *ngIf="!notes.length" class="empty">
          {{ view.kind === 'trash' ? 'La corbeille est vide' : 'Aucune note ici' }}
        </div>
      </div>
    </section>
  `,
  styles: [`
    .list {
      width: 320px; min-width: 320px; height: 100vh;
      background: var(--bg-list);
      border-right: 1px solid var(--border);
      display: flex; flex-direction: column;
    }
    header {
      display: flex; align-items: baseline; gap: 10px;
      padding: 18px 16px 10px;
    }
    h2 { font-size: 18px; font-weight: 700; }
    .count { font-size: 12px; color: var(--text-muted); }
    .search { padding: 0 16px 12px; }
    .search input { width: 100%; padding: 8px 12px; font-size: 13px; border-radius: 18px; }
    .cards { flex: 1; overflow-y: auto; padding: 0 10px 10px; display: flex; flex-direction: column; gap: 8px; }
    .card {
      background: var(--bg-card); border: 1px solid var(--border);
      border-radius: 8px; padding: 12px; cursor: pointer;
      transition: border-color .15s;
    }
    .card:hover { border-color: var(--text-muted); }
    .card.selected { border-color: var(--accent); }
    .card-title { font-size: 14px; font-weight: 600; margin-bottom: 4px; }
    .pin { color: var(--pin); margin-right: 2px; }
    .card-snippet {
      font-size: 12px; color: var(--text-secondary);
      display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical;
      overflow: hidden; margin-bottom: 8px; white-space: pre-wrap;
    }
    .card-meta { display: flex; gap: 10px; font-size: 11px; color: var(--text-muted); }
    .card-tags { display: flex; flex-wrap: wrap; gap: 4px; margin-top: 6px; }
    .tag {
      font-size: 10px; padding: 2px 8px; border-radius: 10px;
      background: var(--bg-active); color: var(--text-secondary);
    }
    .empty { text-align: center; color: var(--text-muted); font-size: 13px; padding: 40px 0; }
  `]
})
export class NoteListComponent {
  @Input() notes: Note[] = [];
  @Input() selectedId: number | null = null;
  @Input() view: ViewMode = { kind: 'all' };
  @Input() searchQuery = '';

  @Output() open = new EventEmitter<Note>();
  @Output() search = new EventEmitter<string>();

  get title(): string {
    switch (this.view.kind) {
      case 'all': return 'Notes';
      case 'notebook': return this.view.name;
      case 'tag': return '# ' + this.view.name;
      case 'search': return 'Recherche';
      case 'trash': return 'Corbeille';
    }
  }

  snippet(note: Note): string {
    return (note.content || '').slice(0, 180);
  }
}
