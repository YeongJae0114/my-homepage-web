import type { Service } from "../../types/infra";
import { Badge } from "../common/Badge";
import { Card } from "../common/Card";
import { StatusBadge } from "../common/StatusBadge";

type ServiceStatusCardProps = {
  service: Service;
};

export function ServiceStatusCard({ service }: ServiceStatusCardProps) {
  return (
    <Card className="p-4">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h3 className="font-semibold text-zinc-50">{service.name}</h3>
          <p className="mt-1 font-mono text-xs text-zinc-500">{service.endpoint}</p>
        </div>
        <StatusBadge status={service.status} />
      </div>
      <p className="mt-3 text-sm leading-6 text-zinc-400">{service.description}</p>
      <div className="mt-4 flex flex-wrap gap-2">
        <Badge tone={service.isPublic ? "emerald" : "cyan"}>{service.isPublic ? "public" : "private"}</Badge>
        <Badge>{service.type}</Badge>
        <Badge>{service.uptime.toFixed(1)}%</Badge>
      </div>
    </Card>
  );
}
