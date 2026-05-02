import type { Service } from "../../types/infra";
import { cn } from "../../utils/cn";
import { statusStyles } from "../../utils/statusStyles";
import { Badge } from "../common/Badge";
import { Card } from "../common/Card";
import { StatusBadge } from "../common/StatusBadge";
import { UptimeBar } from "./UptimeBar";

type ServiceStatusCardProps = {
  service: Service;
};

export function ServiceStatusCard({ service }: ServiceStatusCardProps) {
  return (
    <Card interactive className={cn("p-5", statusStyles[service.status].cardClassName)}>
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="break-words font-mono text-xs uppercase text-zinc-500">{service.type} / {service.isPublic ? "public" : "private"}</p>
          <h3 className="mt-2 break-words text-lg font-semibold text-zinc-50">{service.name}</h3>
        </div>
        <div className="shrink-0">
          <StatusBadge status={service.status} />
        </div>
      </div>
      <p className="mt-3 min-h-12 text-sm leading-6 text-zinc-400">{service.description}</p>
      <div className="mt-5 rounded-md border border-white/10 bg-white/[0.025] px-3 py-2">
        <p className="text-xs text-zinc-500">Endpoint</p>
        <p className="mt-1 break-all font-mono text-xs text-zinc-200">{service.endpoint}</p>
      </div>
      <div className="mt-5 grid grid-cols-2 gap-3 text-sm">
        <div>
          <p className="text-zinc-500">Uptime</p>
          <p className="mt-1 font-semibold text-zinc-100">{service.uptime.toFixed(2)}%</p>
        </div>
        <div>
          <p className="text-zinc-500">Latency</p>
          <p className="mt-1 font-semibold text-zinc-100">{service.latencyMs ? `${service.latencyMs}ms` : "idle"}</p>
        </div>
      </div>
      <div className="mt-4">
        <UptimeBar value={service.uptime} />
      </div>
      <div className="mt-5 flex flex-wrap gap-2">
        {service.tags.map((tag) => (
          <Badge key={tag}>{tag}</Badge>
        ))}
      </div>
      <p className="mt-5 text-xs text-zinc-500">Checked {new Date(service.lastCheckedAt).toLocaleString("ko-KR")}</p>
    </Card>
  );
}
