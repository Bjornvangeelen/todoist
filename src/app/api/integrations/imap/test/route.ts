import { NextRequest, NextResponse } from "next/server";
import { testImapConnection } from "@/lib/integrations/imap-client";
import { z } from "zod";

const schema = z.object({
  host: z.string().min(1),
  port: z.number().int().min(1).max(65535),
  secure: z.boolean(),
  user: z.string().email(),
  password: z.string().min(1),
});

export async function POST(req: NextRequest) {
  const body = await req.json();
  const parsed = schema.safeParse(body);
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const result = await testImapConnection(parsed.data);
  return NextResponse.json(result);
}
