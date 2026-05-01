import type { Project } from "../types/content";

export const projects: Project[] = [
  {
    id: "ops-console",
    name: "Operations Console",
    description: "운영 로그, 배포 상태, 장애 기록을 한곳에서 확인하기 위한 내부 운영 콘솔 컨셉입니다.",
    techStack: ["Spring Boot", "React", "PostgreSQL", "Docker"],
    outcomes: ["운영 이벤트 조회 흐름 설계", "서버 상태 카드 기반 UI 구성", "API 응답 구조 표준화"],
    links: [{ label: "Case Note", href: "#" }],
    featured: true,
  },
  {
    id: "auth-platform",
    name: "Auth Platform Skeleton",
    description: "권한, 세션, JWT, 감사 로그를 분리해 확장 가능한 인증 기반을 설계한 프로젝트입니다.",
    techStack: ["Java", "Spring Security", "Redis", "MariaDB"],
    outcomes: ["권한 모델 분리", "토큰 만료 정책 설계", "인증 이벤트 로깅"],
    links: [{ label: "Architecture", href: "#" }],
    featured: true,
  },
  {
    id: "db-migration",
    name: "Database Migration Notes",
    description: "운영 데이터 이관 시 검증 쿼리, 롤백 기준, 점검 체크리스트를 정리한 기술 기록입니다.",
    techStack: ["Oracle", "PostgreSQL", "SQL", "Batch"],
    outcomes: ["검증 쿼리 템플릿화", "마이그레이션 단계 문서화", "운영 리스크 체크리스트"],
    links: [{ label: "Read Log", href: "#" }],
    featured: false,
  },
  {
    id: "home-lab",
    name: "Home Lab Monitoring",
    description: "개인 서버, reverse proxy, API 서비스를 관측하는 홈랩 모니터링 초기 구성입니다.",
    techStack: ["Nginx", "Prometheus", "Loki", "Docker"],
    outcomes: ["서비스 상태 데이터 모델링", "가용률 지표 설계", "알림 연동 준비"],
    links: [{ label: "Status", href: "#infra" }],
    featured: true,
  },
];
