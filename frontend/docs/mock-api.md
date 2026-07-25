# Mock API Server Specification

이 문서는 프론트엔드와 연동할 Mock 서버가 구현해야 하는 API 규격입니다.

프론트엔드는 API 호출에 실패하면 기존 정적 fallback 데이터로 화면을 렌더링합니다. 따라서 Mock 서버는 아래 endpoint를 필요한 것부터 하나씩 구현해도 됩니다.

## Base URL

프론트엔드 기본값:

```text
/api
```

Production deploys keep `/api` as a same-origin path and Vercel rewrites it to `https://api.zerojae.cloud/api`.

외부 Mock 서버 사용 시 `frontend/.env.local`:

```bash
VITE_API_BASE_URL=http://localhost:8080/api
```

예시:

```text
GET http://localhost:8080/api/home
GET http://localhost:8080/api/about
GET http://localhost:8080/api/service
GET http://localhost:8080/api/monitoring
GET http://localhost:8080/api/monitoring/services
GET http://localhost:8080/api/project
```

## Required Headers

Request:

```http
Accept: application/json
```

Response:

```http
Content-Type: application/json
```

외부 Mock 서버가 `localhost:8080`처럼 프론트 dev server와 다른 origin에서 뜨는 경우 CORS 허용이 필요합니다.

```http
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Methods: GET, OPTIONS
Access-Control-Allow-Headers: Content-Type, Accept
```

## Endpoints

| Method | Path | Frontend Page | Description |
| --- | --- | --- | --- |
| GET | `/api/home` | `/` | 홈 Hero, Quick Overview |
| GET | `/api/about` | `/about` | 프로필, 기술 스택, 경험, 연락처 |
| GET | `/api/service` | `/service` | 서비스 목록, 미디어, 연락처 |
| GET | `/api/monitoring` | `/monitoring` | 서버 상태 |
| GET | `/api/monitoring/services` | `/monitoring` | 운영 서비스 상태 |
| GET | `/api/project` | `/project` | 프로젝트, 실험 기능 |

참고: `/blog` 페이지의 글 목록은 Mock API가 아니라 `/velog/graphql`을 통해 Velog GraphQL API를 호출합니다. 개발 환경에서는 Vite proxy가 `https://v2.velog.io/graphql`로 전달합니다. 자세한 내용은 `docs/velog-api.md`를 참고하세요.

## Common Types

```ts
type ServiceStatus = "online" | "degraded" | "offline" | "standby" | "maintenance";
type LabStatus = "planned" | "building" | "live";
type SkillLevel = "learning" | "working" | "core";
type ContentType = "article" | "note" | "video" | "external-link" | "project-log";
type CtaVariant = "primary" | "secondary" | "ghost";
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

type ServerPayload = {
  id: string;
  name: string;
  description: string;
  role: "web" | "api" | "db" | "redis" | "monitoring" | "reverse-proxy" | "lab";
  environment?: "home-lab" | "cloud" | "raspberry-pi" | "proxmox" | "docker";
  provider?: string;
  location?: string;
  status: ServiceStatus;
  uptime?: number;
  latencyMs?: number;
  lastCheckedAt: string;
  services?: string[];
  tags: string[];
  metrics?: {
    cpu: {
      usedPercent: number;
    };
    memory: {
      usedPercent: number;
      availableGb: number;
      totalGb: number;
    };
    disk: {
      usedPercent: number;
      availableGb: number;
      totalGb: number;
    };
  };
};

type ProjectPayload = {
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
};

type PostPayload = {
  id: string;
  title: string;
  summary: string;
  publishedAt: string;
  tags: string[];
  url: string;
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

type LabFeaturePayload = {
  id: string;
  name: string;
  description: string;
  status: LabStatus;
  tags: string[];
  link?: string;
};
```

## GET /api/home

홈 화면에서 사용합니다.

```ts
type HomeApiResponse = {
  hero: {
    title: string;
    headline: string;
    description: string;
    ctaLinks: Array<{
      label: string;
      href: string;
      variant: CtaVariant;
      external?: boolean;
    }>;
    highlights: Array<{
      label: string;
      value: string;
    }>;
    operations: {
      eyebrow: string;
      title: string;
      status: ServiceStatus;
      servers: Array<{
        id: string;
        name: string;
        provider: string;
        role: string;
        status: ServiceStatus;
        latencyMs: number;
      }>;
      extensionEyebrow: string;
      extensionDescription: string;
    };
  };
  overview: {
    eyebrow: string;
    title: string;
    description: string;
    featuredProject: {
      id: string;
      title: string;
      description: string;
      href: string;
      tags: string[];
    };
    latestNote: {
      id: string;
      title: string;
      description: string;
      href: string;
      tags: string[];
    };
    currentExperiment: {
      id: string;
      title: string;
      description: string;
      href: string;
      tags: string[];
    };
    navigationCards: Array<{
      label: string;
      href: string;
      description: string;
    }>;
  };
};
```

최소 예시:

```json
{
  "hero": {
    "title": "Java/Spring Backend Engineer",
    "headline": "운영 가능한 백엔드와 신뢰할 수 있는 시스템을 설계합니다.",
    "description": "Java, Spring, DB, 인프라 운영 경험을 기반으로 오래 버티는 시스템을 만듭니다.",
    "ctaLinks": [
      {
        "label": "GitHub",
        "href": "https://github.com/example",
        "variant": "primary",
        "external": true
      },
      {
        "label": "Monitoring",
        "href": "/monitoring",
        "variant": "primary"
      }
    ],
    "highlights": [
      {
        "label": "Core",
        "value": "Spring"
      },
      {
        "label": "Data",
        "value": "SQL"
      },
      {
        "label": "Ops",
        "value": "Status"
      }
    ],
    "operations": {
      "eyebrow": "platform snapshot",
      "title": "Live Operations Board",
      "status": "online",
      "servers": [
        {
          "id": "edge-01",
          "name": "edge-01",
          "provider": "Oracle Cloud",
          "role": "reverse-proxy",
          "status": "online",
          "latencyMs": 38
        }
      ],
      "extensionEyebrow": "next extension point",
      "extensionDescription": "Status API, Notion sync, GitHub activity, Local LLM query panel can attach here."
    }
  },
  "overview": {
    "eyebrow": "Quick Overview",
    "title": "최근 작업과 대표 신호를 한눈에",
    "description": "대표 프로젝트, 최신 기록, 진행 중인 실험을 먼저 보여줍니다.",
    "featuredProject": {
      "id": "home-lab",
      "title": "Home Lab Monitoring",
      "description": "개인 서버와 API 서비스를 관측하는 홈랩 모니터링 구성입니다.",
      "href": "/project",
      "tags": ["Nginx", "Prometheus", "Docker"]
    },
    "latestNote": {
      "id": "spring-auth-audit",
      "title": "Spring 인증 흐름에 감사 로그를 붙이는 기준",
      "description": "인증 이벤트를 운영 관점에서 추적하는 방법을 정리합니다.",
      "href": "/blog",
      "tags": ["Spring Security", "Audit", "Operations"]
    },
    "currentExperiment": {
      "id": "infra-monitor",
      "title": "Infra Monitoring API",
      "description": "서버 상태 데이터를 실시간 API로 연결하는 실험입니다.",
      "href": "/service",
      "tags": ["status", "prometheus", "alert"]
    },
    "navigationCards": [
      {
        "label": "About",
        "href": "/about",
        "description": "Detailed profile and skills"
      },
      {
        "label": "Monitoring",
        "href": "/monitoring",
        "description": "Infrastructure and service status"
      }
    ]
  }
}
```

## GET /api/about

```ts
type AboutApiResponse = {
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

```ts
type ServiceApiResponse = {
  services: ServicePayload[];
  mediaItems: ContentItemPayload[];
  contact: ContactPayload;
};
```

## GET /api/monitoring

```ts
type MonitoringApiResponse = {
  summary?: {
    status: string;
    message: string;
    lastUpdatedAt: string;
  };
  servers: ServerPayload[];
};
```

## GET /api/monitoring/services

```ts
type MonitoringServicesApiResponse = {
  services: ServicePayload[];
};
```

## GET /api/project

```ts
type ProjectApiResponse = {
  projects: ProjectPayload[];
  labFeatures: LabFeaturePayload[];
};
```

## Mock Data Rules

- `id`는 안정적인 문자열이어야 합니다.
- 날짜는 가능하면 ISO 형태를 사용합니다.
  - 날짜만 필요한 경우: `2026-05-02`
  - 시간 포함: `2026-05-02T10:30:00+09:00`
- `status` 값은 정의된 union 값만 사용합니다.
- 배열은 비어 있어도 되지만, 현재 UI 품질을 위해 각 endpoint별 최소 1개 이상을 권장합니다.
- 홈의 `navigationCards.href`는 현재 프론트 라우트와 맞춰주세요.
  - `/about`
  - `/service`
  - `/monitoring`
  - `/project`
  - `/blog`

## Frontend Fallback Behavior

API 호출 실패 시 화면은 기존 정적 데이터로 동작합니다.

관련 파일:

```text
src/data/homeFallback.ts
src/data/pageFallbacks.ts
src/hooks/useHomeData.ts
src/hooks/usePageData.ts
src/services/apiClient.ts
src/services/contentApi.ts
src/services/pageApi.ts
src/adapters/homeAdapter.ts
src/adapters/pageAdapters.ts
```

## Suggested Mock Server Checklist

1. `GET /api/home` 먼저 구현
2. `.env.local`에 `VITE_API_BASE_URL` 설정
3. 프론트 홈 화면에서 mock 데이터가 보이는지 확인
4. `/api/about`, `/api/service`, `/api/monitoring`, `/api/monitoring/services`, `/api/project` 순서로 추가
5. Spring Boot 전환 시 같은 response shape의 DTO를 유지
