import { servers } from "../data/servers";
import { services } from "../data/services";

export async function getStatusSnapshot() {
  return {
    servers,
    services,
    generatedAt: new Date().toISOString(),
  };
}
