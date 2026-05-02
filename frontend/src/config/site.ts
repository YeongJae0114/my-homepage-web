import type { SiteConfig } from "../types/site";

export const siteConfig: SiteConfig = {
  name: "Yeongjae.dev",
  nickname: "Yeongjae",
  title: "Java/Spring Backend Engineer",
  headline: "운영 가능한 백엔드와 신뢰할 수 있는 시스템을 설계합니다.",
  description:
    "Java, Spring, DB, 인프라 운영 경험을 기반으로 서비스가 배포된 뒤에도 오래 버티는 구조를 만드는 개발자 브랜드 페이지입니다.",
  email: "zerojae175@gmail.com",
  githubUrl: "https://github.com/YeongJae0114",
  blogUrl: "https://velog.io/@yjl8628/posts",
  linkedinUrl: "https://www.linkedin.com/in/zerojae",
  ctaLinks: [
    { label: "GitHub", href: "https://github.com/YeongJae0114", variant: "primary", external: true },
    { label: "Blog", href: "https://velog.io/@yjl8628/posts", variant: "primary", external: true },
    { label: "Monitoring", href: "/monitoring", variant: "primary" },
    { label: "Service", href: "/service", variant: "primary" },
  ],
  contactLinks: [
    {
      label: "Email",
      href: "mailto:zerojae175@gmail.com",
      description: "협업, 채용, 기술 대화 제안",
    },
    {
      label: "GitHub",
      href: "https://github.com/YeongJae0114",
      description: "코드, 실험, 운영 자동화 기록",
      external: true,
    },
    {
      label: "Blog",
      href: "https://velog.io/@yjl8628/posts",
      description: "트러블슈팅과 백엔드 학습 노트",
      external: true,
    },
    {
      label: "LinkedIn",
      href: "https://www.linkedin.com/in/zerojae",
      description: "경력과 네트워크 프로필",
      external: true,
    },
  ],
};
