import { Footer } from "./components/layout/Footer";
import { Header } from "./components/layout/Header";
import { usePathname } from "./hooks/usePathname";
import { AboutPage } from "./pages/AboutPage";
import { BlogPage } from "./pages/BlogPage";
import { HomePage } from "./pages/HomePage";
import { MonitoringPage } from "./pages/MonitoringPage";
import { ProjectsPage } from "./pages/ProjectsPage";
import { ServicePage } from "./pages/ServicePage";

export default function App() {
  const pathname = usePathname();

  const Page = (() => {
    switch (pathname) {
      case "/":
        return HomePage;
      case "/about":
        return AboutPage;
      case "/service":
        return ServicePage;
      case "/monitoring":
      case "/infra":
      case "/status":
        return MonitoringPage;
      case "/project":
      case "/projects":
        return ProjectsPage;
      case "/blog":
        return BlogPage;
      default:
        return HomePage;
    }
  })();

  return (
    <div className="min-h-screen bg-surface-950 text-zinc-50">
      <Header />
      <main>
        <Page />
      </main>
      <Footer />
    </div>
  );
}
