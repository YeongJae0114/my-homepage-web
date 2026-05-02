import type { HomeOverviewViewModel } from "../../types/home";
import { AppLink } from "../common/AppLink";
import { Badge } from "../common/Badge";
import { Card } from "../common/Card";
import { Container } from "../common/Container";
import { SectionTitle } from "../common/SectionTitle";

type HomeOverviewProps = {
  overview: HomeOverviewViewModel;
};

export function HomeOverview({ overview }: HomeOverviewProps) {
  return (
    <section className="section-shell bg-surface-900">
      <Container>
        <SectionTitle
          eyebrow={overview.eyebrow}
          title={overview.title}
          description={overview.description}
        />
        <div className="grid gap-5 lg:grid-cols-3">
          {overview.items.map((item) => (
            <AppLink key={item.eyebrow} href={item.href} className="block min-w-0">
              <Card interactive className="flex h-full flex-col p-6">
                <Badge tone={item.tone}>{item.eyebrow}</Badge>
                <h3 className="mt-5 break-words text-xl font-semibold leading-snug text-zinc-50">{item.title}</h3>
                <p className="mt-3 text-sm leading-6 text-zinc-400">{item.description}</p>
                <div className="mt-auto flex flex-wrap gap-2 pt-6">
                  {item.meta.map((meta) => (
                    <Badge key={meta}>{meta}</Badge>
                  ))}
                </div>
              </Card>
            </AppLink>
          ))}
        </div>

        <div className="mt-8 grid gap-3 md:grid-cols-2 xl:grid-cols-5">
          {overview.pageCards.map((item) => (
            <AppLink key={item.href} href={item.href} className="block min-w-0">
              <Card interactive className="h-full p-4">
                <Badge tone={item.tone}>{item.label}</Badge>
                <p className="mt-3 text-sm leading-6 text-zinc-400">{item.description}</p>
              </Card>
            </AppLink>
          ))}
        </div>
      </Container>
    </section>
  );
}
