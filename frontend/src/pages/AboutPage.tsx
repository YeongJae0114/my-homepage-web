import { About } from "../components/sections/About";
import { Contact } from "../components/sections/Contact";
import { Experience } from "../components/sections/Experience";
import { Skills } from "../components/sections/Skills";
import { useAboutPageData } from "../hooks/usePageData";

export function AboutPage() {
  const { data } = useAboutPageData();

  return (
    <>
      <About profile={data.profile} />
      <Skills categories={data.skills} />
      <Experience items={data.experiences} />
      <Contact contact={data.contact} />
    </>
  );
}
