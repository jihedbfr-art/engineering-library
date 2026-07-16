export interface Attachment {
  id: number;
  filename: string;
  contentType: string;
  size: number;
  createdAt: string;
}

export interface Note {
  id: number;
  title: string;
  content: string;
  pinned: boolean;
  deleted: boolean;
  createdAt: string;
  updatedAt: string;
  deletedAt: string | null;
  notebookId: number | null;
  notebookName: string | null;
  tags: string[];
  attachments: Attachment[];
}

export interface Notebook {
  id: number;
  name: string;
  createdAt: string;
  noteCount: number;
}

export interface Tag {
  id: number;
  name: string;
}

export interface Activity {
  id: number;
  eventType: string;
  noteId: number;
  noteTitle: string;
  occurredAt: string;
}

export interface NotePayload {
  title: string;
  content: string;
  notebookId: number | null;
  pinned: boolean;
  tags: string[];
}

export type ViewMode =
  | { kind: 'all' }
  | { kind: 'notebook'; id: number; name: string }
  | { kind: 'tag'; id: number; name: string }
  | { kind: 'search'; query: string }
  | { kind: 'trash' };
