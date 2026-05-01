import type { Experience as ExperienceItem } from "../../types/content";
import { Badge } from "../common/Badge";
import { Card } from "../common/Card";
import { Container } from "../common/Container";
import { SectionTitle } from "../common/SectionTitle";

type ExperienceProps = {
  items: ExperienceItem[];
};

export function Experience({ items }: ExperienceProps) {
  return (
    <section id="experience" className="section-shell bg-surface-900">
      <Container>
        <SectionTitle eyebrow="Experience" title="장애와 변경을 견디는 업무 방식" description="운영 경험, DB 변경, 인증, 자동화 같은 실제 백엔드 업무 흐름을 feature grid로 정리했습니다." />
        <div className="grid gap-4 lg:grid-cols-4">
          {items.map((item) => (
            <Card key={item.id} interactive className="p-5">
              <p className="font-mono text-xs uppercase text-zinc-500">{item.category} · {item.period}</p>
              <h3 className="mt-3 text-lg font-semibold text-zinc-50">{item.title}</h3>
              <p className="mt-3 text-sm leading-6 text-zinc-400">{item.description}</p>
              <div className="mt-5 flex flex-wrap gap-2">
                {item.tags.map((tag) => (
                  <Badge key={tag}>{tag}</Badge>
                ))}
              </div>
            </Card>
          ))}
        </div>
      </Container>
    </section>
  );
}
