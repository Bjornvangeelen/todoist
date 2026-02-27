import { anthropic } from "./claude-client";
import {
  TASK_GENERATION_SYSTEM_PROMPT,
  EMAIL_TASK_PROMPT,
  CALENDAR_TASK_PROMPT,
} from "./prompts";
import type { AISuggestedTask } from "@/types";

export async function generateTasksFromEmail(params: {
  from: string;
  subject: string;
  date: string;
  body: string;
}): Promise<AISuggestedTask[]> {
  const today = new Date().toISOString().split("T")[0];

  const userMessage = EMAIL_TASK_PROMPT.replace("{FROM}", params.from)
    .replace("{SUBJECT}", params.subject)
    .replace("{DATE}", params.date)
    .replace("{BODY}", params.body)
    .replace("{TODAY}", today);

  return generateTasks(userMessage);
}

export async function generateTasksFromCalendarEvent(params: {
  title: string;
  description?: string | null;
  startAt: Date;
  endAt: Date;
}): Promise<AISuggestedTask[]> {
  const today = new Date().toISOString().split("T")[0];
  const eventContent = `
Titel: ${params.title}
Start: ${params.startAt.toLocaleString("nl-NL")}
Einde: ${params.endAt.toLocaleString("nl-NL")}
${params.description ? `Beschrijving: ${params.description}` : ""}
`.trim();

  const userMessage = CALENDAR_TASK_PROMPT.replace("{EVENT_CONTENT}", eventContent).replace(
    "{TODAY}",
    today
  );

  return generateTasks(userMessage);
}

export async function generateTasksFromText(text: string): Promise<AISuggestedTask[]> {
  const today = new Date().toISOString().split("T")[0];
  const userMessage = `Analyseer de volgende tekst en genereer actiepunten. Huidige datum: ${today}\n\n${text}`;
  return generateTasks(userMessage);
}

async function generateTasks(userMessage: string): Promise<AISuggestedTask[]> {
  try {
    const message = await anthropic.messages.create({
      model: "claude-sonnet-4-6",
      max_tokens: 1024,
      system: TASK_GENERATION_SYSTEM_PROMPT,
      messages: [{ role: "user", content: userMessage }],
    });

    const content = message.content[0];
    if (content.type !== "text") return [];

    // Extract JSON from response
    const text = content.text.trim();
    const jsonMatch = text.match(/\[[\s\S]*\]/);
    if (!jsonMatch) return [];

    const tasks = JSON.parse(jsonMatch[0]) as AISuggestedTask[];

    // Validate and sanitize
    return tasks
      .filter((t) => t.title && typeof t.title === "string")
      .map((t) => ({
        title: String(t.title).slice(0, 200),
        description: t.description ? String(t.description).slice(0, 1000) : undefined,
        priority: ([1, 2, 3, 4].includes(Number(t.priority)) ? Number(t.priority) : 4) as 1 | 2 | 3 | 4,
        dueDate: t.dueDate ? String(t.dueDate) : undefined,
      }));
  } catch (error) {
    console.error("AI task generation error:", error);
    return [];
  }
}
