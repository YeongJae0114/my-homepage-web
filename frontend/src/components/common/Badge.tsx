import type { ReactNode } from "react";
import { cn } from "../../utils/cn";

type BadgeProps = {
  children: ReactNode;
  tone?: "neutral" | "cyan" | "emerald" | "amber";
  className?: string;
};

const tones = {
  neutral: "border-white/10 bg-white/[0.06] text-zinc-300",
  cyan: "border-cyan-200/25 bg-cyan-200/10 text-cyan-100",
  emerald: "border-emerald-200/25 bg-emerald-200/10 text-emerald-100",
  amber: "border-amber-200/25 bg-amber-200/10 text-amber-100",
};

export function Badge({ children, tone = "neutral", className }: BadgeProps) {
  return (
    <span className={cn("inline-flex max-w-full items-center break-words rounded-md border px-2.5 py-1 text-xs font-medium", tones[tone], className)}>
      {children}
    </span>
  );
}
