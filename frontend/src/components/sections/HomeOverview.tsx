import { navigationItems } from "../../config/navigation";
import { routeConfig } from "../../config/routes";
import { AppLink } from "../common/AppLink";
import { Badge } from "../common/Badge";
import { Card } from "../common/Card";
import { Container } from "../common/Container";
import { SectionTitle } from "../common/SectionTitle";

export function HomeOverview() {
  const pageCards = navigationItems
    .filter((item) => item.enabled && item.href !== "/")
    .sort((a, b) => a.order - b.order)
    .map((item) => ({
      ...item,
      description: routeConfig.find((route) => route.path === item.href)?.description ?? "",
    }));

  return (
    <section className="section-shell bg-surface-900">
      <Container>
        <SectionTitle
          eyebrow="Platform"
          title="개인 기술 플랫폼의 첫 화면"
          description="홈은 전체 방향을 빠르게 보여주고, 세부 정보는 각 독립 페이지에서 확장하도록 구성했습니다."
        />
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
          {pageCards.map((item) => (
            <AppLink key={item.href} href={item.href} className="block">
              <Card interactive className="h-full p-5">
                <Badge tone={item.href === "/monitoring" ? "emerald" : "cyan"}>{item.label}</Badge>
                <p className="mt-4 text-sm leading-6 text-zinc-400">{item.description}</p>
              </Card>
            </AppLink>
          ))}
        </div>
      </Container>
    </section>
  );
}
