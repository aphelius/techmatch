import { Navigate, Route, Routes, useLocation, useNavigate } from "react-router-dom";
import { AppShell } from "./components/AppShell";
import { LoginPage } from "./pages/LoginPage";
import { DashboardPage } from "./pages/DashboardPage";
import { ResumesPage } from "./pages/ResumesPage";
import { JobsPage } from "./pages/JobsPage";
import { MatchAnalysisPage } from "./pages/MatchAnalysisPage";
import { ReportPage } from "./pages/ReportPage";
import { EvidenceGraphPage } from "./pages/EvidenceGraphPage";
import { TimelinePage } from "./pages/TimelinePage";
import { FeedbackPage } from "./pages/FeedbackPage";
import { isAuthenticated, logout, readSession } from "./services/session";

function RequireAuth({ children }: { children: JSX.Element }) {
  const location = useLocation();

  if (!isAuthenticated()) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return children;
}

function ShellRoutes() {
  const navigate = useNavigate();
  const session = readSession();

  return (
    <AppShell
      user={session?.user}
      onLogout={() => {
        logout();
        navigate("/login", { replace: true });
      }}
    >
      <Routes>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/resumes" element={<ResumesPage />} />
        <Route path="/jobs" element={<JobsPage />} />
        <Route path="/analysis" element={<MatchAnalysisPage />} />
        <Route path="/reports" element={<ReportPage />} />
        <Route path="/evidence-graph" element={<EvidenceGraphPage />} />
        <Route path="/timeline" element={<TimelinePage />} />
        <Route path="/feedback" element={<FeedbackPage />} />
      </Routes>
    </AppShell>
  );
}

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/*"
        element={
          <RequireAuth>
            <ShellRoutes />
          </RequireAuth>
        }
      />
    </Routes>
  );
}
