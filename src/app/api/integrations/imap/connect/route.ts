import { NextRequest, NextResponse } from "next/server";
import { getUserId } from "@/lib/get-user-id";
import { prisma } from "@/lib/prisma";
import { encrypt, testImapConnection } from "@/lib/integrations/imap-client";
import { z } from "zod";

const imapSchema = z.object({
  host: z.string().min(1),
  port: z.number().int().min(1).max(65535).default(993),
  secure: z.boolean().default(true),
  user: z.string().email(),
  password: z.string().min(1),
});

export async function POST(req: NextRequest) {
  const userId = await getUserId();

  const body = await req.json();
  const parsed = imapSchema.safeParse(body);
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const config = parsed.data;

  const test = await testImapConnection(config);
  if (!test.success) {
    return NextResponse.json(
      { error: `Verbinding mislukt: ${test.error}` },
      { status: 400 }
    );
  }

  const encryptedPassword = encrypt(config.password);

  const integration = await prisma.integration.upsert({
    where: {
      userId_provider_email: {
        userId,
        provider: "imap",
        email: config.user,
      },
    },
    create: {
      provider: "imap",
      email: config.user,
      userId,
      metadata: JSON.stringify({
        host: config.host,
        port: config.port,
        secure: config.secure,
        encryptedPassword,
      }),
      isActive: true,
    },
    update: {
      metadata: JSON.stringify({
        host: config.host,
        port: config.port,
        secure: config.secure,
        encryptedPassword,
      }),
      isActive: true,
    },
  });

  return NextResponse.json({ success: true, id: integration.id });
}
