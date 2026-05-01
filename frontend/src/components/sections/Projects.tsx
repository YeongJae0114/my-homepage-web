import type { Project } from "../../types/content";
import { Badge } from "../common/Badge";
import { Button } from "../common/Button";
import { Card } from "../common/Card";
import { Container } from "../common/Container";
import { SectionTitle } from "../common/SectionTitle";

type ProjectsProps = {
  projects: Project[];
};

export function Projects({ projects }: ProjectsProps) {
  return (
    <section id="projects" className="section-shell">
      <Container>
        <SectionTitle eyebrow="Projects" title="운영 관점이 담긴 대표 프로젝트" description="각 프로젝트는 기술 스택, 성과, 링크를 데이터 배열로 관리해 개수가 늘어나도 grid가 자연스럽게 확장됩니다." />
        <div className="grid gap-5 md:grid-cols-2">
          {projects.map((project) => (
            <Card key={project.id} interactive className="flex h-full flex-col p-6">
              <div className="flex items-start justify-between gap-4">
                <h3 className="text-xl font-semibold text-zinc-50">{project.name}</h3>
                {project.featured ? <Badge tone="emerald">featured</Badge> : null}
              </div>
              <p className="mt-3 text-sm leading-6 text-zinc-400">{project.description}</p>
              <div className="mt-5 flex flex-wrap gap-2">
                {project.techStack.map((tech) => (
                  <Badge key={tech} tone="cyan">{tech}</Badge>
                ))}
              </div>
              <ul className="mt-5 grid gap-2 text-sm text-zinc-300">
                {project.outcomes.map((outcome) => (
                  <li key={outcome} className="flex gap-2">
                    <span className="mt-2 h-1.5 w-1.5 shrink-0 rounded-full bg-emerald-300" aria-hidden="true" />
                    <span>{outcome}</span>
                  </li>
                ))}
              </ul>
              <div className="mt-auto flex flex-wrap gap-3 pt-6">
                {project.links.map((link) => (
                  <Button key={link.label} href={link.href} variant="ghost" className="px-0 hover:bg-transparent hover:text-emerald-100">
                    {link.label}
                  </Button>
                ))}
              </div>
            </Card>
          ))}
        </div>
      </Container>
    </section>
  );
}
