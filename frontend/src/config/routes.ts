import type { RouteConfig } from "../types/site";

export const routeConfig: RouteConfig[] = [
  { path: "/", label: "Home", enabled: true, showInNavigation: false, description: "Single page landing" },
  { path: "/about", label: "About", enabled: false, showInNavigation: false, description: "Detailed profile page" },
  { path: "/projects", label: "Projects", enabled: false, showInNavigation: false, description: "Project archive" },
  { path: "/blog", label: "Blog", enabled: false, showInNavigation: false, description: "Markdown or API powered posts" },
  { path: "/infra", label: "Infra", enabled: false, showInNavigation: false, description: "Infrastructure overview" },
  { path: "/status", label: "Status", enabled: false, showInNavigation: false, description: "Public service status" },
  { path: "/lab", label: "Lab", enabled: false, showInNavigation: false, description: "Experimental features" },
  { path: "/llm", label: "LLM", enabled: false, showInNavigation: false, description: "Local LLM workspace" },
];
