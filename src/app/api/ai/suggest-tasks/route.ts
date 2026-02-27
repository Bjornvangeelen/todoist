import { NextRequest, NextResponse } from "next/server";
import { getUserId } from "@/lib/get-user-id";
import { prisma } from "@/lib/prisma";
import { generateTasksFromEmail, generateTasksFromText } from "@/lib/ai/task-generator";
import { z } from "zod";

const schema = z.object({
  type: z.enum(["email", "text"]),
  text: z.string().min(1).max(10000),
  emailMeta: z
    .object({
      id: z.string().optional(),
      from: z.string(),
      subject: z.string(),
      date: z.string(),
      provider: z.string(),
    })
    .optional(),
});

export async function POST(req: NextRequest) {
  const userId = await getUserId();

  const body = await req.json();
  const parsed = schema.safeParse(body);
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const { type, text, emailMeta } = parsed.data;

  let suggestions;
  if (type === "email" && emailMeta) {
    suggestions = await generateTasksFromEmail({
      from: emailMeta.from,
      subject: emailMeta.subject,
      date: emailMeta.date,
      body: text,
    });
  } else {
    suggestions = await generateTasksFromText(text);
  }

  const suggestionRecord = await prisma.emailSuggestion.create({
    data: {
      emailId: emailMeta?.id,
      provider: emailMeta?.provider ?? "manual",
      subject: emailMeta?.subject,
      fromEmail: emailMeta?.from,
      rawContent: text,
      aiResponse: JSON.stringify(suggestions),
      status: "pending",
      userId,
    },
  });

  return NextResponse.json({
    suggestionId: suggestionRecord.id,
    suggestions,
  });
}
