import type { NavigationItem } from "../types/site";

export const navigationItems: NavigationItem[] = [
  { label: "Home", href: "/", enabled: true, order: 10 },
  { label: "About", href: "/about", enabled: true, order: 20 },
  { label: "Service", href: "/service", enabled: true, order: 30 },
  { label: "Monitoring", href: "/monitoring", enabled: true, order: 40 },
  { label: "Project", href: "/project", enabled: true, order: 50 },
  { label: "Blog", href: "/blog", enabled: true, order: 60 },
];
