import type { SectionConfig } from "../types/site";

export const sections: SectionConfig[] = [
  {
    id: "about",
    title: "Backend Profile",
    description: "운영 환경을 고려하는 Java/Spring 백엔드 개발자 소개",
    enabled: true,
    order: 10,
  },
  {
    id: "skills",
    title: "Engineering Stack",
    description: "백엔드, 데이터베이스, 인프라, 프론트엔드, AI 실험까지 확장 가능한 기술 구조",
    enabled: true,
    order: 20,
  },
  {
    id: "projects",
    title: "Selected Projects",
    description: "문제 해결 방식과 운영 관점을 보여주는 대표 프로젝트",
    enabled: true,
    order: 30,
  },
  {
    id: "experience",
    title: "Work Style",
    description: "운영, 장애 대응, 인증, 자동화 경험을 중심으로 정리한 업무 방식",
    enabled: true,
    order: 40,
  },
  {
    id: "infra",
    title: "Infra Status",
    description: "개인 서버와 서비스 상태를 모니터링 대시보드처럼 확인하는 기반",
    enabled: true,
    order: 50,
  },
  {
    id: "blog",
    title: "Blog & Notes",
    description: "트러블슈팅, 학습 기록, 운영 회고를 위한 콘텐츠 프리뷰",
    enabled: true,
    order: 60,
  },
  {
    id: "media",
    title: "Media Stream",
    description: "영상, 외부 링크, 프로젝트 로그까지 통합할 수 있는 콘텐츠 영역",
    enabled: true,
    order: 70,
  },
  {
    id: "lab",
    title: "Future Lab",
    description: "로컬 LLM, RAG, 자동화, 홈랩 기능으로 확장될 실험실",
    enabled: true,
    order: 80,
  },
  {
    id: "contact",
    title: "Contact",
    description: "기술 대화와 협업을 시작할 수 있는 연결 지점",
    enabled: true,
    order: 90,
  },
];
