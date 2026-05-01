import { useMemo, useState } from "react";
import { navigationItems } from "../../config/navigation";
import { siteConfig } from "../../config/site";
import { Container } from "../common/Container";
import { cn } from "../../utils/cn";

export function Header() {
  const [isOpen, setIsOpen] = useState(false);
  const items = useMemo(
    () => navigationItems.filter((item) => item.enabled).sort((a, b) => a.order - b.order),
    [],
  );

  return (
    <header className="sticky top-0 z-50 border-b border-white/10 bg-surface-950/[0.78] backdrop-blur-xl">
      <Container>
        <nav className="flex min-h-16 items-center justify-between" aria-label="Primary navigation">
          <a href="#top" className="flex items-center gap-3 font-semibold text-zinc-50" aria-label="Go to top">
            <span className="grid h-9 w-9 place-items-center rounded-md border border-emerald-300/25 bg-emerald-300/10 font-mono text-sm text-emerald-100">
              Y
            </span>
            <span className="hidden sm:block">{siteConfig.name}</span>
          </a>

          <div className="hidden items-center gap-1 md:flex">
            {items.map((item) => (
              <a key={item.href} href={item.href} className="rounded-md px-3 py-2 text-sm font-medium text-zinc-400 transition hover:bg-white/[0.06] hover:text-zinc-50">
                {item.label}
              </a>
            ))}
          </div>

          <button
            type="button"
            className="inline-flex h-10 w-10 items-center justify-center rounded-md border border-white/10 bg-white/[0.04] text-zinc-100 md:hidden"
            aria-expanded={isOpen}
            aria-controls="mobile-menu"
            aria-label="Toggle navigation"
            onClick={() => setIsOpen((value) => !value)}
          >
            <span className="sr-only">Open menu</span>
            <span className="flex flex-col gap-1.5" aria-hidden="true">
              <span className={cn("h-0.5 w-5 rounded bg-current transition", isOpen && "translate-y-2 rotate-45")} />
              <span className={cn("h-0.5 w-5 rounded bg-current transition", isOpen && "opacity-0")} />
              <span className={cn("h-0.5 w-5 rounded bg-current transition", isOpen && "-translate-y-2 -rotate-45")} />
            </span>
          </button>
        </nav>
        {isOpen ? (
          <div id="mobile-menu" className="grid gap-1 border-t border-white/10 py-3 md:hidden">
            {items.map((item) => (
              <a
                key={item.href}
                href={item.href}
                className="rounded-md px-3 py-3 text-sm font-medium text-zinc-300 hover:bg-white/[0.06] hover:text-zinc-50"
                onClick={() => setIsOpen(false)}
              >
                {item.label}
              </a>
            ))}
          </div>
        ) : null}
      </Container>
    </header>
  );
}
