import type { RouteConfig } from "../types/site";

export const routeConfig: RouteConfig[] = [
  { path: "/", label: "Home", enabled: true, showInNavigation: true, description: "Landing and platform overview" },
  { path: "/about", label: "About", enabled: true, showInNavigation: true, description: "Detailed profile and skills" },
  { path: "/service", label: "LLM Service", enabled: true, showInNavigation: true, description: "Local GPU LLM system overview" },
  { path: "/monitoring", label: "Monitoring", enabled: true, showInNavigation: true, description: "Infrastructure and service status" },
  { path: "/project", label: "Project", enabled: true, showInNavigation: true, description: "Project archive" },
  { path: "/blog", label: "Blog", enabled: true, showInNavigation: true, description: "Markdown or API powered posts" },
  { path: "/projects", label: "Projects Redirect", enabled: true, showInNavigation: false, description: "Legacy project route" },
  { path: "/infra", label: "Infra Redirect", enabled: true, showInNavigation: false, description: "Legacy infra route" },
  { path: "/status", label: "Status Redirect", enabled: true, showInNavigation: false, description: "Legacy status route" },
  { path: "/lab", label: "Lab", enabled: false, showInNavigation: false, description: "Experimental features" },
  { path: "/llm", label: "LLM", enabled: true, showInNavigation: false, description: "Local LLM workspace" },
];
