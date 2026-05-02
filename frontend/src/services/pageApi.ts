import {
  mapAboutApiToViewModel,
  mapBlogApiToViewModel,
  mapMonitoringApiToViewModel,
  mapProjectApiToViewModel,
  mapServiceApiToViewModel,
} from "../adapters/pageAdapters";
import type {
  AboutPageApiResponse,
  BlogPageApiResponse,
  MonitoringPageApiResponse,
  ProjectPageApiResponse,
  ServicePageApiResponse,
} from "../types/pages";
import { blogPageFallback } from "../data/pageFallbacks";
import { fetchJson } from "./apiClient";
import { getVelogPosts } from "./velogApi";

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

export async function getBlogPageContent() {
  try {
    return mapBlogApiToViewModel(await fetchJson<BlogPageApiResponse>("/blog"));
  } catch {
    const posts = await getVelogPosts();

    return {
      posts,
      mediaItems: blogPageFallback.mediaItems,
    };
  }
}
