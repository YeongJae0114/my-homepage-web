import type { LabFeature } from "../../types/content";
import { Badge } from "../common/Badge";
import { Card } from "../common/Card";
import { Container } from "../common/Container";
import { SectionTitle } from "../common/SectionTitle";

const labTone = {
  planned: "neutral",
  building: "amber",
  live: "emerald",
} as const;

type LabPreviewProps = {
  features: LabFeature[];
};

export function LabPreview({ features }: LabPreviewProps) {
  return (
    <section id="lab" className="section-shell bg-surface-900">
      <Container>
        <SectionTitle eyebrow="Lab" title="로컬 LLM과 운영 자동화로 확장될 실험실" description="초기에는 Coming Soon 카드로 표현하고, 이후 /lab 또는 /llm 페이지와 전용 컴포넌트를 붙일 수 있게 비워둡니다." />
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          {features.map((feature) => (
            <Card key={feature.id} interactive className="p-5">
              <Badge tone={labTone[feature.status]}>{feature.status}</Badge>
              <h3 className="mt-4 text-lg font-semibold text-zinc-50">{feature.name}</h3>
              <p className="mt-3 text-sm leading-6 text-zinc-400">{feature.description}</p>
              <div className="mt-5 flex flex-wrap gap-2">
                {feature.tags.map((tag) => (
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
