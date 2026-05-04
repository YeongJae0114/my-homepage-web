# Home API Specification

This document defines the mock API contract for the homepage.

The frontend keeps static `data/config` files as fallback data. When a mock or real backend is available, the homepage calls this API and maps the response into an internal `HomeViewModel` before rendering UI components.

## Frontend Integration

- Default base URL: `/api`
- Production deploys rewrite `/api` to `https://api.zerojae175-dev.shop/api`.
- Override base URL: `VITE_API_BASE_URL`
- Endpoint used by frontend: `GET /home`
- Full URL examples:
  - Same origin mock: `/api/home`
  - External mock: `http://localhost:8080/api/home`

Example `.env.local`:

```bash
VITE_API_BASE_URL=http://localhost:8080/api
```

## Request

```http
GET /api/home
Accept: application/json
```

No query parameters are required for the initial version.

## Response

```ts
type ServiceStatus = "online" | "degraded" | "offline" | "standby" | "maintenance";

type HomeApiResponse = {
  hero: {
    title: string;
    headline: string;
    description: string;
    ctaLinks: Array<{
      label: string;
      href: string;
      variant: "primary" | "secondary" | "ghost";
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

## Example Response

```json
{
  "hero": {
    "title": "Java/Spring Backend Engineer",
    "headline": "운영 가능한 백엔드와 신뢰할 수 있는 시스템을 설계합니다.",
    "description": "Java, Spring, DB, 인프라 운영 경험을 기반으로 서비스가 배포된 뒤에도 오래 버티는 구조를 만드는 개발자 브랜드 페이지입니다.",
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
      "extensionDescription": "Status API, Notion sync, GitHub activity, Local LLM query panel can attach here without changing the page composition."
    }
  },
  "overview": {
    "eyebrow": "Quick Overview",
    "title": "최근 작업과 대표 신호를 한눈에",
    "description": "홈은 대표 프로젝트, 최신 기록, 진행 중인 실험을 먼저 보여주는 기술 플랫폼 대시보드입니다.",
    "featuredProject": {
      "id": "home-lab",
      "title": "Home Lab Monitoring",
      "description": "개인 서버, reverse proxy, API 서비스를 관측하는 홈랩 모니터링 초기 구성입니다.",
      "href": "/project",
      "tags": ["Nginx", "Prometheus", "Docker"]
    },
    "latestNote": {
      "id": "spring-auth-audit",
      "title": "Spring 인증 흐름에 감사 로그를 붙이는 기준",
      "description": "로그인, 토큰 갱신, 권한 실패 이벤트를 운영 관점에서 추적하는 방법을 정리합니다.",
      "href": "/blog",
      "tags": ["Spring Security", "Audit", "Operations"]
    },
    "currentExperiment": {
      "id": "infra-monitor",
      "title": "Infra Monitoring API",
      "description": "서버 상태 데이터를 실시간 API로 연결하고 알림 정책을 붙이는 기능",
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

## Frontend Mapping Rules

The API response is not rendered directly.

- API type: `HomeApiResponse`
- Adapter: `src/adapters/homeAdapter.ts`
- Internal UI type: `HomeViewModel`
- Fallback source: `src/data/homeFallback.ts`
- Hook: `src/hooks/useHomeData.ts`

Mapping details:

- `overview.featuredProject` becomes the `Featured Project` card.
- `overview.latestNote` becomes the `Latest Note` card.
- `overview.currentExperiment` becomes the `Current Experiment` card.
- Navigation card badge tone is derived by frontend:
  - `/monitoring`: `emerald`
  - everything else: `cyan`
- Only the first 3 tags are displayed in overview cards.

## Error And Fallback Behavior

If the API request fails, returns a non-2xx status, or the mock server is not running, the homepage uses `src/data/homeFallback.ts`.

The existing UI should continue to render without requiring a backend.

## Spring Boot Migration Notes

The future backend can expose the same `GET /api/home` contract.

Recommended backend composition:

- Hero profile: site/profile table or config record
- Featured project: `projects` table filtered by `featured = true`
- Latest note: `posts` table sorted by `published_at desc`
- Current experiment: `lab_features` table filtered by `status = 'building'`
- Operation snapshot: server status table or monitoring adapter result
- Navigation cards: config table or server-side static config
