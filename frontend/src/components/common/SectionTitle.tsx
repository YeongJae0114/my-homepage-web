type SectionTitleProps = {
  eyebrow?: string;
  title: string;
  description: string;
};

export function SectionTitle({ eyebrow, title, description }: SectionTitleProps) {
  return (
    <div className="mb-8 max-w-3xl sm:mb-10">
      {eyebrow ? (
        <p className="mb-3 break-words font-mono text-xs font-semibold uppercase tracking-[0.16em] text-emerald-200 sm:tracking-[0.18em]">{eyebrow}</p>
      ) : null}
      <h2 className="break-words text-2xl font-semibold leading-tight text-zinc-50 sm:text-3xl lg:text-4xl">{title}</h2>
      <p className="mt-4 text-sm leading-7 text-zinc-400 sm:text-base">{description}</p>
    </div>
  );
}
