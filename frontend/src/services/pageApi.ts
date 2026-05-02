import {
  mapAboutApiToViewModel,
  mapMonitoringApiToViewModel,
  mapProjectApiToViewModel,
  mapServiceApiToViewModel,
} from "../adapters/pageAdapters";
import type {
  AboutPageApiResponse,
  MonitoringPageApiResponse,
  ProjectPageApiResponse,
  ServicePageApiResponse,
} from "../types/pages";
import { fetchJson } from "./apiClient";

export async function getAboutPageContent() {
  return mapAboutApiToViewModel(await fetchJson<AboutPageApiResponse>("/about"));
}

export async function getServicePageContent() {
  return mapServiceApiToViewModel(await fetchJson<ServicePageApiResponse>("/service"));
}

export async function getMonitoringPageContent() {
  return mapMonitoringApiToViewModel(await fetchJson<MonitoringPageApiResponse>("/monitoring"));
}

export async function getProjectPageContent() {
  return mapProjectApiToViewModel(await fetchJson<ProjectPageApiResponse>("/project"));
}
