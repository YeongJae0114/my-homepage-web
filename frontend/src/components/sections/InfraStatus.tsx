import type { Server, Service } from "../../types/infra";
import { Container } from "../common/Container";
import { SectionTitle } from "../common/SectionTitle";
import { ServerStatusCard } from "../infra/ServerStatusCard";
import { ServiceStatusCard } from "../infra/ServiceStatusCard";
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
        <div className="mt-8 grid gap-5 lg:grid-cols-2">
          {servers.map((server) => (
            <ServerStatusCard key={server.id} server={server} />
          ))}
        </div>
        <div className="mt-8">
          <SectionTitle
            eyebrow="Services"
            title="운영 서비스 상태"
            description="현재 운영 중이거나 점검 중인 서비스의 상태, 응답 시간, 가용률을 한눈에 확인합니다."
          />
          <div className="grid gap-4 lg:grid-cols-2">
            {services.map((service) => (
              <ServiceStatusCard key={service.id} service={service} />
            ))}
          </div>
        </div>
      </Container>
    </section>
  );
}
