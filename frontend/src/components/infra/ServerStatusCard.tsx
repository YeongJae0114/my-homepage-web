import type { Server } from "../../types/infra";
import { cn } from "../../utils/cn";
import { statusStyles } from "../../utils/statusStyles";
import { Badge } from "../common/Badge";
import { Card } from "../common/Card";
import { StatusBadge } from "../common/StatusBadge";
import { UptimeBar } from "./UptimeBar";

type ServerStatusCardProps = {
  server: Server;
};

export function ServerStatusCard({ server }: ServerStatusCardProps) {
  return (
    <Card interactive className={cn("p-5", statusStyles[server.status].cardClassName)}>
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="font-mono text-xs uppercase text-zinc-500">{server.role} / {server.environment}</p>
          <h3 className="mt-2 text-lg font-semibold text-zinc-50">{server.name}</h3>
        </div>
        <StatusBadge status={server.status} />
      </div>
      <p className="mt-3 min-h-12 text-sm leading-6 text-zinc-400">{server.description}</p>
      <div className="mt-5 grid grid-cols-2 gap-3 text-sm">
        <div>
          <p className="text-zinc-500">Uptime</p>
          <p className="mt-1 font-semibold text-zinc-100">{server.uptime.toFixed(2)}%</p>
        </div>
        <div>
          <p className="text-zinc-500">Latency</p>
          <p className="mt-1 font-semibold text-zinc-100">{server.latencyMs ? `${server.latencyMs}ms` : "idle"}</p>
        </div>
      </div>
      <div className="mt-4">
        <UptimeBar value={server.uptime} />
      </div>
      <div className="mt-5 flex flex-wrap gap-2">
        {server.tags.map((tag) => (
          <Badge key={tag}>{tag}</Badge>
        ))}
      </div>
      <p className="mt-5 text-xs text-zinc-500">Checked {new Date(server.lastCheckedAt).toLocaleString("ko-KR")}</p>
    </Card>
  );
}
