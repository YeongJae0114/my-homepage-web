import {
  aboutPageFallback,
  monitoringPageFallback,
  projectPageFallback,
  servicePageFallback,
} from "../data/pageFallbacks";
import {
  getAboutPageContent,
  getMonitoringPageContent,
  getProjectPageContent,
  getServicePageContent,
} from "../services/pageApi";
import { useApiFallback } from "./useApiFallback";

export function useAboutPageData() {
  return useApiFallback(getAboutPageContent, aboutPageFallback);
}

export function useServicePageData() {
  return useApiFallback(getServicePageContent, servicePageFallback);
}

export function useMonitoringPageData() {
  return useApiFallback(getMonitoringPageContent, monitoringPageFallback);
}

export function useProjectPageData() {
  return useApiFallback(getProjectPageContent, projectPageFallback);
}
