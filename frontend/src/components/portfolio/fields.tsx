import type { ButtonHTMLAttributes, InputHTMLAttributes, ReactNode, SelectHTMLAttributes, TextareaHTMLAttributes } from "react";
import { cn } from "../../utils/cn";
import { Card } from "../common/Card";

const inputClassName =
  "w-full min-w-0 rounded-md border border-white/10 bg-surface-900 px-3 py-2 text-sm text-zinc-100 outline-none transition placeholder:text-zinc-600 focus:border-emerald-300/50 disabled:opacity-50";

type FieldProps = {
  label: string;
  hint?: string;
  error?: string;
  className?: string;
  children: ReactNode;
};

export function Field({ label, hint, error, className, children }: FieldProps) {
  return (
    <label className={cn("grid min-w-0 content-start gap-1.5 text-sm text-zinc-400", className)}>
      <span>{label}</span>
      {children}
      {error ? <span className="text-xs text-rose-300">{error}</span> : hint ? <span className="text-xs text-zinc-500">{hint}</span> : null}
    </label>
  );
}

export function TextInput({ className, ...props }: InputHTMLAttributes<HTMLInputElement>) {
  return <input className={cn(inputClassName, className)} {...props} />;
}

/** Text input for amounts: accepts commas, opens the numeric keypad on phones. */
export function NumberInput({ className, ...props }: InputHTMLAttributes<HTMLInputElement>) {
  return <input type="text" inputMode="decimal" autoComplete="off" className={cn(inputClassName, "font-mono tabular-nums", className)} {...props} />;
}

export function SelectInput({ className, children, ...props }: SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <select className={cn(inputClassName, className)} {...props}>
      {children}
    </select>
  );
}

export function TextArea({ className, ...props }: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return <textarea rows={2} className={cn(inputClassName, className)} {...props} />;
}

type ActionButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & { variant?: "primary" | "secondary" | "ghost" | "danger" };

const buttonVariants = {
  primary: "border-emerald-300/40 bg-emerald-300 text-surface-950 hover:bg-emerald-200",
  secondary: "border-white/10 bg-white/[0.06] text-zinc-50 hover:border-cyan-200/30 hover:bg-cyan-200/10",
  ghost: "border-transparent bg-transparent text-zinc-400 hover:bg-white/[0.06] hover:text-zinc-50",
  danger: "border-rose-300/30 bg-rose-400/10 text-rose-200 hover:bg-rose-400/20",
};

export function ActionButton({ variant = "secondary", className, type = "button", ...props }: ActionButtonProps) {
  return (
    <button
      type={type}
      className={cn(
        "inline-flex min-h-10 items-center justify-center gap-1.5 rounded-md border px-3.5 py-2 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-50",
        buttonVariants[variant],
        className,
      )}
      {...props}
    />
  );
}

export function FormMessage({ error, success }: { error?: string | null; success?: string | null }) {
  if (error) {
    return (
      <p className="rounded-md border border-rose-300/20 bg-rose-400/10 px-3 py-2 text-sm text-rose-200" role="alert">
        {error}
      </p>
    );
  }
  if (success) {
    return (
      <p className="rounded-md border border-emerald-300/20 bg-emerald-300/10 px-3 py-2 text-sm text-emerald-100" role="status">
        {success}
      </p>
    );
  }
  return null;
}

type PanelProps = {
  title: string;
  description?: string;
  actions?: ReactNode;
  children: ReactNode;
};

export function Panel({ title, description, actions, children }: PanelProps) {
  return (
    <Card className="p-4 sm:p-6">
      <div className="mb-5 flex flex-wrap items-start justify-between gap-3">
        <div className="min-w-0">
          <h2 className="text-base font-semibold text-zinc-50">{title}</h2>
          {description ? <p className="mt-1 text-sm text-zinc-400">{description}</p> : null}
        </div>
        {actions ? <div className="flex flex-wrap gap-2">{actions}</div> : null}
      </div>
      {children}
    </Card>
  );
}

export function EmptyState({ children }: { children: ReactNode }) {
  return <p className="rounded-md border border-dashed border-white/10 px-4 py-8 text-center text-sm text-zinc-500">{children}</p>;
}
