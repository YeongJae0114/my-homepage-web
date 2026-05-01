import { Hero } from "../components/sections/Hero";
import { LabPreview } from "../components/sections/LabPreview";
import { HomeOverview } from "../components/sections/HomeOverview";
import { useHomeData } from "../hooks/useHomeData";

export function HomePage() {
  const { data } = useHomeData();

  return (
    <>
      <Hero hero={data.hero} />
      <HomeOverview overview={data.overview} />
      <LabPreview />
    </>
  );
}
