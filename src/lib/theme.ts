export type ThemeId =
  | "todoist"
  | "ocean"
  | "forest"
  | "purple"
  | "sunset"
  | "rose"
  | "teal"
  | "dark"
  | "indigo"
  | "amber";

export interface Theme {
  id: ThemeId;
  name: string;
  primary: string;
  sidebar: string;
}

export const THEMES: Theme[] = [
  { id: "todoist", name: "Todoist Rood",     primary: "#db4035", sidebar: "#1f2937" },
  { id: "ocean",   name: "Oceaan Blauw",     primary: "#2563eb", sidebar: "#0f172a" },
  { id: "forest",  name: "Woud Groen",       primary: "#16a34a", sidebar: "#052e16" },
  { id: "purple",  name: "Paars Magie",      primary: "#7c3aed", sidebar: "#2e1065" },
  { id: "sunset",  name: "Zonsondergang",    primary: "#ea580c", sidebar: "#1c1917" },
  { id: "rose",    name: "Roze Bloesem",     primary: "#e11d48", sidebar: "#1f0a14" },
  { id: "teal",    name: "Teal Modern",      primary: "#0891b2", sidebar: "#042f2e" },
  { id: "dark",    name: "Donker Pro",       primary: "#94a3b8", sidebar: "#0a0a0a" },
  { id: "indigo",  name: "Indigo Nacht",     primary: "#4338ca", sidebar: "#1e1b4b" },
  { id: "amber",   name: "Amber Warm",       primary: "#d97706", sidebar: "#1c1007" },
];

export const DEFAULT_THEME: ThemeId = "todoist";
