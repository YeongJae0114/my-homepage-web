import {
  aboutPageFallback,
  monitoringPageFallback,
  projectPageFallback,
  servicePageFallback,
} from "../data/pageFallbacks";
import {
  getAboutPageContent,
  getMonitoringServersContent,
  getMonitoringServicesContent,
  getProjectPageContent,
  getServicePageContent,
} from "../services/pageApi";
import { useApiFallback } from "./useApiFallback";

const monitoringServersFallback = { servers: monitoringPageFallback.servers };
const monitoringServicesFallback = { services: monitoringPageFallback.services };

export function useAboutPageData() {
  return useApiFallback(getAboutPageContent, aboutPageFallback);
}

export function useServicePageData() {
  return useApiFallback(getServicePageContent, servicePageFallback);
}

export function useMonitoringPageData() {
  const serversState = useApiFallback(getMonitoringServersContent, monitoringServersFallback);
  const servicesState = useApiFallback(getMonitoringServicesContent, monitoringServicesFallback);
  const errors = [serversState.error, servicesState.error].filter(Boolean);

  return {
    data: {
      servers: serversState.data.servers,
      services: servicesState.data.services,
    },
    isLoading: serversState.isLoading || servicesState.isLoading,
    isFallback: serversState.isFallback || servicesState.isFallback,
    error: errors.length > 0 ? errors.join("\n") : null,
  };
}

export function useProjectPageData() {
  return useApiFallback(getProjectPageContent, projectPageFallback);
}
