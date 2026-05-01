import { navigationItems } from "../config/navigation";
import { routeConfig } from "../config/routes";
import { siteConfig } from "../config/site";
import { labFeatures } from "./lab";
import { posts } from "./posts";
import { projects } from "./projects";
import { servers } from "./servers";
import type { HomeViewModel } from "../types/home";

export const homeFallback: HomeViewModel = (() => {
  const featuredProject = projects.find((project) => project.featured) ?? projects[0];
  const latestNote = [...posts].sort((a, b) => b.publishedAt.localeCompare(a.publishedAt))[0];
  const currentExperiment =
    labFeatures.find((feature) => feature.status === "building") ??
    labFeatures.find((feature) => feature.status === "live") ??
    labFeatures[0];
  const primaryServer = servers[0];
  const pageCards = navigationItems
    .filter((item) => item.enabled && item.href !== "/")
    .sort((a, b) => a.order - b.order)
    .map((item) => ({
      label: item.label,
      href: item.href,
      tone: item.href === "/monitoring" ? "emerald" : "cyan",
      description: routeConfig.find((route) => route.path === item.href)?.description ?? "",
    })) satisfies HomeViewModel["overview"]["pageCards"];

  return {
    hero: {
      title: siteConfig.title,
      headline: siteConfig.headline,
      description: siteConfig.description,
      ctaLinks: siteConfig.ctaLinks,
      highlights: [
        { label: "Core", value: "Spring" },
        { label: "Data", value: "SQL" },
        { label: "Ops", value: "Status" },
      ],
      operations: {
        eyebrow: "platform snapshot",
        title: "Live Operations Board",
        status: primaryServer.status,
        servers: servers.slice(0, 3).map((server) => ({
          id: server.id,
          name: server.name,
          provider: server.provider,
          role: server.role,
          latencyMs: server.latencyMs,
        })),
        extensionEyebrow: "next extension point",
        extensionDescription:
          "Status API, Notion sync, GitHub activity, Local LLM query panel can attach here without changing the page composition.",
      },
    },
    overview: {
      eyebrow: "Quick Overview",
      title: "최근 작업과 대표 신호를 한눈에",
      description:
        "홈은 전체 메뉴를 나열하기보다 대표 프로젝트, 최신 기록, 진행 중인 실험을 먼저 보여주는 기술 플랫폼 대시보드로 구성했습니다.",
      items: [
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
      ],
      pageCards,
    },
  };
})();
