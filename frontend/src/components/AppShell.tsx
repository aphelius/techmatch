import { NavLink, useLocation } from "react-router-dom";
import type { AuthUser } from "../types/api";
import { titleFromPath } from "../utils/format";

const navItems = [
  ["dashboard", "仪表盘", "/"],
  ["description", "简历", "/resumes"],
  ["work", "岗位 JD", "/jobs"],
  ["analytics", "匹配分析", "/analysis"],
  ["assessment", "报告", "/reports"],
  ["account_tree", "证据图谱", "/evidence-graph"],
  ["timeline", "Agent 时间线", "/timeline"],
  ["rate_review", "反馈", "/feedback"]
] as const;

export function AppShell({
  user,
  onLogout,
  children
}: {
  user?: AuthUser;
  onLogout: () => void;
  children: React.ReactNode;
}) {
  const location = useLocation();

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand-block">
          <div className="brand-mark">TM</div>
          <div>
            <div className="brand-title">TechMatch</div>
            <div className="brand-subtitle">基于证据链的 AI 招聘智能</div>
          </div>
        </div>

        <nav className="nav-list">
          {navItems.map(([icon, label, to]) => (
            <NavLink
              key={to}
              to={to}
              end={to === "/"}
              className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`}
            >
              <span className="material-symbols-outlined">{icon}</span>
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="user-card">
            <div className="user-avatar">{(user?.nickname || user?.username || "T").slice(0, 1)}</div>
            <div>
              <div className="user-name">{user?.nickname || "招聘方用户"}</div>
              <div className="user-role">{user?.username || "已登录用户"}</div>
            </div>
          </div>
          <button className="ghost-button full-width" onClick={onLogout}>
            退出登录
          </button>
        </div>
      </aside>

      <div className="shell-main">
        <header className="topbar">
          <div>
            <h1 className="page-title">{titleFromPath(location.pathname)}</h1>
            <p className="page-subtitle">用可观测、可解释的 AI 能力辅助技术招聘决策。</p>
          </div>
          <div className="topbar-actions">
            <span className="pill subtle">后端联调中</span>
            <button className="icon-button" type="button">
              <span className="material-symbols-outlined">notifications</span>
            </button>
            <button className="icon-button" type="button">
              <span className="material-symbols-outlined">help</span>
            </button>
          </div>
        </header>

        <main className="page-container">{children}</main>
      </div>
    </div>
  );
}
