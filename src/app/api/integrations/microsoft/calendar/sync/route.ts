import { NextRequest, NextResponse } from "next/server";
import { getUserId } from "@/lib/get-user-id";
import { syncMicrosoftCalendar } from "@/lib/integrations/microsoft-graph";

export async function POST(_req: NextRequest) {
  const userId = await getUserId();

  try {
    const result = await syncMicrosoftCalendar(userId);
    return NextResponse.json({ success: true, ...result });
  } catch (error) {
    return NextResponse.json(
      { error: error instanceof Error ? error.message : "Sync mislukt" },
      { status: 500 }
    );
  }
}
