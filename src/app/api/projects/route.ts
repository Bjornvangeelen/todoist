import { NextRequest, NextResponse } from "next/server";
import { getUserId } from "@/lib/get-user-id";
import { prisma } from "@/lib/prisma";
import { z } from "zod";

const createProjectSchema = z.object({
  name: z.string().min(1).max(100),
  color: z.string().default("#6366f1"),
  icon: z.string().optional(),
  isFavorite: z.boolean().default(false),
});

export async function GET() {
  const userId = await getUserId();

  const projects = await prisma.project.findMany({
    where: { userId, isArchived: false },
    include: {
      sections: { orderBy: { sortOrder: "asc" } },
      _count: { select: { tasks: { where: { isCompleted: false, isDeleted: false } } } },
    },
    orderBy: [{ isFavorite: "desc" }, { sortOrder: "asc" }, { name: "asc" }],
  });

  return NextResponse.json(projects);
}

export async function POST(req: NextRequest) {
  const userId = await getUserId();

  const body = await req.json();
  const parsed = createProjectSchema.safeParse(body);
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const project = await prisma.project.create({
    data: {
      ...parsed.data,
      userId,
    },
    include: { sections: true, _count: { select: { tasks: true } } },
  });

  return NextResponse.json(project, { status: 201 });
}
