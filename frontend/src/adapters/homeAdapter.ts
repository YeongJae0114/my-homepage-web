import type { HomeApiResponse, HomeViewModel } from "../types/home";

export function mapHomeApiToViewModel(response: HomeApiResponse): HomeViewModel {
  return {
    hero: {
      title: response.hero.title,
      headline: response.hero.headline,
      description: response.hero.description,
      ctaLinks: response.hero.ctaLinks,
      highlights: response.hero.highlights,
      operations: {
        eyebrow: response.hero.operations.eyebrow,
        title: response.hero.operations.title,
        status: response.hero.operations.status,
        servers: response.hero.operations.servers.map((server) => ({
          id: server.id,
          name: server.name,
          provider: server.provider,
          role: server.role,
          latencyMs: server.latencyMs,
        })),
        extensionEyebrow: response.hero.operations.extensionEyebrow,
        extensionDescription: response.hero.operations.extensionDescription,
      },
    },
    overview: {
      eyebrow: response.overview.eyebrow,
      title: response.overview.title,
      description: response.overview.description,
      items: [
        {
          eyebrow: "Featured Project",
          title: response.overview.featuredProject.title,
          description: response.overview.featuredProject.description,
          href: response.overview.featuredProject.href,
          tone: "emerald",
          meta: response.overview.featuredProject.tags.slice(0, 3),
        },
        {
          eyebrow: "Latest Note",
          title: response.overview.latestNote.title,
          description: response.overview.latestNote.description,
          href: response.overview.latestNote.href,
          tone: "cyan",
          meta: response.overview.latestNote.tags.slice(0, 3),
        },
        {
          eyebrow: "Current Experiment",
          title: response.overview.currentExperiment.title,
          description: response.overview.currentExperiment.description,
          href: response.overview.currentExperiment.href,
          tone: "amber",
          meta: response.overview.currentExperiment.tags.slice(0, 3),
        },
      ],
      pageCards: response.overview.navigationCards.map((card) => ({
        ...card,
        tone: card.href === "/monitoring" ? "emerald" : "cyan",
      })),
    },
  };
}
