import type { Task, Project, Section, Label, Comment, Integration, CalendarEvent, EmailSuggestion, User } from "@prisma/client";

export type TaskWithRelations = Task & {
  labels: { label: Label }[];
  project?: Project | null;
  section?: Section | null;
  comments?: Comment[];
};

export type ProjectWithSections = Project & {
  sections: Section[];
  _count?: { tasks: number };
};

export type AISuggestedTask = {
  title: string;
  description?: string;
  priority: 1 | 2 | 3 | 4;
  dueDate?: string;
  labels?: string[];
};

export type IntegrationProvider = "google" | "microsoft" | "imap";

export { Task, Project, Section, Label, Comment, Integration, CalendarEvent, EmailSuggestion, User };
