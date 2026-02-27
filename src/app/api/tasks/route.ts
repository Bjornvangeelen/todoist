import { NextRequest, NextResponse } from "next/server";
import { getUserId } from "@/lib/get-user-id";
import { prisma } from "@/lib/prisma";
import { z } from "zod";

const createTaskSchema = z.object({
  title: z.string().min(1).max(500),
  description: z.string().optional(),
  priority: z.number().int().min(1).max(4).default(4),
  dueDate: z.string().optional(),
  dueTime: z.string().optional(),
  projectId: z.string().optional(),
  sectionId: z.string().optional(),
  labels: z.array(z.string()).optional(),
});

export async function GET(req: NextRequest) {
  const userId = await getUserId();

  const { searchParams } = new URL(req.url);
  const filter = searchParams.get("filter"); // "inbox" | "today" | "upcoming"
  const projectId = searchParams.get("projectId");

  const now = new Date();
  const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const todayEnd = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1);
  const weekEnd = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 7);

  const where: Record<string, unknown> = {
    userId,
    isDeleted: false,
  };

  if (filter === "inbox") {
    where.projectId = null;
    where.isCompleted = false;
  } else if (filter === "today") {
    where.dueDate = { gte: todayStart, lt: todayEnd };
    where.isCompleted = false;
  } else if (filter === "upcoming") {
    where.dueDate = { gte: todayStart, lt: weekEnd };
    where.isCompleted = false;
  } else if (projectId) {
    where.projectId = projectId;
    where.isCompleted = false;
  }

  const tasks = await prisma.task.findMany({
    where,
    include: {
      labels: { include: { label: true } },
      project: true,
      section: true,
      _count: { select: { comments: true } },
    },
    orderBy: [
      { priority: "asc" },
      { dueDate: "asc" },
      { createdAt: "desc" },
    ],
  });

  return NextResponse.json(tasks);
}

export async function POST(req: NextRequest) {
  const userId = await getUserId();

  const body = await req.json();
  const parsed = createTaskSchema.safeParse(body);
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const { labels, dueDate, ...data } = parsed.data;

  const task = await prisma.task.create({
    data: {
      ...data,
      dueDate: dueDate ? new Date(dueDate) : undefined,
      userId,
      labels: labels
        ? {
            create: labels.map((labelId) => ({ labelId })),
          }
        : undefined,
    },
    include: {
      labels: { include: { label: true } },
      project: true,
      section: true,
    },
  });

  return NextResponse.json(task, { status: 201 });
}
