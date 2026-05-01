import type { ServiceStatus, StatusStyle } from "../types/infra";

export const statusStyles: Record<ServiceStatus, StatusStyle> = {
  online: {
    label: "정상",
    dotClassName: "bg-emerald-300 shadow-[0_0_18px_rgba(110,231,183,.55)]",
    badgeClassName: "border-emerald-300/30 bg-emerald-300/10 text-emerald-100",
    cardClassName: "border-emerald-300/20",
  },
  degraded: {
    label: "성능 저하",
    dotClassName: "bg-amber-300 shadow-[0_0_18px_rgba(252,211,77,.45)]",
    badgeClassName: "border-amber-300/30 bg-amber-300/10 text-amber-100",
    cardClassName: "border-amber-300/25",
  },
  offline: {
    label: "오프라인",
    dotClassName: "bg-rose-300 shadow-[0_0_18px_rgba(253,164,175,.45)]",
    badgeClassName: "border-rose-300/30 bg-rose-300/10 text-rose-100",
    cardClassName: "border-rose-300/25",
  },
  standby: {
    label: "대기 중",
    dotClassName: "bg-cyan-300 shadow-[0_0_18px_rgba(103,232,249,.45)]",
    badgeClassName: "border-cyan-300/30 bg-cyan-300/10 text-cyan-100",
    cardClassName: "border-cyan-300/20",
  },
  maintenance: {
    label: "점검 중",
    dotClassName: "bg-zinc-300 shadow-[0_0_18px_rgba(212,212,216,.32)]",
    badgeClassName: "border-zinc-300/25 bg-zinc-300/10 text-zinc-100",
    cardClassName: "border-zinc-300/20",
  },
};
