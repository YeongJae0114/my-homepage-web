import type { ContentItem } from "../../types/content";
import { Badge } from "../common/Badge";
import { Card } from "../common/Card";
import { Container } from "../common/Container";
import { SectionTitle } from "../common/SectionTitle";

type MediaPreviewProps = {
  items: ContentItem[];
};

export function MediaPreview({ items }: MediaPreviewProps) {
  return (
    <section id="media" className="section-shell">
      <Container>
        <SectionTitle eyebrow="Media" title="외부 콘텐츠까지 담는 스트림" description="article, note, video, external-link, project-log 타입을 하나의 ContentItem 구조로 다뤄 확장성을 확보합니다." />
        <div className="grid gap-4 lg:grid-cols-3">
          {items.map((item) => (
            <Card key={item.id} interactive className="p-5">
              <div className="flex items-center justify-between gap-3">
                <Badge tone={item.featured ? "emerald" : "neutral"}>{item.type}</Badge>
                <span className="font-mono text-xs text-zinc-500">{item.source}</span>
              </div>
              <h3 className="mt-4 text-lg font-semibold text-zinc-50">{item.title}</h3>
              <p className="mt-3 text-sm leading-6 text-zinc-400">{item.description}</p>
              <div className="mt-5 flex flex-wrap gap-2">
                {item.tags.map((tag) => (
                  <Badge key={tag} tone="cyan">{tag}</Badge>
                ))}
              </div>
            </Card>
          ))}
        </div>
      </Container>
    </section>
  );
}
