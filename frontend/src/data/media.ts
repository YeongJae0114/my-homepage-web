import type { ContentItem } from "../types/content";

export const mediaItems: ContentItem[] = [
  {
    id: "ops-video-note",
    title: "홈랩 모니터링 구축 로그",
    description: "서버 상태, 가용률, 응답 시간 데이터를 개인 홈페이지와 연결하는 과정을 기록할 예정입니다.",
    type: "video",
    url: "#",
    publishedAt: "2026-05-01",
    source: "YouTube Draft",
    tags: ["home-lab", "monitoring"],
    featured: true,
  },
  {
    id: "notion-architecture",
    title: "개인 기술 플랫폼 아키텍처 보드",
    description: "Notion 또는 FigJam으로 관리할 수 있는 백엔드 플랫폼 확장 로드맵입니다.",
    type: "external-link",
    url: "#",
    publishedAt: "2026-04-12",
    source: "Notion",
    tags: ["architecture", "roadmap"],
    featured: false,
  },
  {
    id: "project-log-auth",
    title: "인증 플랫폼 설계 로그",
    description: "권한 모델과 토큰 정책을 분리하면서 남긴 프로젝트 로그입니다.",
    type: "project-log",
    url: "#",
    publishedAt: "2026-03-08",
    source: "GitHub",
    tags: ["auth", "spring"],
    featured: true,
  },
];
