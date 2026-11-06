import { lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { OwnerProvider } from "./components/auth/OwnerProvider";
import { RequireOwner } from "./components/auth/RequireOwner";
import { Footer } from "./components/layout/Footer";
import { Header } from "./components/layout/Header";
import { useOwnerShortcut } from "./hooks/useOwnerShortcut";
import { AboutPage } from "./pages/AboutPage";
import { BlogPage } from "./pages/BlogPage";
import { HomePage } from "./pages/HomePage";
import { LocalLLMChatPage } from "./pages/LocalLLMChatPage";
import { MonitoringPage } from "./pages/MonitoringPage";
import { OwnerLoginPage } from "./pages/OwnerLoginPage";
import { ProjectsPage } from "./pages/ProjectsPage";
import { ServicePage } from "./pages/ServicePage";

// Separate chunk: public visitors never download the owner dashboard code.
const PortfolioPage = lazy(() => import("./pages/PortfolioPage").then((module) => ({ default: module.PortfolioPage })));

function AppShell() {
  useOwnerShortcut();

  return (
    <div className="min-h-screen bg-surface-950 text-zinc-50">
      <Header />
      <main>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/about" element={<AboutPage />} />
          <Route path="/service" element={<ServicePage />} />
          <Route path="/llm" element={<LocalLLMChatPage />} />
          <Route path="/monitoring" element={<MonitoringPage />} />
          <Route path="/infra" element={<Navigate to="/monitoring" replace />} />
          <Route path="/status" element={<Navigate to="/monitoring" replace />} />
          <Route path="/project" element={<ProjectsPage />} />
          <Route path="/projects" element={<Navigate to="/project" replace />} />
          <Route path="/blog" element={<BlogPage />} />
          <Route path="/owner" element={<OwnerLoginPage />} />
          <Route
            path="/portfolio"
            element={
              <RequireOwner>
                <Suspense fallback={null}>
                  <PortfolioPage />
                </Suspense>
              </RequireOwner>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}

export default function App() {
  return (
    <OwnerProvider>
      <AppShell />
    </OwnerProvider>
  );
}
