import type { Server, Service } from "../../types/infra";
import type { MonitoringSummaryViewModel } from "../../types/pages";
import { MetricCard } from "../common/MetricCard";

type StatusSummaryProps = {
  summary?: MonitoringSummaryViewModel;
  servers: Server[];
  services: Service[];
};

function getLatestTimestamp(servers: Server[], summary?: MonitoringSummaryViewModel) {
  if (summary?.lastUpdatedAt) {
    return summary.lastUpdatedAt;
  }

  return [...servers]
    .map((server) => server.lastCheckedAt)
    .sort((a, b) => b.localeCompare(a))[0];
}

function formatUpdatedAt(value?: string) {
  if (!value) {
    return "No collection yet";
  }

  return new Date(value).toLocaleString("ko-KR", {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function getPeakResource(servers: Server[]) {
  const resourceValues = servers.flatMap((server) => {
    if (!server.metrics) {
      return [];
    }

    return [
      { label: "CPU", value: server.metrics.cpu.usedPercent },
      { label: "MEM", value: server.metrics.memory.usedPercent },
      { label: "DISK", value: server.metrics.disk.usedPercent },
    ];
  });

  return resourceValues.sort((a, b) => b.value - a.value)[0];
}

export function StatusSummary({ summary, servers, services }: StatusSummaryProps) {
  const onlineServers = servers.filter((server) => server.status === "online").length;
  const onlineServices = services.filter((service) => service.status === "online").length;
  const incidentCount = [...servers, ...services].filter((item) => item.status === "offline" || item.status === "degraded").length;
  const peakResource = getPeakResource(servers);
  const latestTimestamp = getLatestTimestamp(servers, summary);
  const statusValue = summary?.status === "operational" ? "Operational" : summary?.status ?? (incidentCount > 0 ? "Attention" : "Operational");

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <MetricCard label="Fleet Status" value={statusValue} detail={summary?.message ?? "Monitoring snapshot is available"} />
      <MetricCard label="Servers" value={`${onlineServers}/${servers.length}`} detail={incidentCount > 0 ? `${incidentCount} needs attention` : "all monitored nodes online"} />
      <MetricCard label="Services" value={`${onlineServices}/${services.length}`} detail="online application endpoints" />
      <MetricCard
        label="Peak Resource"
        value={peakResource ? `${peakResource.label} ${peakResource.value.toFixed(1)}%` : "n/a"}
        detail={`updated ${formatUpdatedAt(latestTimestamp)}`}
      />
    </div>
  );
}
