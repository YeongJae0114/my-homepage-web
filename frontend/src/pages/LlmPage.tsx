import { Container } from "../components/common/Container";
import { SectionTitle } from "../components/common/SectionTitle";
import { ModelStatusCard } from "../components/llm/ModelStatusCard";

export function LlmPage() {
  return (
    <section className="section-shell">
      <Container>
        <SectionTitle
          eyebrow="LLM"
          title="Local LLM Workspace"
          description="향후 ChatPanel, PromptInput, RetrievalResultCard, SourceList를 연결할 페이지 스텁입니다."
        />
        <ModelStatusCard />
      </Container>
    </section>
  );
}
