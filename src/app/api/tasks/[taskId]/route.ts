import { NextRequest, NextResponse } from "next/server";
import { getUserId } from "@/lib/get-user-id";
import { prisma } from "@/lib/prisma";
import { z } from "zod";

const updateTaskSchema = z.object({
  title: z.string().min(1).max(500).optional(),
  description: z.string().optional().nullable(),
  priority: z.number().int().min(1).max(4).optional(),
  dueDate: z.string().optional().nullable(),
  dueTime: z.string().optional().nullable(),
  projectId: z.string().optional().nullable(),
  sectionId: z.string().optional().nullable(),
  isCompleted: z.boolean().optional(),
});

export async function GET(
  _req: NextRequest,
  { params }: { params: Promise<{ taskId: string }> }
) {
  const userId = await getUserId();
  const { taskId } = await params;

  const task = await prisma.task.findFirst({
    where: { id: taskId, userId, isDeleted: false },
    include: {
      labels: { include: { label: true } },
      project: true,
      section: true,
      comments: {
        include: { user: true },
        orderBy: { createdAt: "asc" },
      },
    },
  });

  if (!task) return NextResponse.json({ error: "Niet gevonden" }, { status: 404 });
  return NextResponse.json(task);
}

export async function PATCH(
  req: NextRequest,
  { params }: { params: Promise<{ taskId: string }> }
) {
  const userId = await getUserId();
  const { taskId } = await params;
  const body = await req.json();
  const parsed = updateTaskSchema.safeParse(body);
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const { dueDate, ...data } = parsed.data;

  const task = await prisma.task.updateMany({
    where: { id: taskId, userId },
    data: {
      ...data,
      ...(dueDate !== undefined && {
        dueDate: dueDate ? new Date(dueDate) : null,
      }),
    },
  });

  if (!task.count) return NextResponse.json({ error: "Niet gevonden" }, { status: 404 });

  const updated = await prisma.task.findUnique({
    where: { id: taskId },
    include: { labels: { include: { label: true } }, project: true, section: true },
  });

  return NextResponse.json(updated);
}

export async function DELETE(
  _req: NextRequest,
  { params }: { params: Promise<{ taskId: string }> }
) {
  const userId = await getUserId();
  const { taskId } = await params;

  await prisma.task.updateMany({
    where: { id: taskId, userId },
    data: { isDeleted: true },
  });

  return NextResponse.json({ success: true });
}
