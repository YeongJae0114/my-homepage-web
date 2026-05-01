import type { SiteConfig } from "../types/site";

export const siteConfig: SiteConfig = {
  name: "Yeongjae.dev",
  nickname: "Yeongjae",
  title: "Java/Spring Backend Engineer",
  headline: "운영 가능한 백엔드와 신뢰할 수 있는 시스템을 설계합니다.",
  description:
    "Java, Spring, DB, 인프라 운영 경험을 기반으로 서비스가 배포된 뒤에도 오래 버티는 구조를 만드는 개발자 브랜드 페이지입니다.",
  email: "hello@example.com",
  githubUrl: "https://github.com/example",
  blogUrl: "https://blog.example.com",
  ctaLinks: [
    { label: "GitHub", href: "https://github.com/example", variant: "primary", external: true },
    { label: "Blog", href: "https://blog.example.com", variant: "secondary", external: true },
    { label: "Monitoring", href: "/monitoring", variant: "secondary" },
    { label: "Service", href: "/service", variant: "ghost" },
  ],
  contactLinks: [
    {
      label: "Email",
      href: "mailto:hello@example.com",
      description: "협업, 채용, 기술 대화 제안",
    },
    {
      label: "GitHub",
      href: "https://github.com/example",
      description: "코드, 실험, 운영 자동화 기록",
      external: true,
    },
    {
      label: "Blog",
      href: "https://blog.example.com",
      description: "트러블슈팅과 백엔드 학습 노트",
      external: true,
    },
  ],
};
