import { Card } from "./Card";

type MetricCardProps = {
  label: string;
  value: string;
  detail: string;
};

export function MetricCard({ label, value, detail }: MetricCardProps) {
  return (
    <Card className="p-4">
      <p className="break-words text-xs font-medium uppercase text-zinc-500">{label}</p>
      <p className="mt-2 break-words text-xl font-semibold text-zinc-50 sm:text-2xl">{value}</p>
      <p className="mt-1 text-sm text-zinc-400">{detail}</p>
    </Card>
  );
}
