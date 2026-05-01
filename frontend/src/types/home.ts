import type { CtaLink } from "./site";
import type { ServiceStatus } from "./infra";

export type BadgeTone = "neutral" | "cyan" | "emerald" | "amber";

export type HomeHeroServerViewModel = {
  id: string;
  name: string;
  provider: string;
  role: string;
  latencyMs: number;
};

export type HomeHeroViewModel = {
  title: string;
  headline: string;
  description: string;
  ctaLinks: CtaLink[];
  highlights: Array<{
    label: string;
    value: string;
  }>;
  operations: {
    eyebrow: string;
    title: string;
    status: ServiceStatus;
    servers: HomeHeroServerViewModel[];
    extensionEyebrow: string;
    extensionDescription: string;
  };
};

export type HomeOverviewItemViewModel = {
  eyebrow: string;
  title: string;
  description: string;
  href: string;
  tone: BadgeTone;
  meta: string[];
};

export type HomePageCardViewModel = {
  label: string;
  href: string;
  description: string;
  tone: BadgeTone;
};

export type HomeOverviewViewModel = {
  eyebrow: string;
  title: string;
  description: string;
  items: HomeOverviewItemViewModel[];
  pageCards: HomePageCardViewModel[];
};

export type HomeViewModel = {
  hero: HomeHeroViewModel;
  overview: HomeOverviewViewModel;
};

export type HomeApiCtaLink = {
  label: string;
  href: string;
  variant: CtaLink["variant"];
  external?: boolean;
};

export type HomeApiServerSnapshot = {
  id: string;
  name: string;
  provider: string;
  role: string;
  status: ServiceStatus;
  latencyMs: number;
};

export type HomeApiOverviewContent = {
  id: string;
  title: string;
  description: string;
  href: string;
  tags: string[];
};

export type HomeApiNavigationCard = {
  label: string;
  href: string;
  description: string;
};

export type HomeApiResponse = {
  hero: {
    title: string;
    headline: string;
    description: string;
    ctaLinks: HomeApiCtaLink[];
    highlights: Array<{
      label: string;
      value: string;
    }>;
    operations: {
      eyebrow: string;
      title: string;
      status: ServiceStatus;
      servers: HomeApiServerSnapshot[];
      extensionEyebrow: string;
      extensionDescription: string;
    };
  };
  overview: {
    eyebrow: string;
    title: string;
    description: string;
    featuredProject: HomeApiOverviewContent;
    latestNote: HomeApiOverviewContent;
    currentExperiment: HomeApiOverviewContent;
    navigationCards: HomeApiNavigationCard[];
  };
};
