import { prisma } from "@/lib/prisma";

const GRAPH_API = "https://graph.microsoft.com/v1.0";

export async function getMicrosoftToken(userId: string): Promise<string> {
  const integration = await prisma.integration.findFirst({
    where: { userId, provider: "microsoft", isActive: true },
  });

  if (!integration?.accessToken) {
    throw new Error("Microsoft integratie niet gevonden.");
  }

  // Check if token needs refresh
  if (integration.expiresAt && integration.expiresAt < new Date()) {
    if (!integration.refreshToken) {
      throw new Error("Microsoft token verlopen en geen refresh token beschikbaar.");
    }

    const res = await fetch(
      `https://login.microsoftonline.com/${process.env.MICROSOFT_TENANT_ID ?? "common"}/oauth2/v2.0/token`,
      {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({
          client_id: process.env.MICROSOFT_CLIENT_ID!,
          client_secret: process.env.MICROSOFT_CLIENT_SECRET!,
          refresh_token: integration.refreshToken,
          grant_type: "refresh_token",
          scope: "offline_access Calendars.ReadWrite Mail.Read User.Read",
        }),
      }
    );

    const tokens = await res.json();
    await prisma.integration.update({
      where: { id: integration.id },
      data: {
        accessToken: tokens.access_token,
        refreshToken: tokens.refresh_token ?? integration.refreshToken,
        expiresAt: new Date(Date.now() + tokens.expires_in * 1000),
      },
    });

    return tokens.access_token;
  }

  return integration.accessToken;
}

export async function syncMicrosoftCalendar(userId: string) {
  const token = await getMicrosoftToken(userId);

  const integration = await prisma.integration.findFirst({
    where: { userId, provider: "microsoft" },
  });

  const timeMin = new Date();
  timeMin.setDate(timeMin.getDate() - 1);
  const timeMax = new Date();
  timeMax.setDate(timeMax.getDate() + 60);

  const url = `${GRAPH_API}/me/calendarview?startDateTime=${timeMin.toISOString()}&endDateTime=${timeMax.toISOString()}&$top=100&$select=id,subject,start,end,isAllDay,bodyPreview,webLink`;

  const res = await fetch(url, {
    headers: { Authorization: `Bearer ${token}` },
  });

  if (!res.ok) throw new Error("Fout bij ophalen Microsoft Calendar");

  const data = await res.json();
  const events = data.value ?? [];
  let newEvents = 0;

  for (const event of events) {
    const startAt = new Date(event.start.dateTime ?? event.start.date);
    const endAt = new Date(event.end.dateTime ?? event.end.date);

    const existing = await prisma.calendarEvent.findFirst({
      where: { userId, provider: "microsoft", externalId: event.id },
    });

    if (existing) {
      await prisma.calendarEvent.update({
        where: { id: existing.id },
        data: {
          title: event.subject ?? "Naamloos evenement",
          description: event.bodyPreview,
          startAt,
          endAt,
          isAllDay: event.isAllDay,
          htmlLink: event.webLink,
          syncedAt: new Date(),
        },
      });
    } else {
      await prisma.calendarEvent.create({
        data: {
          externalId: event.id,
          provider: "microsoft",
          title: event.subject ?? "Naamloos evenement",
          description: event.bodyPreview,
          startAt,
          endAt,
          isAllDay: event.isAllDay,
          htmlLink: event.webLink,
          userId,
        },
      });
      newEvents++;
    }
  }

  return { synced: events.length, newEvents };
}

export async function fetchMicrosoftEmails(
  userId: string,
  maxResults = 20
): Promise<{ id: string; subject: string; from: string; body: string; date: string }[]> {
  const token = await getMicrosoftToken(userId);

  const url = `${GRAPH_API}/me/messages?$filter=isRead eq false&$top=${maxResults}&$select=id,subject,from,receivedDateTime,body&$orderby=receivedDateTime desc`;

  const res = await fetch(url, {
    headers: { Authorization: `Bearer ${token}` },
  });

  if (!res.ok) throw new Error("Fout bij ophalen Microsoft emails");

  const data = await res.json();

  return (data.value ?? []).map((msg: {
    id: string;
    subject: string;
    from: { emailAddress: { address: string; name: string } };
    receivedDateTime: string;
    body: { content: string };
  }) => ({
    id: msg.id,
    subject: msg.subject ?? "(geen onderwerp)",
    from: msg.from?.emailAddress?.address ?? "",
    date: msg.receivedDateTime,
    body: (msg.body?.content ?? "").replace(/<[^>]*>/g, "").slice(0, 2000),
  }));
}
