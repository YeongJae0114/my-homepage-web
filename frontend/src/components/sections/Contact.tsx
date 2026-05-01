import { siteConfig } from "../../config/site";
import { Button } from "../common/Button";
import { Card } from "../common/Card";
import { Container } from "../common/Container";
import { SectionTitle } from "../common/SectionTitle";

export function Contact() {
  return (
    <section id="contact" className="section-shell">
      <Container>
        <div className="grid gap-8 lg:grid-cols-[.95fr_1.05fr] lg:items-end">
          <SectionTitle eyebrow="Contact" title="기술 이야기를 이어갈 수 있는 연결 지점" description="협업, 채용, 백엔드 설계, 운영 자동화, 로컬 LLM 실험에 대해 편하게 연락할 수 있도록 링크를 분리해 관리합니다." />
          <div className="flex flex-wrap gap-3 lg:justify-end">
            <Button href={`mailto:${siteConfig.email}`} variant="primary">Email</Button>
            <Button href={siteConfig.githubUrl} variant="secondary" target="_blank" rel="noreferrer">GitHub</Button>
            <Button href={siteConfig.blogUrl} variant="ghost" target="_blank" rel="noreferrer">Blog</Button>
          </div>
        </div>
        <div className="mt-6 grid gap-4 md:grid-cols-3">
          {siteConfig.contactLinks.map((link) => (
            <a key={link.label} href={link.href} target={link.external ? "_blank" : undefined} rel={link.external ? "noreferrer" : undefined}>
              <Card interactive className="h-full p-5">
                <h3 className="font-semibold text-zinc-50">{link.label}</h3>
                <p className="mt-2 text-sm leading-6 text-zinc-400">{link.description}</p>
              </Card>
            </a>
          ))}
        </div>
      </Container>
    </section>
  );
}
