import type { ServiceStatus } from "../../types/infra";
import { cn } from "../../utils/cn";
import { statusStyles } from "../../utils/statusStyles";

type StatusBadgeProps = {
  status: ServiceStatus;
  showDot?: boolean;
};

export function StatusBadge({ status, showDot = true }: StatusBadgeProps) {
  const style = statusStyles[status];

  return (
    <span className={cn("inline-flex items-center gap-2 rounded-md border px-2.5 py-1 text-xs font-semibold", style.badgeClassName)}>
      {showDot ? <span aria-hidden="true" className={cn("h-2 w-2 rounded-full", style.dotClassName)} /> : null}
      {style.label}
    </span>
  );
}
