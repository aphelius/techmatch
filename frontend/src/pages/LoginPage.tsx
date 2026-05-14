import { FormEvent, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { api } from "../services/api";
import { saveSession } from "../services/session";

export function LoginPage() {
  const [mode, setMode] = useState<"login" | "register">("login");
  const [username, setUsername] = useState("alice");
  const [password, setPassword] = useState("123456");
  const [nickname, setNickname] = useState("Alice");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const location = useLocation();
  const navigate = useNavigate();

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    setError("");

    try {
      const session =
        mode === "login"
          ? await api.login(username, password)
          : await api.register(username, password, nickname);
      saveSession(session);
      const from = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname || "/";
      navigate(from, { replace: true });
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : "认证失败");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="login-page">
      <div className="login-hero">
        <div className="hero-badge">B2B SaaS 招聘智能平台</div>
        <h1>TechMatch</h1>
        <p className="hero-tagline">基于证据链的 AI 招聘智能</p>
        <p className="hero-copy">
          用可信的 AI Agent 解析简历、校验结论、生成可追溯的分析结果，让技术招聘从经验判断走向更清晰、
          更可审计的决策流程。
        </p>
        <div className="hero-grid">
          <div className="hero-metric">
            <strong>可观测</strong>
            <span>完整展示 Agent 执行时间线与证据链</span>
          </div>
          <div className="hero-metric">
            <strong>可评分</strong>
            <span>支持混合匹配评分、置信度与结构化维度</span>
          </div>
          <div className="hero-metric">
            <strong>可行动</strong>
            <span>直接生成面试问题与招聘建议</span>
          </div>
        </div>
      </div>

      <div className="login-panel">
        <div className="login-card">
          <div className="toggle-row">
            <button className={`toggle-chip ${mode === "login" ? "active" : ""}`} onClick={() => setMode("login")}>
              登录
            </button>
            <button
              className={`toggle-chip ${mode === "register" ? "active" : ""}`}
              onClick={() => setMode("register")}
            >
              注册
            </button>
          </div>

          <div className="login-copy">
            <h2>{mode === "login" ? "欢迎回来" : "创建工作区用户"}</h2>
          </div>

          <form className="form-grid" onSubmit={handleSubmit}>
            <label>
              <span>用户名</span>
              <input value={username} onChange={(event) => setUsername(event.target.value)} required />
            </label>
            {mode === "register" ? (
              <label>
                <span>昵称</span>
                <input value={nickname} onChange={(event) => setNickname(event.target.value)} required />
              </label>
            ) : null}
            <label>
              <span>密码</span>
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                required
              />
            </label>

            {error ? <div className="form-error">{error}</div> : null}

            <button className="primary-button large" disabled={submitting} type="submit">
              {submitting ? "处理中..." : mode === "login" ? "登录 TechMatch" : "创建并登录"}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
