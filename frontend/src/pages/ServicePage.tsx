import { Container } from "../components/common/Container";
import { SectionTitle } from "../components/common/SectionTitle";
import { ServiceStatusCard } from "../components/infra/ServiceStatusCard";
import { Contact } from "../components/sections/Contact";
import { MediaPreview } from "../components/sections/MediaPreview";
import { useServicePageData } from "../hooks/usePageData";

export function ServicePage() {
  const { data } = useServicePageData();

  return (
    <>
      <section className="section-shell">
        <Container>
          <SectionTitle
            eyebrow="Service"
            title="운영 중이거나 연결 예정인 서비스"
            description="개인 홈페이지가 단순 소개 페이지를 넘어 API, 모니터링, 콘텐츠, 로컬 LLM 기능을 제공하는 플랫폼으로 확장될 수 있도록 서비스 단위를 분리했습니다."
          />
          <div className="grid gap-4 md:grid-cols-2">
            {data.services.map((service) => (
              <ServiceStatusCard key={service.id} service={service} />
            ))}
          </div>
        </Container>
      </section>
      <MediaPreview items={data.mediaItems} />
      <Contact contact={data.contact} />
    </>
  );
}
