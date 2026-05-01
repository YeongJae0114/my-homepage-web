export type ServiceStatus = "online" | "degraded" | "offline" | "standby" | "maintenance";

export type ServerRole = "web" | "api" | "db" | "redis" | "monitoring" | "reverse-proxy" | "lab";

export type ServerEnvironment = "home-lab" | "cloud" | "raspberry-pi" | "proxmox" | "docker";

export type Server = {
  id: string;
  name: string;
  description: string;
  role: ServerRole;
  environment: ServerEnvironment;
  provider: string;
  location: string;
  status: ServiceStatus;
  uptime: number;
  latencyMs: number;
  lastCheckedAt: string;
  services: string[];
  tags: string[];
};

export type Service = {
  id: string;
  name: string;
  description: string;
  serverId: string;
  type: string;
  status: ServiceStatus;
  endpoint: string;
  isPublic: boolean;
  uptime: number;
  latencyMs: number;
  lastCheckedAt: string;
  tags: string[];
};

export type StatusStyle = {
  label: string;
  dotClassName: string;
  badgeClassName: string;
  cardClassName: string;
};
