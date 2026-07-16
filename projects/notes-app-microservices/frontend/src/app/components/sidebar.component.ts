import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Notebook, Tag, ViewMode } from '../models/models';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <aside class="sidebar">
      <div class="user">
        <div class="avatar">{{ initial }}</div>
        <div class="user-info">
          <div class="user-name">{{ fullName }}</div>
          <button class="logout" (click)="logout.emit()">Se déconnecter</button>
        </div>
      </div>

      <button class="btn-accent new-note" (click)="newNote.emit()">
        <span class="plus">+</span> Nouvelle note
      </button>

      <nav>
        <button class="nav-item" [class.active]="view.kind === 'all'" (click)="select.emit({ kind: 'all' })">
          📝 Notes
        </button>

        <div class="section">
          <div class="section-header">
            <span>Carnets</span>
            <button class="add" title="Nouveau carnet" (click)="addingNotebook = !addingNotebook">+</button>
          </div>
          <form *ngIf="addingNotebook" (ngSubmit)="submitNotebook()">
            <input [(ngModel)]="notebookName" name="notebookName" placeholder="Nom du carnet" autofocus />
          </form>
          <button *ngFor="let nb of notebooks" class="nav-item sub"
                  [class.active]="view.kind === 'notebook' && view.id === nb.id"
                  (click)="select.emit({ kind: 'notebook', id: nb.id, name: nb.name })">
            📓 {{ nb.name }}
            <span class="count">{{ nb.noteCount }}</span>
            <span class="delete" title="Supprimer le carnet"
                  (click)="$event.stopPropagation(); deleteNotebook.emit(nb)">✕</span>
          </button>
        </div>

        <div class="section">
          <div class="section-header"><span>Étiquettes</span></div>
          <button *ngFor="let tag of tags" class="nav-item sub"
                  [class.active]="view.kind === 'tag' && view.id === tag.id"
                  (click)="select.emit({ kind: 'tag', id: tag.id, name: tag.name })">
            🏷️ {{ tag.name }}
            <span class="delete" title="Supprimer l'étiquette"
                  (click)="$event.stopPropagation(); deleteTag.emit(tag)">✕</span>
          </button>
        </div>

        <button class="nav-item trash" [class.active]="view.kind === 'trash'" (click)="select.emit({ kind: 'trash' })">
          🗑️ Corbeille
        </button>
      </nav>
    </aside>
  `,
  styles: [`
    .sidebar {
      width: 240px;
      min-width: 240px;
      height: 100vh;
      background: var(--bg-sidebar);
      border-right: 1px solid var(--border);
      display: flex;
      flex-direction: column;
      padding: 16px 10px;
      gap: 14px;
      overflow-y: auto;
    }
    .user { display: flex; align-items: center; gap: 10px; padding: 0 6px; }
    .avatar {
      width: 36px; height: 36px; border-radius: 50%;
      background: var(--accent); color: #fff;
      display: flex; align-items: center; justify-content: center;
      font-weight: 700; font-size: 16px;
    }
    .user-name { font-size: 14px; font-weight: 600; }
    .logout { font-size: 11px; color: var(--text-muted); padding: 0; }
    .logout:hover { color: var(--danger); }
    .new-note { justify-content: center; }
    .plus { font-size: 18px; line-height: 1; }
    nav { display: flex; flex-direction: column; gap: 2px; flex: 1; }
    .nav-item {
      display: flex; align-items: center; gap: 8px;
      padding: 8px 10px; border-radius: 6px;
      font-size: 14px; color: var(--text-secondary);
      text-align: left; width: 100%;
    }
    .nav-item:hover { background: var(--bg-hover); color: var(--text-primary); }
    .nav-item.active { background: var(--bg-active); color: var(--text-primary); }
    .nav-item .count { margin-left: auto; font-size: 11px; color: var(--text-muted); }
    .nav-item .delete { display: none; color: var(--text-muted); font-size: 11px; margin-left: 4px; }
    .nav-item:hover .delete { display: inline; }
    .nav-item .delete:hover { color: var(--danger); }
    .section { margin-top: 10px; }
    .section-header {
      display: flex; justify-content: space-between; align-items: center;
      padding: 4px 10px; font-size: 11px; font-weight: 600;
      text-transform: uppercase; letter-spacing: .5px; color: var(--text-muted);
    }
    .section-header .add { color: var(--text-muted); font-size: 15px; }
    .section-header .add:hover { color: var(--accent); }
    .section form { padding: 4px 8px; }
    .section input { width: 100%; padding: 6px 8px; font-size: 13px; }
    .trash { margin-top: auto; }
  `]
})
export class SidebarComponent {
  @Input() fullName = '';
  @Input() notebooks: Notebook[] = [];
  @Input() tags: Tag[] = [];
  @Input() view: ViewMode = { kind: 'all' };

  @Output() select = new EventEmitter<ViewMode>();
  @Output() newNote = new EventEmitter<void>();
  @Output() createNotebook = new EventEmitter<string>();
  @Output() deleteNotebook = new EventEmitter<Notebook>();
  @Output() deleteTag = new EventEmitter<Tag>();
  @Output() logout = new EventEmitter<void>();

  addingNotebook = false;
  notebookName = '';

  get initial(): string {
    return (this.fullName || '?').charAt(0).toUpperCase();
  }

  submitNotebook(): void {
    const name = this.notebookName.trim();
    if (name) {
      this.createNotebook.emit(name);
      this.notebookName = '';
      this.addingNotebook = false;
    }
  }
}
