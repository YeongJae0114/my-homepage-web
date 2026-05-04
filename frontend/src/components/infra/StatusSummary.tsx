import type { Server, Service } from "../../types/infra";
import { MetricCard } from "../common/MetricCard";

type StatusSummaryProps = {
  servers: Server[];
  services: Service[];
};

export function StatusSummary({ servers, services }: StatusSummaryProps) {
  const onlineServers = servers.filter((server) => server.status === "online").length;
  const incidentCount = [...servers, ...services].filter((item) => item.status === "offline" || item.status === "degraded").length;
  const latencyServers = servers.filter((server) => typeof server.latencyMs === "number" && server.latencyMs > 0);
  const uptimeServers = servers.filter((server) => typeof server.uptime === "number");
  const averageLatency = Math.round(
    latencyServers.reduce((sum, server) => sum + (server.latencyMs ?? 0), 0) /
      Math.max(1, latencyServers.length),
  );
  const availability =
    uptimeServers.reduce((sum, server) => sum + (server.uptime ?? 0), 0) / Math.max(1, uptimeServers.length);

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <MetricCard label="Servers" value={`${onlineServers}/${servers.length}`} detail="online nodes" />
      <MetricCard label="Incidents" value={`${incidentCount}`} detail="degraded or offline" />
      <MetricCard label="Avg Latency" value={`${averageLatency}ms`} detail="reachable nodes" />
      <MetricCard label="Availability" value={`${availability.toFixed(2)}%`} detail="dummy aggregate" />
    </div>
  );
}
