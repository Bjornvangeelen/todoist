import { createDecipheriv, createCipheriv, randomBytes, scryptSync } from "crypto";

// Encrypt/decrypt IMAP passwords
const ALGORITHM = "aes-256-gcm";

export function encrypt(text: string): string {
  const key = scryptSync(process.env.ENCRYPTION_KEY!, "salt", 32);
  const iv = randomBytes(16);
  const cipher = createCipheriv(ALGORITHM, key, iv);
  const encrypted = Buffer.concat([cipher.update(text, "utf8"), cipher.final()]);
  const tag = cipher.getAuthTag();
  return `${iv.toString("hex")}:${encrypted.toString("hex")}:${tag.toString("hex")}`;
}

export function decrypt(encryptedText: string): string {
  const [ivHex, encryptedHex, tagHex] = encryptedText.split(":");
  const key = scryptSync(process.env.ENCRYPTION_KEY!, "salt", 32);
  const iv = Buffer.from(ivHex, "hex");
  const encrypted = Buffer.from(encryptedHex, "hex");
  const tag = Buffer.from(tagHex, "hex");
  const decipher = createDecipheriv(ALGORITHM, key, iv);
  decipher.setAuthTag(tag);
  return Buffer.concat([decipher.update(encrypted), decipher.final()]).toString("utf8");
}

export interface ImapConfig {
  host: string;
  port: number;
  secure: boolean;
  user: string;
  password: string;
}

export interface ImapEmail {
  uid: number;
  subject: string;
  from: string;
  date: string;
  body: string;
}

export async function fetchImapEmails(config: ImapConfig, maxMessages = 20): Promise<ImapEmail[]> {
  // Dynamic import to avoid server startup issues
  const { ImapFlow } = await import("imapflow");
  const { simpleParser } = await import("mailparser");

  const client = new ImapFlow({
    host: config.host,
    port: config.port,
    secure: config.secure,
    auth: {
      user: config.user,
      pass: config.password,
    },
    logger: false,
  });

  const emails: ImapEmail[] = [];

  await client.connect();

  try {
    await client.mailboxOpen("INBOX");

    // Fetch recent unread messages
    const messages = client.fetch("1:*", {
      uid: true,
      envelope: true,
      bodyStructure: true,
    });

    const collected: { uid: number; subject: string; from: string; date: Date }[] = [];

    for await (const msg of messages) {
      collected.push({
        uid: msg.uid,
        subject: msg.envelope.subject ?? "(geen onderwerp)",
        from: msg.envelope.from?.[0]?.address ?? "",
        date: msg.envelope.date,
      });
    }

    // Get last N messages
    const recent = collected.slice(-maxMessages);

    for (const msg of recent) {
      try {
        const download = await client.download(String(msg.uid), undefined, { uid: true });
        const parsed = await simpleParser(download.content);
        emails.push({
          uid: msg.uid,
          subject: msg.subject,
          from: msg.from,
          date: msg.date.toISOString(),
          body: (parsed.text ?? "").slice(0, 2000),
        });
      } catch {
        // Skip problematic messages
      }
    }
  } finally {
    await client.logout();
  }

  return emails;
}

export async function testImapConnection(config: ImapConfig): Promise<{ success: boolean; error?: string }> {
  try {
    const { ImapFlow } = await import("imapflow");
    const client = new ImapFlow({
      host: config.host,
      port: config.port,
      secure: config.secure,
      auth: { user: config.user, pass: config.password },
      logger: false,
    });
    await client.connect();
    await client.logout();
    return { success: true };
  } catch (error) {
    return {
      success: false,
      error: error instanceof Error ? error.message : "Onbekende fout",
    };
  }
}
