import type { Server, Service } from "../../types/infra";
import type { MonitoringSummaryViewModel } from "../../types/pages";
import { Container } from "../common/Container";
import { SectionTitle } from "../common/SectionTitle";
import { ServerStatusCard } from "../infra/ServerStatusCard";
import { ServiceStatusCard } from "../infra/ServiceStatusCard";
import { StatusSummary } from "../infra/StatusSummary";

type InfraStatusProps = {
  summary?: MonitoringSummaryViewModel;
  servers: Server[];
  services: Service[];
};

export function InfraStatus({ summary, servers, services }: InfraStatusProps) {
  return (
    <section id="infra" className="section-shell">
      <Container>
        <SectionTitle eyebrow="Infra" title="개인 서버와 서비스 상태" description="홈랩 서버와 운영 서비스의 현재 상태, 리소스 사용률, 마지막 수집 시각을 한 화면에서 확인합니다." />
        <StatusSummary summary={summary} servers={servers} services={services} />
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
