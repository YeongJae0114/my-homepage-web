import { InfraStatus } from "../components/sections/InfraStatus";
import { useMonitoringPageData } from "../hooks/usePageData";

export function StatusPage() {
  const { data } = useMonitoringPageData();

  return <InfraStatus summary={data.summary} servers={data.servers} services={data.services} />;
}
