import type { Service } from "../../types/infra";
import { AppLink } from "../common/AppLink";
import { Badge } from "../common/Badge";
import { Card } from "../common/Card";

type ServiceLinkCardProps = {
  service: Service;
};

function isNavigableEndpoint(endpoint: string) {
  return endpoint.startsWith("http") || endpoint.startsWith("/");
}

export function ServiceLinkCard({ service }: ServiceLinkCardProps) {
  const isNavigable = isNavigableEndpoint(service.endpoint);
  const content = (
    <Card interactive={isNavigable} className="h-full p-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Badge tone={service.isPublic ? "emerald" : "neutral"}>{service.isPublic ? "public" : "private"}</Badge>
        <span className="break-all font-mono text-xs text-zinc-500">{service.endpoint}</span>
      </div>
      <h3 className="mt-4 break-words text-lg font-semibold text-zinc-50">{service.name}</h3>
      <p className="mt-3 text-sm leading-6 text-zinc-400">{service.description}</p>
      <div className="mt-5 flex flex-wrap gap-2">
        <Badge tone="cyan">{service.type}</Badge>
        {service.tags.map((tag) => (
          <Badge key={tag}>{tag}</Badge>
        ))}
      </div>
    </Card>
  );

  if (!isNavigable) {
    return content;
  }

  return (
    <AppLink
      href={service.endpoint}
      className="block min-w-0"
      target={service.endpoint.startsWith("http") ? "_blank" : undefined}
      rel={service.endpoint.startsWith("http") ? "noreferrer" : undefined}
    >
      {content}
    </AppLink>
  );
}
