import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Attachment, Note, Notebook, NotePayload } from '../models/models';

@Component({
  selector: 'app-editor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="editor" *ngIf="note; else placeholder">
      <header>
        <select [(ngModel)]="notebookId" [disabled]="note.deleted">
          <option [ngValue]="null">📓 Sans carnet</option>
          <option *ngFor="let nb of notebooks" [ngValue]="nb.id">📓 {{ nb.name }}</option>
        </select>

        <div class="actions">
          <ng-container *ngIf="!note.deleted">
            <button class="icon" [class.pinned]="pinned" (click)="pinned = !pinned" title="Épingler">★</button>
            <button class="save" (click)="submit()">Enregistrer</button>
            <button class="icon danger" *ngIf="note.id" (click)="trash.emit(note)" title="Envoyer à la corbeille">🗑️</button>
          </ng-container>
          <ng-container *ngIf="note.deleted">
            <button class="save" (click)="restore.emit(note)">♻️ Restaurer</button>
            <button class="purge" (click)="purge.emit(note)">Supprimer définitivement</button>
          </ng-container>
        </div>
      </header>

      <input class="title" placeholder="Titre" [(ngModel)]="title" [disabled]="note.deleted" />

      <input class="tags" placeholder="Étiquettes séparées par des virgules (ex: travail, idées)"
             [(ngModel)]="tagsInput" [disabled]="note.deleted" />

      <textarea class="content" placeholder="Commencez à écrire…"
                [(ngModel)]="content" [disabled]="note.deleted"></textarea>

      <footer *ngIf="note.id">
        <div class="attachments-header">
          <span>Pièces jointes ({{ note.attachments.length }})</span>
          <label class="upload" *ngIf="!note.deleted">
            + Ajouter un fichier
            <input type="file" hidden (change)="onFile($event)" />
          </label>
        </div>
        <div class="attachment" *ngFor="let att of note.attachments">
          <span class="filename" (click)="download.emit(att)" title="Télécharger">📎 {{ att.filename }}</span>
          <span class="size">{{ formatSize(att.size) }}</span>
          <button class="icon danger" *ngIf="!note.deleted" (click)="deleteAttachment.emit(att)">✕</button>
        </div>
      </footer>
    </section>

    <ng-template #placeholder>
      <section class="editor empty-state">
        <div>
          <div class="big">🐘</div>
          <p>Sélectionnez une note ou créez-en une nouvelle</p>
        </div>
      </section>
    </ng-template>
  `,
  styles: [`
    .editor {
      flex: 1; height: 100vh; background: var(--bg-editor);
      display: flex; flex-direction: column; padding: 16px 24px;
      gap: 12px; overflow-y: auto;
    }
    header { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
    select { padding: 6px 10px; font-size: 13px; }
    .actions { display: flex; align-items: center; gap: 8px; }
    .icon { font-size: 16px; padding: 6px; border-radius: 6px; color: var(--text-muted); }
    .icon:hover { background: var(--bg-hover); color: var(--text-primary); }
    .icon.pinned { color: var(--pin); }
    .icon.danger:hover { color: var(--danger); }
    .save {
      background: var(--accent); color: #fff; font-weight: 600;
      font-size: 13px; padding: 8px 16px; border-radius: 18px;
    }
    .save:hover { background: var(--accent-hover); }
    .purge {
      color: var(--danger); border: 1px solid var(--danger);
      font-size: 13px; padding: 7px 14px; border-radius: 18px;
    }
    .purge:hover { background: var(--danger); color: #fff; }
    .title {
      font-size: 26px; font-weight: 700; border: none; background: transparent;
      padding: 4px 0;
    }
    .tags { font-size: 12px; padding: 8px 12px; }
    .content {
      flex: 1; min-height: 300px; resize: none; border: none;
      background: transparent; font-size: 15px; line-height: 1.7; padding: 4px 0;
    }
    footer { border-top: 1px solid var(--border); padding-top: 12px; }
    .attachments-header {
      display: flex; justify-content: space-between; align-items: center;
      font-size: 12px; color: var(--text-muted); margin-bottom: 8px;
    }
    .upload { color: var(--accent); cursor: pointer; font-weight: 600; }
    .upload:hover { color: var(--accent-hover); }
    .attachment {
      display: flex; align-items: center; gap: 10px;
      padding: 6px 10px; border-radius: 6px; font-size: 13px;
    }
    .attachment:hover { background: var(--bg-hover); }
    .filename { cursor: pointer; }
    .filename:hover { color: var(--accent); }
    .size { color: var(--text-muted); font-size: 11px; }
    .empty-state {
      align-items: center; justify-content: center; display: flex;
      color: var(--text-muted); text-align: center;
    }
    .big { font-size: 60px; margin-bottom: 12px; }
  `]
})
export class EditorComponent {
  @Input() notebooks: Notebook[] = [];

  @Input() set note(value: Note | null) {
    this._note = value;
    this.title = value?.title ?? '';
    this.content = value?.content ?? '';
    this.notebookId = value?.notebookId ?? null;
    this.pinned = value?.pinned ?? false;
    this.tagsInput = value?.tags.join(', ') ?? '';
  }

  get note(): Note | null {
    return this._note;
  }

  @Output() save = new EventEmitter<NotePayload>();
  @Output() trash = new EventEmitter<Note>();
  @Output() restore = new EventEmitter<Note>();
  @Output() purge = new EventEmitter<Note>();
  @Output() upload = new EventEmitter<File>();
  @Output() download = new EventEmitter<Attachment>();
  @Output() deleteAttachment = new EventEmitter<Attachment>();

  private _note: Note | null = null;
  title = '';
  content = '';
  notebookId: number | null = null;
  pinned = false;
  tagsInput = '';

  submit(): void {
    this.save.emit({
      title: this.title.trim() || 'Sans titre',
      content: this.content,
      notebookId: this.notebookId,
      pinned: this.pinned,
      tags: this.tagsInput.split(',').map(t => t.trim()).filter(t => !!t)
    });
  }

  onFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.upload.emit(input.files[0]);
      input.value = '';
    }
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' o';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' Ko';
    return (bytes / 1024 / 1024).toFixed(1) + ' Mo';
  }
}
