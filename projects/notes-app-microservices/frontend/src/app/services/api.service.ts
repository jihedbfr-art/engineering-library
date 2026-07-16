import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Activity, Attachment, Note, Notebook, NotePayload, Tag } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ApiService {

  private http = inject(HttpClient);
  private base = '/api';

  listNotes(params: { notebookId?: number; tagId?: number; q?: string } = {}): Observable<Note[]> {
    const query: Record<string, string> = {};
    if (params.notebookId != null) query['notebookId'] = String(params.notebookId);
    if (params.tagId != null) query['tagId'] = String(params.tagId);
    if (params.q) query['q'] = params.q;
    return this.http.get<Note[]>(`${this.base}/notes`, { params: query });
  }

  listTrash(): Observable<Note[]> {
    return this.http.get<Note[]>(`${this.base}/notes/trash`);
  }

  createNote(payload: NotePayload): Observable<Note> {
    return this.http.post<Note>(`${this.base}/notes`, payload);
  }

  updateNote(id: number, payload: NotePayload): Observable<Note> {
    return this.http.put<Note>(`${this.base}/notes/${id}`, payload);
  }

  trashNote(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/notes/${id}`);
  }

  restoreNote(id: number): Observable<Note> {
    return this.http.post<Note>(`${this.base}/notes/${id}/restore`, {});
  }

  purgeNote(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/notes/${id}/purge`);
  }

  listNotebooks(): Observable<Notebook[]> {
    return this.http.get<Notebook[]>(`${this.base}/notebooks`);
  }

  createNotebook(name: string): Observable<Notebook> {
    return this.http.post<Notebook>(`${this.base}/notebooks`, { name });
  }

  deleteNotebook(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/notebooks/${id}`);
  }

  listTags(): Observable<Tag[]> {
    return this.http.get<Tag[]>(`${this.base}/tags`);
  }

  deleteTag(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/tags/${id}`);
  }

  uploadAttachment(noteId: number, file: File): Observable<Attachment> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<Attachment>(`${this.base}/notes/${noteId}/attachments`, form);
  }

  downloadAttachment(id: number): Observable<Blob> {
    return this.http.get(`${this.base}/attachments/${id}`, { responseType: 'blob' });
  }

  deleteAttachment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/attachments/${id}`);
  }

  recentActivity(): Observable<Activity[]> {
    return this.http.get<Activity[]>(`${this.base}/activity`);
  }
}
