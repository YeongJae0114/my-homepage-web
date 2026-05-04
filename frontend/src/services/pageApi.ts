import {
  mapAboutApiToViewModel,
  mapMonitoringServersApiToViewModel,
  mapMonitoringServicesApiToViewModel,
  mapProjectApiToViewModel,
  mapServiceApiToViewModel,
} from "../adapters/pageAdapters";
import type {
  AboutPageApiResponse,
  MonitoringServersApiResponse,
  MonitoringServicesApiResponse,
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

export async function getMonitoringServersContent() {
  return mapMonitoringServersApiToViewModel(await fetchJson<MonitoringServersApiResponse>("/monitoring"));
}

export async function getMonitoringServicesContent() {
  return mapMonitoringServicesApiToViewModel(await fetchJson<MonitoringServicesApiResponse>("/monitoring/services"));
}

export async function getProjectPageContent() {
  return mapProjectApiToViewModel(await fetchJson<ProjectPageApiResponse>("/project"));
}
