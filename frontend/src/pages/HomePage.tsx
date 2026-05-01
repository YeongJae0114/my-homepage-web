import { Hero } from "../components/sections/Hero";
import { LabPreview } from "../components/sections/LabPreview";
import { HomeOverview } from "../components/sections/HomeOverview";

export function HomePage() {
  return (
    <>
      <Hero />
      <HomeOverview />
      <LabPreview />
    </>
  );
}
