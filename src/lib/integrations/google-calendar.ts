import { google } from "googleapis";
import { prisma } from "@/lib/prisma";

export async function getGoogleAuthClient(userId: string) {
  const integration = await prisma.integration.findFirst({
    where: { userId, provider: "google", isActive: true },
  });

  if (!integration?.accessToken) {
    throw new Error("Google integratie niet gevonden. Verbind je Google account eerst.");
  }

  const oauth2Client = new google.auth.OAuth2(
    process.env.GOOGLE_CLIENT_ID,
    process.env.GOOGLE_CLIENT_SECRET
  );

  oauth2Client.setCredentials({
    access_token: integration.accessToken,
    refresh_token: integration.refreshToken,
    expiry_date: integration.expiresAt?.getTime(),
  });

  // Auto-refresh token if expired
  oauth2Client.on("tokens", async (tokens) => {
    await prisma.integration.update({
      where: { id: integration.id },
      data: {
        accessToken: tokens.access_token ?? integration.accessToken,
        refreshToken: tokens.refresh_token ?? integration.refreshToken,
        expiresAt: tokens.expiry_date ? new Date(tokens.expiry_date) : undefined,
      },
    });
  });

  return { oauth2Client, integration };
}

export async function syncGoogleCalendar(userId: string) {
  const { oauth2Client, integration } = await getGoogleAuthClient(userId);
  const calendar = google.calendar({ version: "v3", auth: oauth2Client });

  const params: {
    calendarId: string;
    maxResults: number;
    singleEvents: boolean;
    orderBy: string;
    syncToken?: string;
    timeMin?: string;
  } = {
    calendarId: "primary",
    maxResults: 100,
    singleEvents: true,
    orderBy: "startTime",
  };

  if (integration.syncToken) {
    params.syncToken = integration.syncToken;
  } else {
    // Initial sync: fetch upcoming 30 days
    const timeMin = new Date();
    timeMin.setDate(timeMin.getDate() - 1);
    params.timeMin = timeMin.toISOString();
  }

  const response = await calendar.events.list(params);
  const events = response.data.items ?? [];
  const nextSyncToken = response.data.nextSyncToken;

  // Store sync token for incremental updates
  if (nextSyncToken) {
    await prisma.integration.update({
      where: { id: integration.id },
      data: { syncToken: nextSyncToken },
    });
  }

  // Upsert events in database
  const createdTasks: string[] = [];

  for (const event of events) {
    if (!event.id || event.status === "cancelled") {
      // Delete cancelled events
      await prisma.calendarEvent.deleteMany({
        where: { userId, provider: "google", externalId: event.id! },
      });
      continue;
    }

    const startAt = event.start?.dateTime
      ? new Date(event.start.dateTime)
      : new Date(event.start?.date ?? "");
    const endAt = event.end?.dateTime
      ? new Date(event.end.dateTime)
      : new Date(event.end?.date ?? "");
    const isAllDay = !!event.start?.date && !event.start.dateTime;

    const existing = await prisma.calendarEvent.findFirst({
      where: { userId, provider: "google", externalId: event.id },
    });

    if (existing) {
      await prisma.calendarEvent.update({
        where: { id: existing.id },
        data: {
          title: event.summary ?? "Naamloos evenement",
          description: event.description,
          startAt,
          endAt,
          isAllDay,
          htmlLink: event.htmlLink,
          syncedAt: new Date(),
        },
      });
    } else {
      const calEvent = await prisma.calendarEvent.create({
        data: {
          externalId: event.id,
          provider: "google",
          title: event.summary ?? "Naamloos evenement",
          description: event.description,
          startAt,
          endAt,
          isAllDay,
          htmlLink: event.htmlLink,
          userId,
        },
      });
      createdTasks.push(calEvent.id);
    }
  }

  return {
    synced: events.length,
    newEvents: createdTasks.length,
  };
}

export async function createGoogleCalendarEvent(
  userId: string,
  task: { title: string; description?: string | null; dueDate: Date; dueTime?: string | null }
) {
  const { oauth2Client } = await getGoogleAuthClient(userId);
  const calendar = google.calendar({ version: "v3", auth: oauth2Client });

  const startDate = task.dueDate;
  let startDateTime: string | undefined;
  let endDateTime: string | undefined;
  let startDateOnly: string | undefined;

  if (task.dueTime) {
    const [hours, minutes] = task.dueTime.split(":").map(Number);
    startDate.setHours(hours, minutes, 0, 0);
    const endDate = new Date(startDate.getTime() + 60 * 60 * 1000); // +1 hour
    startDateTime = startDate.toISOString();
    endDateTime = endDate.toISOString();
  } else {
    startDateOnly = startDate.toISOString().split("T")[0];
  }

  const event = await calendar.events.insert({
    calendarId: "primary",
    requestBody: {
      summary: task.title,
      description: task.description ?? undefined,
      start: startDateTime
        ? { dateTime: startDateTime }
        : { date: startDateOnly },
      end: endDateTime
        ? { dateTime: endDateTime }
        : { date: startDateOnly },
    },
  });

  return event.data;
}
