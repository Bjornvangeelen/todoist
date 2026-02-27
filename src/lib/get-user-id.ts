import { auth } from "@/lib/auth";
import { ensureLocalUser } from "@/lib/local-user";

export async function getUserId(): Promise<string> {
  const session = await auth();
  if (session?.user?.id) return session.user.id;
  return await ensureLocalUser();
}
