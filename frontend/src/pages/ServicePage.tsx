import { Badge } from "../components/common/Badge";
import { Button } from "../components/common/Button";
import { Card } from "../components/common/Card";
import { Container } from "../components/common/Container";
import { SectionTitle } from "../components/common/SectionTitle";

const hardwareSpecs = [
  {
    label: "GPU",
    value: "NVIDIA RTX 5060 Ti",
    detail: "8GB VRAM",
    tone: "emerald",
  },
  {
    label: "CPU",
    value: "AMD Ryzen 7 7800X3D",
    detail: "High-cache desktop compute",
    tone: "cyan",
  },
  {
    label: "Memory",
    value: "32GB DDR5 RAM",
    detail: "RAG indexing and model runtime buffer",
    tone: "neutral",
  },
  {
    label: "Storage",
    value: "1TB NVMe SSD",
    detail: "Vector index, documents, model assets",
    tone: "amber",
  },
] as const;

const architectureSteps = [
  {
    title: "Wake",
    description: "인증된 사용자가 WOL API를 호출해 로컬 GPU 서버를 깨웁니다.",
  },
  {
    title: "Load",
    description: "서버가 모델 런타임과 문서 인덱스를 준비하고 상태를 online으로 전환합니다.",
  },
  {
    title: "Retrieve",
    description: "질문과 관련된 문서, 코드, 운영 기록을 벡터 검색으로 먼저 찾습니다.",
  },
  {
    title: "Generate",
    description: "검색된 출처를 컨텍스트로 붙여 로컬 LLM이 답변을 생성합니다.",
  },
] as const;

const ragLayers = [
  "개인 문서와 기술 기록",
  "코드/설정 스냅샷",
  "운영 로그와 장애 메모",
  "출처 기반 응답",
] as const;

export function ServicePage() {
  return (
    <>
      <section className="relative overflow-hidden bg-surface-950 py-14 sm:py-20 lg:py-24">
        <div className="absolute inset-x-0 top-0 h-80 bg-[radial-gradient(circle_at_50%_0%,rgba(110,231,183,0.16),transparent_62%)]" aria-hidden="true" />
        <Container className="relative">
          <div className="grid gap-10 lg:grid-cols-[1.05fr_.95fr] lg:items-center">
            <div className="min-w-0">
              <Badge tone="emerald">Local GPU LLM System</Badge>
              <h1 className="mt-5 max-w-4xl break-words text-3xl font-semibold leading-tight text-zinc-50 sm:text-5xl">
                내 로컬 GPU 서버에서 구동되는 개인 AI 워크스페이스
              </h1>
              <p className="mt-5 max-w-2xl text-base leading-8 text-zinc-400 sm:text-lg">
                외부 서비스에 모든 데이터를 맡기지 않고, 로컬 GPU 서버에서 문서 검색과 답변 생성을 처리하는 LLM 시스템입니다.
                개인 기술 기록, 코드, 운영 메모를 RAG 파이프라인으로 연결하는 것을 목표로 합니다.
              </p>
              <div className="mt-8 flex flex-wrap gap-3">
                <Button href="/llm" variant="primary">Launch Local LLM Chat</Button>
                <Button href="/monitoring" variant="secondary">View Monitoring</Button>
              </div>
            </div>

            <Card className="p-5">
              <p className="font-mono text-xs uppercase tracking-[0.16em] text-zinc-500">Runtime profile</p>
              <div className="mt-5 grid gap-3 sm:grid-cols-2">
                {hardwareSpecs.map((spec) => (
                  <div key={spec.label} className="rounded-md border border-white/10 bg-white/[0.035] p-4">
                    <Badge tone={spec.tone}>{spec.label}</Badge>
                    <p className="mt-4 break-words text-lg font-semibold text-zinc-50">{spec.value}</p>
                    <p className="mt-2 text-sm leading-6 text-zinc-400">{spec.detail}</p>
                  </div>
                ))}
              </div>
            </Card>
          </div>
        </Container>
      </section>

      <section className="section-shell bg-surface-900">
        <Container>
          <SectionTitle
            eyebrow="Architecture"
            title="WOL부터 RAG 응답까지 이어지는 로컬 파이프라인"
            description="퍼블릭 페이지에서는 시스템 구조를 설명하고, 실제 채팅 기능은 인증된 사용자만 별도 공간에서 사용할 수 있게 분리합니다."
          />
          <div className="grid gap-4 lg:grid-cols-4">
            {architectureSteps.map((step, index) => (
              <Card key={step.title} className="p-5">
                <span className="font-mono text-sm font-semibold text-emerald-200">0{index + 1}</span>
                <h3 className="mt-4 text-lg font-semibold text-zinc-50">{step.title}</h3>
                <p className="mt-3 text-sm leading-6 text-zinc-400">{step.description}</p>
              </Card>
            ))}
          </div>
        </Container>
      </section>

      <section className="section-shell">
        <Container>
          <div className="grid gap-8 lg:grid-cols-[.9fr_1.1fr] lg:items-start">
            <SectionTitle
              eyebrow="RAG"
              title="검색 증강 생성으로 답변의 근거를 남깁니다"
              description="질문을 바로 모델에 던지는 것이 아니라, 관련 자료를 먼저 검색하고 출처와 함께 답변을 구성하는 구조를 지향합니다."
            />
            <Card className="p-5">
              <div className="grid gap-3">
                {ragLayers.map((layer, index) => (
                  <div key={layer} className="flex items-center gap-3 rounded-md border border-white/10 bg-white/[0.035] px-4 py-3">
                    <span className="grid h-8 w-8 shrink-0 place-items-center rounded-md border border-cyan-200/20 bg-cyan-200/10 font-mono text-xs text-cyan-100">
                      {index + 1}
                    </span>
                    <p className="text-sm font-medium text-zinc-200">{layer}</p>
                  </div>
                ))}
              </div>
              <div className="mt-6 rounded-md border border-emerald-300/20 bg-emerald-300/[0.06] p-4">
                <p className="font-mono text-xs uppercase text-emerald-100">Private by design</p>
                <p className="mt-2 text-sm leading-6 text-zinc-300">
                  민감한 작업 기록과 실험 문서를 외부 채팅 서비스에 직접 업로드하지 않고, 로컬 서버 안에서 검색과 생성 흐름을 제어합니다.
                </p>
              </div>
            </Card>
          </div>
        </Container>
      </section>
    </>
  );
}
