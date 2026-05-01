import { skillCategories } from "../../data/skills";
import { Badge } from "../common/Badge";
import { Card } from "../common/Card";
import { Container } from "../common/Container";
import { SectionTitle } from "../common/SectionTitle";

export function Skills() {
  return (
    <section id="skills" className="section-shell bg-surface-900">
      <Container>
        <SectionTitle eyebrow="Skills" title="확장 가능한 기술 스택" description="카테고리와 기술 항목을 데이터로 관리해 새로운 영역이 추가되어도 컴포넌트 구조를 유지합니다." />
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {skillCategories.map((category) => (
            <Card key={category.id} interactive className="p-5">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <h3 className="text-lg font-semibold text-zinc-50">{category.title}</h3>
                  <p className="mt-2 text-sm leading-6 text-zinc-400">{category.description}</p>
                </div>
              </div>
              <div className="mt-5 flex flex-wrap gap-2">
                {category.items.map((item) => (
                  <Badge key={item.name} tone={item.level === "core" ? "emerald" : item.level === "working" ? "cyan" : "neutral"}>
                    {item.name}
                  </Badge>
                ))}
              </div>
            </Card>
          ))}
        </div>
      </Container>
    </section>
  );
}
