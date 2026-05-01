import { profile } from "../../data/profile";
import { Card } from "../common/Card";
import { Container } from "../common/Container";
import { SectionTitle } from "../common/SectionTitle";

export function About() {
  return (
    <section id="about" className="section-shell">
      <Container>
        <SectionTitle eyebrow="About" title="운영까지 생각하는 백엔드 개발자" description={profile.intro} />
        <div className="grid gap-6 lg:grid-cols-[.8fr_1.2fr]">
          <div className="border-l border-emerald-300/30 pl-5">
            <p className="text-2xl font-semibold leading-snug text-zinc-50">{profile.highlight}</p>
            <p className="mt-4 text-sm leading-7 text-zinc-400">
              이 페이지는 이력서의 정적 복사본이 아니라, 프로젝트와 운영 데이터, 글, 실험 기능이 계속 붙을 수 있는 개인 기술 플랫폼의 시작점입니다.
            </p>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            {profile.strengths.map((item) => (
              <Card key={item.title} interactive className="p-5">
                <h3 className="text-lg font-semibold text-zinc-50">{item.title}</h3>
                <p className="mt-3 text-sm leading-6 text-zinc-400">{item.description}</p>
              </Card>
            ))}
          </div>
        </div>
      </Container>
    </section>
  );
}
