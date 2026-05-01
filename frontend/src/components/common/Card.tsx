import type { HTMLAttributes, ReactNode } from "react";
import { cn } from "../../utils/cn";

type CardProps = HTMLAttributes<HTMLDivElement> & {
  children: ReactNode;
  interactive?: boolean;
};

export function Card({ children, className, interactive = false, ...props }: CardProps) {
  return (
    <div
      className={cn(
        "rounded-lg border border-white/10 bg-white/[0.045] shadow-glow backdrop-blur",
        interactive && "transition duration-200 hover:-translate-y-1 hover:border-cyan-200/25 hover:bg-white/[0.07]",
        className,
      )}
      {...props}
    >
      {children}
    </div>
  );
}
