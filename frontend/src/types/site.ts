export type CtaVariant = "primary" | "secondary" | "ghost";

export type CtaLink = {
  label: string;
  href: string;
  variant: CtaVariant;
  external?: boolean;
};

export type ContactLink = {
  label: string;
  href: string;
  description: string;
  external?: boolean;
};

export type SiteConfig = {
  name: string;
  nickname: string;
  title: string;
  headline: string;
  description: string;
  email: string;
  githubUrl: string;
  blogUrl: string;
  linkedinUrl: string;
  ctaLinks: CtaLink[];
  contactLinks: ContactLink[];
};

export type NavigationItem = {
  label: string;
  href: string;
  enabled: boolean;
  order: number;
};

export type SectionConfig = {
  id: string;
  title: string;
  description: string;
  enabled: boolean;
  order: number;
};

export type RouteConfig = {
  path: string;
  label: string;
  enabled: boolean;
  showInNavigation: boolean;
  description: string;
};
