type UptimeBarProps = {
  value: number;
};

export function UptimeBar({ value }: UptimeBarProps) {
  return (
    <div className="h-2 overflow-hidden rounded-full bg-white/10" aria-label={`Uptime ${value}%`}>
      <div
        className="h-full rounded-full bg-emerald-300 transition-all"
        style={{ width: `${Math.max(0, Math.min(100, value))}%` }}
      />
    </div>
  );
}
