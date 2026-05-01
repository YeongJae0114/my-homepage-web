import { LabPreview } from "../components/sections/LabPreview";
import { projectPageFallback } from "../data/pageFallbacks";

export function LabPage() {
  return <LabPreview features={projectPageFallback.labFeatures} />;
}
