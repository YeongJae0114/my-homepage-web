import { Container } from "../components/common/Container";
import { GameScene } from "../components/game/GameScene";

export function HomePage() {
  return (
    <div className="overflow-hidden bg-surface-950">
      <section className="relative py-12 sm:py-16 lg:py-20">
        <div className="absolute inset-x-0 top-0 h-72 bg-[radial-gradient(circle_at_50%_0%,rgba(110,231,183,0.18),transparent_58%)]" aria-hidden="true" />
        <Container className="relative">
          <div className="mx-auto max-w-3xl text-center">
            <p className="font-mono text-xs font-semibold uppercase tracking-[0.16em] text-emerald-200 sm:tracking-[0.2em]">Interactive Home</p>
            <h1 className="mt-4 text-3xl font-semibold tracking-tight text-zinc-50 sm:text-5xl">Welcome to My World</h1>
            <p className="mt-5 text-base leading-8 text-zinc-400 sm:text-lg">
              백엔드, 인프라, 기술 기록을 하나의 작은 공간처럼 둘러볼 수 있는 개인 개발자 플랫폼입니다.
            </p>
          </div>

          <div className="mx-auto mt-10 max-w-5xl rounded-xl border border-white/10 bg-surface-900/70 p-3 shadow-glow backdrop-blur sm:p-4">
            <div className="aspect-[4/5] overflow-hidden rounded-lg border border-white/10 bg-surface-950 sm:aspect-video">
              <GameScene />
            </div>
          </div>
        </Container>
      </section>
    </div>
  );
}
