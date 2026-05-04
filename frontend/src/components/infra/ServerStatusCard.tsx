import type { Server } from "../../types/infra";
import { cn } from "../../utils/cn";
import { statusStyles } from "../../utils/statusStyles";
import { Badge } from "../common/Badge";
import { Card } from "../common/Card";
import { StatusBadge } from "../common/StatusBadge";

type ServerStatusCardProps = {
  server: Server;
};

type ResourceMetricProps = {
  label: string;
  usedPercent: number;
  detail?: string;
};

function clampPercent(value: number) {
  return Math.min(100, Math.max(0, value));
}

function formatGb(value: number) {
  return Number.isInteger(value) ? value.toFixed(0) : value.toFixed(1);
}

function getUsageTone(usedPercent: number) {
  if (usedPercent > 90) {
    return "bg-rose-300 shadow-[0_0_14px_rgba(253,164,175,.35)]";
  }

  if (usedPercent > 75) {
    return "bg-amber-300 shadow-[0_0_14px_rgba(252,211,77,.3)]";
  }

  return "bg-emerald-300 shadow-[0_0_14px_rgba(110,231,183,.28)]";
}

function ResourceMetric({ label, usedPercent, detail }: ResourceMetricProps) {
  const safePercent = clampPercent(usedPercent);
  const visualPercent = safePercent > 0 ? Math.max(safePercent, 6) : 0;

  return (
    <div className="rounded-md border border-white/10 bg-white/[0.025] px-3 py-3">
      <div className="flex items-center justify-between gap-3">
        <p className="font-mono text-xs uppercase text-zinc-500">{label}</p>
        <p className="font-mono text-sm font-semibold text-zinc-50">{safePercent.toFixed(1)}%</p>
      </div>
      <div className="mt-2 h-2 overflow-hidden rounded-full bg-white/[0.08]" aria-label={`${label} usage ${safePercent.toFixed(1)}%`}>
        <div className={cn("h-full rounded-full transition-all duration-300", getUsageTone(safePercent))} style={{ width: `${visualPercent}%` }} />
      </div>
      {detail ? <p className="mt-2 text-xs text-zinc-500">{detail}</p> : null}
    </div>
  );
}

export function ServerStatusCard({ server }: ServerStatusCardProps) {
  const metrics = server.metrics;
  const memoryUsedGb = metrics ? Math.max(0, metrics.memory.totalGb - metrics.memory.availableGb) : 0;
  const diskUsedGb = metrics ? Math.max(0, metrics.disk.totalGb - metrics.disk.availableGb) : 0;
  const checkedAt = new Date(server.lastCheckedAt).toLocaleString("ko-KR", {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });

  return (
    <Card interactive className={cn("p-5", statusStyles[server.status].cardClassName)}>
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="min-w-0">
          <h3 className="break-words text-lg font-semibold text-zinc-50">{server.name}</h3>
          <p className="mt-2 text-sm leading-6 text-zinc-400">{server.description}</p>
        </div>
        <div className="shrink-0">
          <StatusBadge status={server.status} />
        </div>
      </div>
      <div className="mt-4 flex flex-wrap gap-2">
        <Badge tone="cyan" className="font-mono uppercase">{server.role}</Badge>
        {server.environment ? <Badge className="font-mono uppercase">{server.environment}</Badge> : null}
        {server.tags.map((tag) => (
          <Badge key={tag}>{tag}</Badge>
        ))}
      </div>

      <div className="mt-5 rounded-md border border-white/10 bg-white/[0.025] px-3 py-2">
        <p className="text-xs text-zinc-500">Last collected</p>
        <p className="mt-1 font-mono text-xs font-semibold text-zinc-200">{checkedAt}</p>
      </div>

      {metrics ? (
        <div className="mt-5 grid gap-3">
          <ResourceMetric label="CPU" usedPercent={metrics.cpu.usedPercent} />
          <ResourceMetric
            label="Memory"
            usedPercent={metrics.memory.usedPercent}
            detail={`${formatGb(memoryUsedGb)}GB / ${formatGb(metrics.memory.totalGb)}GB`}
          />
          <ResourceMetric
            label="Disk"
            usedPercent={metrics.disk.usedPercent}
            detail={`${formatGb(diskUsedGb)}GB / ${formatGb(metrics.disk.totalGb)}GB`}
          />
        </div>
      ) : null}

      {!metrics ? <p className="mt-5 rounded-md border border-amber-300/20 bg-amber-300/[0.08] px-3 py-2 text-xs text-amber-100">Hardware metrics are not available for this node yet.</p> : null}
    </Card>
  );
}
