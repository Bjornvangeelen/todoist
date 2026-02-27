import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

// No auth required - app works without login
// OAuth is available optionally via /settings/integrations
export function proxy(_request: NextRequest) {
  return NextResponse.next();
}

export const config = {
  matcher: [],
};
