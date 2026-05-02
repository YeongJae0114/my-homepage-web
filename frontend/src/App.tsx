import { Navigate, Route, Routes } from "react-router-dom";
import { Footer } from "./components/layout/Footer";
import { Header } from "./components/layout/Header";
import { AboutPage } from "./pages/AboutPage";
import { BlogPage } from "./pages/BlogPage";
import { HomePage } from "./pages/HomePage";
import { MonitoringPage } from "./pages/MonitoringPage";
import { ProjectsPage } from "./pages/ProjectsPage";
import { ServicePage } from "./pages/ServicePage";

function AppShell() {
  return (
    <div className="min-h-screen bg-surface-950 text-zinc-50">
      <Header />
      <main>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/about" element={<AboutPage />} />
          <Route path="/service" element={<ServicePage />} />
          <Route path="/monitoring" element={<MonitoringPage />} />
          <Route path="/infra" element={<Navigate to="/monitoring" replace />} />
          <Route path="/status" element={<Navigate to="/monitoring" replace />} />
          <Route path="/project" element={<ProjectsPage />} />
          <Route path="/projects" element={<Navigate to="/project" replace />} />
          <Route path="/blog" element={<BlogPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}

export default function App() {
  return <AppShell />;
}
