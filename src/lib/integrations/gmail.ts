import { google } from "googleapis";
import { getGoogleAuthClient } from "./google-calendar";

export interface ParsedEmail {
  id: string;
  subject: string;
  from: string;
  date: string;
  body: string;
  snippet: string;
}

export async function fetchRecentEmails(
  userId: string,
  maxResults = 20
): Promise<ParsedEmail[]> {
  const { oauth2Client } = await getGoogleAuthClient(userId);
  const gmail = google.gmail({ version: "v1", auth: oauth2Client });

  // Fetch recent unread emails from last 7 days
  const query = "is:unread newer_than:7d -category:promotions -category:social";

  const listResponse = await gmail.users.messages.list({
    userId: "me",
    q: query,
    maxResults,
  });

  const messageIds = listResponse.data.messages ?? [];
  if (messageIds.length === 0) return [];

  const emails: ParsedEmail[] = [];

  for (const msg of messageIds.slice(0, 10)) {
    if (!msg.id) continue;

    const message = await gmail.users.messages.get({
      userId: "me",
      id: msg.id,
      format: "full",
    });

    const headers = message.data.payload?.headers ?? [];
    const subject = headers.find((h) => h.name === "Subject")?.value ?? "(geen onderwerp)";
    const from = headers.find((h) => h.name === "From")?.value ?? "";
    const date = headers.find((h) => h.name === "Date")?.value ?? "";

    // Extract body text
    let body = "";
    const parts = message.data.payload?.parts ?? [];
    const textPart = parts.find((p) => p.mimeType === "text/plain");
    if (textPart?.body?.data) {
      body = Buffer.from(textPart.body.data, "base64").toString("utf-8");
    } else if (message.data.payload?.body?.data) {
      body = Buffer.from(message.data.payload.body.data, "base64").toString("utf-8");
    }

    emails.push({
      id: msg.id,
      subject,
      from,
      date,
      body: body.slice(0, 2000), // Limit for AI processing
      snippet: message.data.snippet ?? "",
    });
  }

  return emails;
}
