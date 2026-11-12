import { Card } from "./Card";
import { cn } from "../../utils/cn";

type MetricCardProps = {
  label: string;
  value: string;
  detail: string;
  valueClassName?: string;
};

export function MetricCard({ label, value, detail, valueClassName }: MetricCardProps) {
  return (
    <Card className="p-4">
      <p className="break-words text-xs font-medium uppercase text-zinc-500">{label}</p>
      <p className={cn("mt-2 break-words font-mono text-xl font-semibold tabular-nums sm:text-2xl", valueClassName ?? "text-zinc-50")}>{value}</p>
      <p className="mt-1 text-sm text-zinc-400">{detail}</p>
    </Card>
  );
}
