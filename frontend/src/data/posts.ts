import type { Post } from "../types/content";

export const posts: Post[] = [
  {
    id: "spring-auth-audit",
    title: "Spring 인증 흐름에 감사 로그를 붙이는 기준",
    summary: "로그인, 토큰 갱신, 권한 실패 이벤트를 운영 관점에서 추적하는 방법을 정리합니다.",
    publishedAt: "2026-04-18",
    tags: ["Spring Security", "Audit", "Operations"],
    url: "#",
  },
  {
    id: "db-migration-checklist",
    title: "운영 DB 마이그레이션 체크리스트",
    summary: "검증 쿼리, 배포 순서, 롤백 판단 기준을 실제 운영 절차처럼 나눠봅니다.",
    publishedAt: "2026-03-29",
    tags: ["Database", "Migration", "SQL"],
    url: "#",
  },
  {
    id: "loki-log-model",
    title: "Loki로 로그를 읽기 좋게 쌓는 구조",
    summary: "서비스, 환경, 요청 단위 라벨을 어떻게 잡으면 장애 분석이 쉬워지는지 다룹니다.",
    publishedAt: "2026-02-21",
    tags: ["Loki", "Logging", "Infra"],
    url: "#",
  },
];
