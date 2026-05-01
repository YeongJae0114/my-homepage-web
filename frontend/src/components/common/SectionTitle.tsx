type SectionTitleProps = {
  eyebrow?: string;
  title: string;
  description: string;
};

export function SectionTitle({ eyebrow, title, description }: SectionTitleProps) {
  return (
    <div className="mb-10 max-w-3xl">
      {eyebrow ? (
        <p className="mb-3 font-mono text-xs font-semibold uppercase tracking-[0.18em] text-emerald-200">{eyebrow}</p>
      ) : null}
      <h2 className="text-3xl font-semibold text-zinc-50 sm:text-4xl">{title}</h2>
      <p className="mt-4 text-base leading-7 text-zinc-400">{description}</p>
    </div>
  );
}
