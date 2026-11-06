import { Area, CartesianGrid, Cell, ComposedChart, Line, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { formatKrw, formatPercent } from "./labels";
import { EmptyState } from "./fields";

export type DonutSlice = { key: string; label: string; amount: number; weightPercent: number | null };

// Readable on the dark surface; first three match the asset-group accents used elsewhere.
const PALETTE = ["#6ee7b7", "#67e8f9", "#c4b5fd", "#fcd34d", "#fda4af", "#93c5fd", "#bef264", "#f9a8d4"];
const MASK = "₩•••••";

/** 1.2억 / 3,450만 style axis labels. */
function compactKrw(value: number): string {
  const abs = Math.abs(value);
  if (abs >= 100_000_000) return `${(value / 100_000_000).toFixed(1)}억`;
  if (abs >= 10_000) return `${Math.round(value / 10_000).toLocaleString("ko-KR")}만`;
  return value.toLocaleString("ko-KR");
}

const tooltipStyle = "rounded-md border border-white/10 bg-surface-900/95 px-3 py-2 text-xs text-zinc-200 shadow-glow";

export function AllocationDonut({ slices, hidden }: { slices: DonutSlice[]; hidden: boolean }) {
  const data = slices.filter((slice) => slice.amount > 0);
  if (data.length === 0) {
    return <EmptyState>표시할 자산이 없습니다.</EmptyState>;
  }
  const total = data.reduce((sum, slice) => sum + slice.amount, 0);

  return (
    <div className="grid items-center gap-5 sm:grid-cols-[13rem_1fr]">
      <div className="relative mx-auto h-52 w-52">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie data={data} dataKey="amount" nameKey="label" innerRadius="64%" outerRadius="96%" paddingAngle={1} stroke="none" isAnimationActive={false}>
              {data.map((slice, index) => (
                <Cell key={slice.key} fill={PALETTE[index % PALETTE.length]} />
              ))}
            </Pie>
            <Tooltip
              content={({ active, payload }) => {
                const slice = payload?.[0]?.payload as DonutSlice | undefined;
                if (!active || !slice) return null;
                return (
                  <div className={tooltipStyle}>
                    {slice.label}: {hidden ? MASK : formatKrw(slice.amount)} ({formatPercent(slice.weightPercent).replace("+", "")})
                  </div>
                );
              }}
            />
          </PieChart>
        </ResponsiveContainer>
        <div className="pointer-events-none absolute inset-0 grid place-content-center text-center">
          <span className="text-xs text-zinc-500">합계</span>
          <span className="font-mono text-sm font-semibold tabular-nums text-zinc-100">{hidden ? MASK : compactKrw(total)}</span>
        </div>
      </div>
      <ul className="grid gap-2 text-sm">
        {data.map((slice, index) => (
          <li key={slice.key} className="flex items-center justify-between gap-3 border-b border-white/5 pb-2">
            <span className="flex min-w-0 items-center gap-2 text-zinc-300">
              <span aria-hidden="true" className="h-2.5 w-2.5 shrink-0 rounded-full" style={{ backgroundColor: PALETTE[index % PALETTE.length] }} />
              <span className="truncate">{slice.label}</span>
            </span>
            <span className="shrink-0 font-mono tabular-nums text-zinc-100">
              {hidden ? MASK : formatKrw(slice.amount)}
              <span className="ml-2 text-xs text-zinc-500">{formatPercent(slice.weightPercent).replace("+", "")}</span>
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}

type HistoryPoint = { date: string; totalAssets: number; totalLiabilities: number; netWorth: number };

export function NetWorthChart({ points, hidden }: { points: HistoryPoint[]; hidden: boolean }) {
  if (points.length < 2) {
    return <EmptyState>스냅샷이 쌓이면 추이가 표시됩니다 (평일 18:00 자동 저장).</EmptyState>;
  }

  return (
    <div className="h-64 w-full" role="img" aria-label="순자산과 총자산 추이">
      <ResponsiveContainer width="100%" height="100%">
        <ComposedChart data={points} margin={{ top: 8, right: 8, bottom: 0, left: 0 }}>
          <CartesianGrid stroke="rgba(255,255,255,0.06)" vertical={false} />
          <XAxis
            dataKey="date"
            tickFormatter={(date: string) => date.slice(5)}
            tick={{ fill: "#71717a", fontSize: 11 }}
            axisLine={false}
            tickLine={false}
            minTickGap={24}
          />
          <YAxis
            width={hidden ? 8 : 56}
            tick={hidden ? false : { fill: "#71717a", fontSize: 11 }}
            tickFormatter={compactKrw}
            axisLine={false}
            tickLine={false}
          />
          <Tooltip
            content={({ active, payload }) => {
              const point = payload?.[0]?.payload as HistoryPoint | undefined;
              if (!active || !point) return null;
              return (
                <div className={tooltipStyle}>
                  <p className="mb-1 text-zinc-400">{point.date}</p>
                  <p>순자산 {hidden ? MASK : formatKrw(point.netWorth)}</p>
                  <p className="text-zinc-400">총자산 {hidden ? MASK : formatKrw(point.totalAssets)}</p>
                  {point.totalLiabilities > 0 ? <p className="text-zinc-400">부채 {hidden ? MASK : formatKrw(point.totalLiabilities)}</p> : null}
                </div>
              );
            }}
          />
          <Area type="monotone" dataKey="totalAssets" stroke="#67e8f9" strokeOpacity={0.4} fill="#67e8f9" fillOpacity={0.08} isAnimationActive={false} />
          <Line type="monotone" dataKey="netWorth" stroke="#6ee7b7" strokeWidth={2} dot={points.length <= 31} isAnimationActive={false} />
        </ComposedChart>
      </ResponsiveContainer>
    </div>
  );
}
