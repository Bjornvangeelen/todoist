import { NextRequest, NextResponse } from "next/server";
import { getUserId } from "@/lib/get-user-id";
import { prisma } from "@/lib/prisma";
import { z } from "zod";

const createLabelSchema = z.object({
  name: z.string().min(1).max(50),
  color: z.string().default("#6b7280"),
});

export async function GET() {
  const userId = await getUserId();

  const labels = await prisma.label.findMany({
    where: { userId },
    include: { _count: { select: { tasks: true } } },
    orderBy: { name: "asc" },
  });

  return NextResponse.json(labels);
}

export async function POST(req: NextRequest) {
  const userId = await getUserId();

  const body = await req.json();
  const parsed = createLabelSchema.safeParse(body);
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const label = await prisma.label.create({
    data: { ...parsed.data, userId },
  });

  return NextResponse.json(label, { status: 201 });
}
