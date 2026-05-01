import type { HomeHeroViewModel } from "../../types/home";
import { Button } from "../common/Button";
import { Container } from "../common/Container";
import { StatusBadge } from "../common/StatusBadge";

type HeroProps = {
  hero: HomeHeroViewModel;
};

export function Hero({ hero }: HeroProps) {
  return (
    <section id="top" className="relative overflow-hidden bg-surface-950">
      <div className="absolute inset-0 bg-tech-grid bg-[size:44px_44px] opacity-35" aria-hidden="true" />
      <div className="absolute inset-x-0 bottom-0 h-px bg-gradient-to-r from-transparent via-emerald-200/20 to-transparent" aria-hidden="true" />
      <Container className="relative grid min-h-[calc(100vh-4rem)] items-center gap-10 py-16 lg:grid-cols-[1.05fr_.95fr] lg:py-20">
        <div>
          <p className="font-mono text-sm font-semibold uppercase tracking-[0.18em] text-emerald-200">{hero.title}</p>
          <h1 className="mt-5 max-w-4xl text-4xl font-semibold leading-tight text-zinc-50 sm:text-5xl lg:text-6xl">
            {hero.headline}
          </h1>
          <p className="mt-6 max-w-2xl text-lg leading-8 text-zinc-300">{hero.description}</p>
          <div className="mt-8 flex flex-wrap gap-3">
            {hero.ctaLinks.map((link) => (
              <Button key={link.label} href={link.href} variant={link.variant} target={link.external ? "_blank" : undefined} rel={link.external ? "noreferrer" : undefined}>
                {link.label}
              </Button>
            ))}
          </div>
          <dl className="mt-10 grid max-w-xl grid-cols-3 gap-3">
            {hero.highlights.map(({ label, value }) => (
              <div key={label} className="border-l border-white/10 pl-4">
                <dt className="text-xs uppercase text-zinc-500">{label}</dt>
                <dd className="mt-1 font-mono text-sm font-semibold text-zinc-100">{value}</dd>
              </div>
            ))}
          </dl>
        </div>

        <div className="rounded-lg border border-white/10 bg-surface-900/80 p-4 shadow-glow backdrop-blur">
          <div className="flex items-center justify-between border-b border-white/10 pb-4">
            <div>
              <p className="font-mono text-xs uppercase text-zinc-500">{hero.operations.eyebrow}</p>
              <h2 className="mt-1 text-lg font-semibold text-zinc-50">{hero.operations.title}</h2>
            </div>
            <StatusBadge status={hero.operations.status} />
          </div>
          <div className="grid gap-3 py-4">
            {hero.operations.servers.map((server) => (
              <div key={server.id} className="flex items-center justify-between rounded-md border border-white/10 bg-white/[0.035] px-4 py-3">
                <div>
                  <p className="font-mono text-sm text-zinc-100">{server.name}</p>
                  <p className="mt-1 text-xs text-zinc-500">{server.provider} · {server.role}</p>
                </div>
                <span className="text-sm font-semibold text-zinc-200">{server.latencyMs ? `${server.latencyMs}ms` : "idle"}</span>
              </div>
            ))}
          </div>
          <div className="rounded-md border border-emerald-300/20 bg-emerald-300/[0.06] p-4">
            <p className="font-mono text-xs uppercase text-emerald-100">{hero.operations.extensionEyebrow}</p>
            <p className="mt-2 text-sm leading-6 text-zinc-300">{hero.operations.extensionDescription}</p>
          </div>
        </div>
      </Container>
    </section>
  );
}
