import { siteConfig } from "../../config/site";
import { AppLink } from "../common/AppLink";
import { Container } from "../common/Container";

export function Footer() {
  const year = new Date().getFullYear();

  return (
    <footer className="border-t border-white/10 bg-surface-950 py-10">
      <Container>
        <div className="grid gap-8 md:grid-cols-[minmax(0,1fr)_auto] md:items-start">
          <div className="min-w-0">
            <p className="font-semibold text-zinc-50">{siteConfig.name}</p>
            <p className="mt-2 max-w-xl text-sm leading-6 text-zinc-400">{siteConfig.description}</p>
          </div>

          <div className="flex flex-wrap gap-x-5 gap-y-2 md:justify-end">
            {siteConfig.contactLinks.map((link) => (
              <AppLink
                key={link.href}
                href={link.href}
                className="text-sm text-zinc-400 underline-offset-4 transition hover:text-emerald-100 hover:underline"
                target={link.external ? "_blank" : undefined}
                rel={link.external ? "noreferrer" : undefined}
              >
                {link.label}
              </AppLink>
            ))}
          </div>
        </div>

        <div className="mt-8 flex flex-col gap-2 border-t border-white/10 pt-5 text-xs text-zinc-500 sm:flex-row sm:items-center sm:justify-between">
          <p>© {year} {siteConfig.name}. All rights reserved.</p>
          <p>Built with React, TypeScript, and Tailwind CSS.</p>
        </div>
      </Container>
    </footer>
  );
}
