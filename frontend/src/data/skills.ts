import type { SkillCategory } from "../types/content";

export const skillCategories: SkillCategory[] = [
  {
    id: "backend",
    title: "Backend",
    description: "서비스 핵심 로직, API, 트랜잭션 경계를 설계하는 주력 영역",
    items: [
      { name: "Java", level: "core", tags: ["language"] },
      { name: "Spring Boot", level: "core", tags: ["framework"] },
      { name: "JPA", level: "working", tags: ["orm"] },
      { name: "MyBatis", level: "working", tags: ["sql-mapper"] },
      { name: "REST API", level: "core", tags: ["api"] },
    ],
  },
  {
    id: "database",
    title: "Database",
    description: "운영 데이터와 쿼리 성능을 함께 고려하는 데이터 계층",
    items: [
      { name: "Oracle", level: "core" },
      { name: "Altibase", level: "working" },
      { name: "PostgreSQL", level: "working" },
      { name: "MariaDB", level: "working" },
      { name: "Redis", level: "working" },
    ],
  },
  {
    id: "infra",
    title: "Infra / DevOps",
    description: "배포, 프록시, 로그, 모니터링까지 잇는 운영 기반",
    items: [
      { name: "Docker", level: "core" },
      { name: "Nginx", level: "working" },
      { name: "Jenkins", level: "working" },
      { name: "Prometheus", level: "learning" },
      { name: "Loki", level: "learning" },
    ],
  },
  {
    id: "frontend",
    title: "Frontend",
    description: "백엔드 플랫폼을 설명하고 운영 화면을 만드는 UI 기반",
    items: [
      { name: "React", level: "working" },
      { name: "Vue", level: "working" },
      { name: "TypeScript", level: "working" },
      { name: "Tailwind CSS", level: "working" },
    ],
  },
  {
    id: "ai",
    title: "AI / LLM Lab",
    description: "개인 문서, 코드, 운영 데이터를 검색하는 로컬 AI 실험 영역",
    items: [
      { name: "Local LLM", level: "learning" },
      { name: "RAG", level: "learning" },
      { name: "Code Search", level: "learning" },
      { name: "Prompt UI", level: "learning" },
    ],
  },
];
