import { InfraStatus } from "../components/sections/InfraStatus";
import { useMonitoringPageData } from "../hooks/usePageData";

export function MonitoringPage() {
  const { data } = useMonitoringPageData();

  return <InfraStatus servers={data.servers} services={data.services} />;
}
