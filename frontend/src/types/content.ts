export type ContentType = "article" | "note" | "video" | "external-link" | "project-log";

export type SkillLevel = "learning" | "working" | "core";

export type SkillItem = {
  name: string;
  level: SkillLevel;
  tags?: string[];
};

export type SkillCategory = {
  id: string;
  title: string;
  description: string;
  items: SkillItem[];
};

export type ProjectLink = {
  label: string;
  href: string;
};

export type Project = {
  id: string;
  name: string;
  description: string;
  techStack: string[];
  outcomes: string[];
  links: ProjectLink[];
  featured: boolean;
};

export type Experience = {
  id: string;
  title: string;
  description: string;
  period: string;
  category: string;
  tags: string[];
};

export type Post = {
  id: string;
  title: string;
  summary: string;
  publishedAt: string;
  tags: string[];
  url: string;
};

export type ContentItem = {
  id: string;
  title: string;
  description: string;
  type: ContentType;
  url: string;
  publishedAt: string;
  source: string;
  tags: string[];
  featured: boolean;
};

export type LabStatus = "planned" | "building" | "live";

export type LabFeature = {
  id: string;
  name: string;
  description: string;
  status: LabStatus;
  tags: string[];
  link?: string;
};
