import { NextRequest, NextResponse } from "next/server";
import { getUserId } from "@/lib/get-user-id";
import { prisma } from "@/lib/prisma";
import { z } from "zod";

const updateProjectSchema = z.object({
  name: z.string().min(1).max(100).optional(),
  color: z.string().optional(),
  icon: z.string().optional().nullable(),
  isFavorite: z.boolean().optional(),
  isArchived: z.boolean().optional(),
});

export async function GET(
  _req: NextRequest,
  { params }: { params: Promise<{ projectId: string }> }
) {
  const userId = await getUserId();
  const { projectId } = await params;

  const project = await prisma.project.findFirst({
    where: { id: projectId, userId },
    include: {
      sections: {
        orderBy: { sortOrder: "asc" },
        include: {
          tasks: {
            where: { isDeleted: false },
            include: { labels: { include: { label: true } } },
            orderBy: [{ priority: "asc" }, { dueDate: "asc" }],
          },
        },
      },
      tasks: {
        where: { sectionId: null, isDeleted: false },
        include: { labels: { include: { label: true } } },
        orderBy: [{ priority: "asc" }, { dueDate: "asc" }],
      },
    },
  });

  if (!project) return NextResponse.json({ error: "Niet gevonden" }, { status: 404 });
  return NextResponse.json(project);
}

export async function PATCH(
  req: NextRequest,
  { params }: { params: Promise<{ projectId: string }> }
) {
  const userId = await getUserId();
  const { projectId } = await params;
  const body = await req.json();
  const parsed = updateProjectSchema.safeParse(body);
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const project = await prisma.project.updateMany({
    where: { id: projectId, userId },
    data: parsed.data,
  });

  if (!project.count) return NextResponse.json({ error: "Niet gevonden" }, { status: 404 });

  const updated = await prisma.project.findUnique({ where: { id: projectId } });
  return NextResponse.json(updated);
}

export async function DELETE(
  _req: NextRequest,
  { params }: { params: Promise<{ projectId: string }> }
) {
  const userId = await getUserId();
  const { projectId } = await params;

  await prisma.project.deleteMany({
    where: { id: projectId, userId },
  });

  return NextResponse.json({ success: true });
}
