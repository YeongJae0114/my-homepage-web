import type { Server, Service } from "../../types/infra";
import { Container } from "../common/Container";
import { SectionTitle } from "../common/SectionTitle";
import { ServiceLinkCard } from "../infra/ServiceLinkCard";
import { ServerStatusCard } from "../infra/ServerStatusCard";
import { StatusSummary } from "../infra/StatusSummary";

type InfraStatusProps = {
  servers: Server[];
  services: Service[];
};

export function InfraStatus({ servers, services }: InfraStatusProps) {
  return (
    <section id="infra" className="section-shell">
      <Container>
        <SectionTitle eyebrow="Infra" title="개인 서버와 서비스 상태" description="현재는 더미 데이터지만, statusApi를 실제 모니터링 API로 교체하면 같은 UI가 실시간 상태 보드로 확장됩니다." />
        <StatusSummary servers={servers} services={services} />
        <div className="mt-8 grid gap-5 lg:grid-cols-2 xl:grid-cols-4">
          {servers.map((server) => (
            <ServerStatusCard key={server.id} server={server} />
          ))}
        </div>
        <div className="mt-8">
          <SectionTitle
            eyebrow="Services"
            title="운영 서비스 링크"
            description="상태 카드 대신 실제 접근하거나 연결할 수 있는 서비스 단위 링크를 모아둡니다."
          />
          <div className="grid gap-4 lg:grid-cols-3">
            {services.map((service) => (
              <ServiceLinkCard key={service.id} service={service} />
            ))}
          </div>
        </div>
      </Container>
    </section>
  );
}
