import type { ReactElement } from "react";
import { sections } from "../config/sections";
import { Footer } from "../components/layout/Footer";
import { Header } from "../components/layout/Header";
import { About } from "../components/sections/About";
import { BlogPreview } from "../components/sections/BlogPreview";
import { Contact } from "../components/sections/Contact";
import { Experience } from "../components/sections/Experience";
import { Hero } from "../components/sections/Hero";
import { InfraStatus } from "../components/sections/InfraStatus";
import { LabPreview } from "../components/sections/LabPreview";
import { MediaPreview } from "../components/sections/MediaPreview";
import { Projects } from "../components/sections/Projects";
import { Skills } from "../components/sections/Skills";

const sectionComponents: Record<string, () => ReactElement> = {
  about: About,
  skills: Skills,
  projects: Projects,
  experience: Experience,
  infra: InfraStatus,
  blog: BlogPreview,
  media: MediaPreview,
  lab: LabPreview,
  contact: Contact,
};

export function HomePage() {
  const enabledSections = sections.filter((section) => section.enabled).sort((a, b) => a.order - b.order);

  return (
    <div className="min-h-screen bg-surface-950 text-zinc-50">
      <Header />
      <main>
        <Hero />
        {enabledSections.map((section) => {
          const Component = sectionComponents[section.id];
          return Component ? <Component key={section.id} /> : null;
        })}
      </main>
      <Footer />
    </div>
  );
}
