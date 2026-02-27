import { NextRequest, NextResponse } from "next/server";
import { getUserId } from "@/lib/get-user-id";
import { prisma } from "@/lib/prisma";

export async function POST(
  _req: NextRequest,
  { params }: { params: Promise<{ taskId: string }> }
) {
  const userId = await getUserId();
  const { taskId } = await params;

  const task = await prisma.task.findFirst({
    where: { id: taskId, userId },
  });

  if (!task) return NextResponse.json({ error: "Niet gevonden" }, { status: 404 });

  const updated = await prisma.task.update({
    where: { id: taskId },
    data: {
      isCompleted: !task.isCompleted,
      completedAt: !task.isCompleted ? new Date() : null,
    },
  });

  return NextResponse.json(updated);
}
