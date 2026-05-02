import type { AnchorHTMLAttributes, ReactNode } from "react";
import { cn } from "../../utils/cn";
import { AppLink } from "./AppLink";

type ButtonVariant = "primary" | "secondary" | "ghost";

type ButtonProps = AnchorHTMLAttributes<HTMLAnchorElement> & {
  children: ReactNode;
  variant?: ButtonVariant;
};

const buttonVariants: Record<ButtonVariant, string> = {
  primary:
    "border-emerald-300/40 bg-emerald-300 text-surface-950 hover:bg-emerald-200 hover:shadow-[0_0_28px_rgba(110,231,183,.24)]",
  secondary:
    "border-white/10 bg-white/[0.06] text-zinc-50 hover:border-cyan-200/30 hover:bg-cyan-200/10 hover:text-cyan-50",
  ghost: "border-transparent bg-transparent text-zinc-300 hover:bg-white/[0.06] hover:text-zinc-50",
};

export function Button({ children, variant = "secondary", className, ...props }: ButtonProps) {
  return (
    <AppLink
      className={cn(
        "inline-flex min-h-11 max-w-full items-center justify-center rounded-md border px-4 py-2 text-center text-sm font-semibold transition duration-200",
        "focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-cyan-200",
        buttonVariants[variant],
        className,
      )}
      {...props}
    >
      {children}
    </AppLink>
  );
}
