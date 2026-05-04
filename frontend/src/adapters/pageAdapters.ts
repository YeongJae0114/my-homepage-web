import type {
  AboutPageApiResponse,
  AboutPageViewModel,
  BlogPageApiResponse,
  BlogPageViewModel,
  MonitoringServersApiResponse,
  MonitoringServersViewModel,
  MonitoringServicesApiResponse,
  MonitoringServicesViewModel,
  ProjectPageApiResponse,
  ProjectPageViewModel,
  ServicePageApiResponse,
  ServicePageViewModel,
} from "../types/pages";

export function mapAboutApiToViewModel(response: AboutPageApiResponse): AboutPageViewModel {
  return {
    profile: response.profile,
    skills: response.skills,
    experiences: response.experiences,
    contact: response.contact,
  };
}

export function mapServiceApiToViewModel(response: ServicePageApiResponse): ServicePageViewModel {
  return {
    services: response.services,
    mediaItems: response.mediaItems,
    contact: response.contact,
  };
}

export function mapMonitoringServersApiToViewModel(response: MonitoringServersApiResponse): MonitoringServersViewModel {
  return {
    servers: response.servers,
  };
}

export function mapMonitoringServicesApiToViewModel(response: MonitoringServicesApiResponse): MonitoringServicesViewModel {
  return {
    services: response.services,
  };
}

export function mapProjectApiToViewModel(response: ProjectPageApiResponse): ProjectPageViewModel {
  return {
    projects: response.projects,
    labFeatures: response.labFeatures,
  };
}

export function mapBlogApiToViewModel(response: BlogPageApiResponse): BlogPageViewModel {
  return {
    posts: response.posts,
    mediaItems: response.mediaItems,
  };
}
