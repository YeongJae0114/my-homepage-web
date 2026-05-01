import type { ContactLink, CtaLink } from "./site";
import type { ContentItem, Experience, LabFeature, Post, Project, SkillCategory } from "./content";
import type { Server, Service } from "./infra";

export type AboutSectionViewModel = {
  intro: string;
  highlight: string;
  secondaryDescription: string;
  strengths: Array<{
    title: string;
    description: string;
  }>;
};

export type ContactViewModel = {
  email: string;
  githubUrl: string;
  blogUrl: string;
  contactLinks: ContactLink[];
};

export type AboutPageViewModel = {
  profile: AboutSectionViewModel;
  skills: SkillCategory[];
  experiences: Experience[];
  contact: ContactViewModel;
};

export type ServicePageViewModel = {
  services: Service[];
  mediaItems: ContentItem[];
  contact: ContactViewModel;
};

export type MonitoringPageViewModel = {
  servers: Server[];
  services: Service[];
};

export type ProjectPageViewModel = {
  projects: Project[];
  labFeatures: LabFeature[];
};

export type BlogPageViewModel = {
  posts: Post[];
  mediaItems: ContentItem[];
};

export type AboutPageApiResponse = AboutPageViewModel;
export type ServicePageApiResponse = ServicePageViewModel;
export type MonitoringPageApiResponse = MonitoringPageViewModel;
export type ProjectPageApiResponse = ProjectPageViewModel;
export type BlogPageApiResponse = BlogPageViewModel;

export type SiteApiResponse = {
  name: string;
  nickname: string;
  title: string;
  headline: string;
  description: string;
  email: string;
  githubUrl: string;
  blogUrl: string;
  ctaLinks: CtaLink[];
  contactLinks: ContactLink[];
};
