import { About } from "../components/sections/About";
import { Contact } from "../components/sections/Contact";
import { Experience } from "../components/sections/Experience";
import { Skills } from "../components/sections/Skills";

export function AboutPage() {
  return (
    <>
      <About />
      <Skills />
      <Experience />
      <Contact />
    </>
  );
}
