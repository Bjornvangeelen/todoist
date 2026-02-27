import { NextRequest, NextResponse } from "next/server";
import { getUserId } from "@/lib/get-user-id";
import { fetchRecentEmails } from "@/lib/integrations/gmail";

export async function GET(_req: NextRequest) {
  const userId = await getUserId();

  try {
    const emails = await fetchRecentEmails(userId);
    return NextResponse.json(emails);
  } catch (error) {
    return NextResponse.json(
      { error: error instanceof Error ? error.message : "Fout bij ophalen emails" },
      { status: 500 }
    );
  }
}
