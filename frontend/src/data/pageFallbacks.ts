import { siteConfig } from "../config/site";
import { experiences } from "./experiences";
import { labFeatures } from "./lab";
import { mediaItems } from "./media";
import { posts } from "./posts";
import { profile } from "./profile";
import { projects } from "./projects";
import { servers } from "./servers";
import { services } from "./services";
import { skillCategories } from "./skills";
import type {
  AboutPageViewModel,
  BlogPageViewModel,
  ContactViewModel,
  MonitoringPageViewModel,
  ProjectPageViewModel,
  ServicePageViewModel,
} from "../types/pages";

export const contactFallback: ContactViewModel = {
  email: siteConfig.email,
  githubUrl: siteConfig.githubUrl,
  blogUrl: siteConfig.blogUrl,
  contactLinks: siteConfig.contactLinks,
};

export const aboutPageFallback: AboutPageViewModel = {
  profile: {
    intro: profile.intro,
    highlight: profile.highlight,
    secondaryDescription:
      "이 페이지는 이력서의 정적 복사본이 아니라, 프로젝트와 운영 데이터, 글, 실험 기능이 계속 붙을 수 있는 개인 기술 플랫폼의 시작점입니다.",
    strengths: profile.strengths,
  },
  skills: skillCategories,
  experiences,
  contact: contactFallback,
};

export const servicePageFallback: ServicePageViewModel = {
  services,
  mediaItems,
  contact: contactFallback,
};

export const monitoringPageFallback: MonitoringPageViewModel = {
  summary: {
    status: "operational",
    message: "Fallback monitoring snapshot is ready",
    lastUpdatedAt: "2026-05-01T22:42:00+09:00",
  },
  servers,
  services,
};

export const projectPageFallback: ProjectPageViewModel = {
  projects,
  labFeatures,
};

export const blogPageFallback: BlogPageViewModel = {
  posts,
  mediaItems,
};
