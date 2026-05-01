import type { Experience } from "../types/content";

export const experiences: Experience[] = [
  {
    id: "ops",
    title: "운영 장애 대응",
    description: "장애 원인 추적을 위해 로그, 배포 이력, DB 상태를 함께 확인하는 흐름을 선호합니다.",
    period: "Ongoing",
    category: "Operations",
    tags: ["logging", "incident", "observability"],
  },
  {
    id: "db",
    title: "DB 마이그레이션",
    description: "스키마 변경, 데이터 검증, 롤백 기준을 체크리스트로 관리하며 안정성을 확보합니다.",
    period: "Project based",
    category: "Database",
    tags: ["oracle", "postgresql", "migration"],
  },
  {
    id: "security",
    title: "인증/보안 흐름",
    description: "사용자 인증, 권한 검증, 감사 로그를 분리해 추적 가능한 보안 구조를 만듭니다.",
    period: "Core",
    category: "Security",
    tags: ["auth", "jwt", "audit"],
  },
  {
    id: "automation",
    title: "자동화와 모니터링",
    description: "반복 배포와 상태 확인을 자동화하고, 지표 기반으로 운영 판단을 돕는 구조를 만듭니다.",
    period: "Expanding",
    category: "Infra",
    tags: ["jenkins", "docker", "metrics"],
  },
];
