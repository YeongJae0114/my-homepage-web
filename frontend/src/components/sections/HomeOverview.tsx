import { navigationItems } from "../../config/navigation";
import { routeConfig } from "../../config/routes";
import { labFeatures } from "../../data/lab";
import { posts } from "../../data/posts";
import { projects } from "../../data/projects";
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
  const featuredProject = projects.find((project) => project.featured) ?? projects[0];
  const latestNote = [...posts].sort((a, b) => b.publishedAt.localeCompare(a.publishedAt))[0];
  const currentExperiment =
    labFeatures.find((feature) => feature.status === "building") ??
    labFeatures.find((feature) => feature.status === "live") ??
    labFeatures[0];

  const overviewItems = [
    {
      eyebrow: "Featured Project",
      title: featuredProject.name,
      description: featuredProject.description,
      href: "/project",
      tone: "emerald",
      meta: featuredProject.techStack.slice(0, 3),
    },
    {
      eyebrow: "Latest Note",
      title: latestNote.title,
      description: latestNote.summary,
      href: "/blog",
      tone: "cyan",
      meta: latestNote.tags.slice(0, 3),
    },
    {
      eyebrow: "Current Experiment",
      title: currentExperiment.name,
      description: currentExperiment.description,
      href: "/service",
      tone: "amber",
      meta: currentExperiment.tags.slice(0, 3),
    },
  ] as const;

  return (
    <section className="section-shell bg-surface-900">
      <Container>
        <SectionTitle
          eyebrow="Quick Overview"
          title="최근 작업과 대표 신호를 한눈에"
          description="홈은 전체 메뉴를 나열하기보다 대표 프로젝트, 최신 기록, 진행 중인 실험을 먼저 보여주는 기술 플랫폼 대시보드로 구성했습니다."
        />
        <div className="grid gap-5 lg:grid-cols-3">
          {overviewItems.map((item) => (
            <AppLink key={item.eyebrow} href={item.href} className="block">
              <Card interactive className="flex h-full flex-col p-6">
                <Badge tone={item.tone}>{item.eyebrow}</Badge>
                <h3 className="mt-5 text-xl font-semibold leading-snug text-zinc-50">{item.title}</h3>
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
          {pageCards.map((item) => (
            <AppLink key={item.href} href={item.href} className="block">
              <Card interactive className="h-full p-4">
                <Badge tone={item.href === "/monitoring" ? "emerald" : "cyan"}>{item.label}</Badge>
                <p className="mt-3 text-sm leading-6 text-zinc-400">{item.description}</p>
              </Card>
            </AppLink>
          ))}
        </div>
      </Container>
    </section>
  );
}
