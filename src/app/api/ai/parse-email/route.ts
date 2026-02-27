import { NextRequest, NextResponse } from "next/server";
import { getUserId } from "@/lib/get-user-id";
import { prisma } from "@/lib/prisma";
import { z } from "zod";

const acceptSchema = z.object({
  suggestionId: z.string(),
  taskIndices: z.array(z.number()),
  projectId: z.string().optional(),
});

export async function POST(req: NextRequest) {
  const userId = await getUserId();

  const body = await req.json();
  const parsed = acceptSchema.safeParse(body);
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const { suggestionId, taskIndices, projectId } = parsed.data;

  const suggestion = await prisma.emailSuggestion.findFirst({
    where: { id: suggestionId, userId },
  });

  if (!suggestion) return NextResponse.json({ error: "Suggestie niet gevonden" }, { status: 404 });

  const allTasks = JSON.parse(suggestion.aiResponse);
  const selectedTasks = taskIndices.map((i) => allTasks[i]).filter(Boolean);

  const createdTasks = await Promise.all(
    selectedTasks.map((task: { title: string; description?: string; priority?: number; dueDate?: string }) =>
      prisma.task.create({
        data: {
          title: task.title,
          description: task.description,
          priority: task.priority ?? 4,
          dueDate: task.dueDate ? new Date(task.dueDate) : undefined,
          projectId: projectId || undefined,
          userId,
        },
      })
    )
  );

  await prisma.emailSuggestion.update({
    where: { id: suggestionId },
    data: {
      status: taskIndices.length === allTasks.length ? "accepted" : "partial",
      taskId: createdTasks[0]?.id,
    },
  });

  return NextResponse.json({ created: createdTasks.length, tasks: createdTasks });
}
