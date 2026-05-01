import type { LabFeature } from "../types/content";

export const labFeatures: LabFeature[] = [
  {
    id: "local-llm",
    name: "Local LLM Workspace",
    description: "개인 문서, 코드, 운영 로그를 검색하고 요약하는 로컬 모델 UI",
    status: "planned",
    tags: ["llm", "rag", "privacy"],
  },
  {
    id: "infra-monitor",
    name: "Infra Monitoring API",
    description: "서버 상태 데이터를 실시간 API로 연결하고 알림 정책을 붙이는 기능",
    status: "building",
    tags: ["status", "prometheus", "alert"],
  },
  {
    id: "content-hub",
    name: "Content Hub Sync",
    description: "Blog, GitHub, Notion, YouTube 콘텐츠를 하나의 스트림으로 통합",
    status: "planned",
    tags: ["content", "api", "automation"],
  },
  {
    id: "automation",
    name: "Ops Automation",
    description: "배포, 백업, 점검 루틴을 자동화하고 실행 결과를 기록하는 실험",
    status: "building",
    tags: ["jenkins", "backup", "jobs"],
  },
];
