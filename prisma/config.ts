import path from "path";
import { defineConfig } from "prisma/config";

const dbPath = path.resolve(process.cwd(), "dev.db");

export default defineConfig({
  schema: path.join(__dirname, "schema.prisma"),
  datasource: {
    url: `file:${dbPath}`,
  },
});
