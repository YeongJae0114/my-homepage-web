import { Projects } from "../components/sections/Projects";
import { LabPreview } from "../components/sections/LabPreview";
import { useProjectPageData } from "../hooks/usePageData";

export function ProjectsPage() {
  const { data } = useProjectPageData();

  return (
    <>
      <Projects projects={data.projects} />
      <LabPreview features={data.labFeatures} />
    </>
  );
}
