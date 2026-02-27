import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatDate(date: Date | string | null | undefined): string {
  if (!date) return "";
  const d = typeof date === "string" ? new Date(date) : date;
  const today = new Date();
  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);

  if (isSameDay(d, today)) return "Vandaag";
  if (isSameDay(d, tomorrow)) return "Morgen";
  if (isSameDay(d, yesterday)) return "Gisteren";

  return d.toLocaleDateString("nl-NL", { day: "numeric", month: "short" });
}

export function isSameDay(a: Date, b: Date): boolean {
  return (
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
  );
}

export function isOverdue(date: Date | string | null | undefined): boolean {
  if (!date) return false;
  const d = typeof date === "string" ? new Date(date) : date;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return d < today;
}

export const PRIORITY_COLORS = {
  1: "text-red-500",
  2: "text-orange-400",
  3: "text-blue-500",
  4: "text-gray-400",
} as const;

export const PRIORITY_LABELS = {
  1: "Urgent",
  2: "Hoog",
  3: "Normaal",
  4: "Geen",
} as const;

export const PROJECT_COLORS = [
  "#ef4444", "#f97316", "#eab308", "#22c55e",
  "#06b6d4", "#3b82f6", "#6366f1", "#8b5cf6",
  "#ec4899", "#64748b",
];
