# Page API Specification

This document defines mock API contracts for pages outside the homepage.

The frontend uses this flow for every page:

`page hook -> API service -> API response type -> adapter -> page ViewModel -> UI component props`

If an API request fails, the page renders fallback data from `src/data/pageFallbacks.ts`.

## Base URL

- Default: `/api`
- Override: `VITE_API_BASE_URL`

Example:

```bash
VITE_API_BASE_URL=http://localhost:8080/api
```

## Common Types

```ts
type ServiceStatus = "online" | "degraded" | "offline" | "standby" | "maintenance";
type LabStatus = "planned" | "building" | "live";
type SkillLevel = "learning" | "working" | "core";
type ContentType = "article" | "note" | "video" | "external-link" | "project-log";
```

## GET /api/about

Used by `/about`.

```ts
type AboutPageApiResponse = {
  profile: {
    intro: string;
    highlight: string;
    secondaryDescription: string;
    strengths: Array<{
      title: string;
      description: string;
    }>;
  };
  skills: Array<{
    id: string;
    title: string;
    description: string;
    items: Array<{
      name: string;
      level: SkillLevel;
      tags?: string[];
    }>;
  }>;
  experiences: Array<{
    id: string;
    title: string;
    description: string;
    period: string;
    category: string;
    tags: string[];
  }>;
  contact: ContactPayload;
};
```

## GET /api/service

Used by `/service`.

```ts
type ServicePageApiResponse = {
  services: ServicePayload[];
  mediaItems: ContentItemPayload[];
  contact: ContactPayload;
};
```

## GET /api/monitoring

Used by `/monitoring` server status cards and summary.

```ts
type MonitoringServersApiResponse = {
  servers: ServerPayload[];
};
```

## GET /api/monitoring/services

Used by `/monitoring` service status cards.

```ts
type MonitoringServicesApiResponse = {
  services: ServicePayload[];
};
```

## GET /api/project

Used by `/project`.

```ts
type ProjectPageApiResponse = {
  projects: Array<{
    id: string;
    name: string;
    description: string;
    techStack: string[];
    outcomes: string[];
    links: Array<{
      label: string;
      href: string;
    }>;
    featured: boolean;
  }>;
  labFeatures: Array<{
    id: string;
    name: string;
    description: string;
    status: LabStatus;
    tags: string[];
    link?: string;
  }>;
};
```

## Blog Page Data

`/blog`는 현재 Mock API를 사용하지 않고 Velog GraphQL을 직접 호출합니다. 자세한 내용은 `docs/velog-api.md`를 참고하세요.

정적 fallback 구조는 아래와 같습니다.

```ts
type BlogPageApiResponse = {
  posts: Array<{
    id: string;
    title: string;
    summary: string;
    publishedAt: string;
    tags: string[];
    url: string;
  }>;
  mediaItems: ContentItemPayload[];
};
```

## Shared Payloads

```ts
type ContactPayload = {
  email: string;
  githubUrl: string;
  blogUrl: string;
  contactLinks: Array<{
    label: string;
    href: string;
    description: string;
    external?: boolean;
  }>;
};

type ServerPayload = {
  id: string;
  name: string;
  description: string;
  role: "web" | "api" | "db" | "redis" | "monitoring" | "reverse-proxy" | "lab";
  environment: "home-lab" | "cloud" | "raspberry-pi" | "proxmox" | "docker";
  provider: string;
  location: string;
  status: ServiceStatus;
  uptime: number;
  latencyMs: number;
  lastCheckedAt: string;
  services: string[];
  tags: string[];
};

type ServicePayload = {
  id: string;
  name: string;
  description: string;
  serverId: string;
  type: string;
  status: ServiceStatus;
  endpoint: string;
  isPublic: boolean;
  uptime: number;
  latencyMs: number;
  lastCheckedAt: string;
  tags: string[];
};

type ContentItemPayload = {
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
```

## Frontend Files

- Types: `src/types/pages.ts`
- Adapters: `src/adapters/pageAdapters.ts`
- API services: `src/services/pageApi.ts`
- Hooks: `src/hooks/usePageData.ts`
- Fallbacks: `src/data/pageFallbacks.ts`

## Spring Boot Migration Notes

Recommended controller shape:

```text
GET /api/home
GET /api/about
GET /api/service
GET /api/monitoring
GET /api/monitoring/services
GET /api/project
```

Each endpoint can initially aggregate records from DB tables into page-specific DTOs. The frontend adapter layer allows backend DTOs to evolve without forcing UI components to know database-oriented shapes.
