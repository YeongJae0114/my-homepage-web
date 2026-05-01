import { siteConfig } from "../../config/site";
import { navigationItems } from "../../config/navigation";
import { AppLink } from "../common/AppLink";
import { Container } from "../common/Container";

export function Footer() {
  const navItems = navigationItems.filter((item) => item.enabled).sort((a, b) => a.order - b.order);

  return (
    <footer className="border-t border-white/10 bg-surface-950 py-10">
      <Container className="flex flex-col gap-6 md:flex-row md:items-center md:justify-between">
        <div>
          <p className="font-semibold text-zinc-50">{siteConfig.name}</p>
          <p className="mt-2 max-w-xl text-sm leading-6 text-zinc-400">{siteConfig.description}</p>
        </div>
        <div className="flex flex-wrap gap-2">
          {navItems.map((item) => (
            <AppLink key={item.href} href={item.href} className="rounded-md px-3 py-2 text-sm text-zinc-400 hover:bg-white/[0.06] hover:text-zinc-50">
              {item.label}
            </AppLink>
          ))}
        </div>
      </Container>
    </footer>
  );
}
