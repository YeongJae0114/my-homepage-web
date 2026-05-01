import { InfraStatus } from "../components/sections/InfraStatus";
import { useMonitoringPageData } from "../hooks/usePageData";

export function InfraPage() {
  const { data } = useMonitoringPageData();

  return <InfraStatus servers={data.servers} services={data.services} />;
}
