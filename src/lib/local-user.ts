import { prisma } from "@/lib/prisma";

export const LOCAL_USER_ID = "local-user-000000000001";

let initialized = false;

export async function ensureLocalUser(): Promise<string> {
  if (initialized) return LOCAL_USER_ID;

  await prisma.user.upsert({
    where: { id: LOCAL_USER_ID },
    create: {
      id: LOCAL_USER_ID,
      name: "Lokaal",
      email: "local@localhost",
    },
    update: {},
  });

  initialized = true;
  return LOCAL_USER_ID;
}
