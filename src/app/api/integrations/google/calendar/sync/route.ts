import { NextRequest, NextResponse } from "next/server";
import { getUserId } from "@/lib/get-user-id";
import { syncGoogleCalendar } from "@/lib/integrations/google-calendar";

export async function POST(_req: NextRequest) {
  const userId = await getUserId();

  try {
    const result = await syncGoogleCalendar(userId);
    return NextResponse.json({ success: true, ...result });
  } catch (error) {
    return NextResponse.json(
      { error: error instanceof Error ? error.message : "Sync mislukt" },
      { status: 500 }
    );
  }
}
