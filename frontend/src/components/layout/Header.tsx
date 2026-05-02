import { useMemo, useState } from "react";
import { useLocation } from "react-router-dom";
import { navigationItems } from "../../config/navigation";
import { siteConfig } from "../../config/site";
import { AppLink } from "../common/AppLink";
import { Container } from "../common/Container";
import { cn } from "../../utils/cn";

export function Header() {
  const [isOpen, setIsOpen] = useState(false);
  const { pathname } = useLocation();
  const items = useMemo(
    () => navigationItems.filter((item) => item.enabled).sort((a, b) => a.order - b.order),
    [],
  );

  return (
    <header className="sticky top-0 z-50 border-b border-white/10 bg-surface-950/[0.78] backdrop-blur-xl">
      <Container>
        <nav className="flex min-h-16 min-w-0 items-center justify-between gap-3" aria-label="Primary navigation">
          <AppLink href="/" className="flex min-w-0 items-center gap-3 font-semibold text-zinc-50" aria-label="Go to home">
            <span className="grid h-9 w-9 place-items-center rounded-md border border-emerald-300/25 bg-emerald-300/10 font-mono text-sm text-emerald-100">
              Y
            </span>
            <span className="hidden max-w-48 truncate lg:block">{siteConfig.name}</span>
          </AppLink>

          <div className="hidden items-center gap-1 md:flex">
            {items.map((item) => {
              const isActive = item.href === "/" ? pathname === "/" : pathname.startsWith(item.href);

              return (
                <AppLink
                  key={item.href}
                  href={item.href}
                  className={cn(
                    "rounded-md px-2.5 py-2 text-sm font-medium transition hover:bg-white/[0.06] hover:text-zinc-50 lg:px-3",
                    isActive ? "bg-white/[0.07] text-emerald-100" : "text-zinc-400",
                  )}
                  aria-current={isActive ? "page" : undefined}
                >
                  {item.label}
                </AppLink>
              );
            })}
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
              <AppLink
                key={item.href}
                href={item.href}
                className="rounded-md px-3 py-3 text-sm font-medium text-zinc-300 hover:bg-white/[0.06] hover:text-zinc-50"
                onClick={() => setIsOpen(false)}
              >
                {item.label}
              </AppLink>
            ))}
          </div>
        ) : null}
      </Container>
    </header>
  );
}
