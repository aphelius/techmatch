export function SectionCard({
  title,
  subtitle,
  action,
  children
}: {
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
  children: React.ReactNode;
}) {
  return (
    <section className="section-card">
      <div className="section-header">
        <div>
          <h3>{title}</h3>
          {subtitle ? <p>{subtitle}</p> : null}
        </div>
        {action}
      </div>
      {children}
    </section>
  );
}

export function Badge({
  tone = "neutral",
  children
}: {
  tone?: "neutral" | "success" | "warning" | "danger" | "info";
  children: React.ReactNode;
}) {
  return <span className={`pill ${tone}`}>{children}</span>;
}

export function EmptyState({
  title,
  description
}: {
  title: string;
  description: string;
}) {
  return (
    <div className="empty-state">
      <span className="material-symbols-outlined">inbox</span>
      <h4>{title}</h4>
      <p>{description}</p>
    </div>
  );
}

export function StatCard({
  title,
  value,
  meta,
  tone = "default"
}: {
  title: string;
  value: string;
  meta: string;
  tone?: "default" | "primary";
}) {
  return (
    <div className={`stat-card ${tone === "primary" ? "primary" : ""}`}>
      <div className="stat-title">{title}</div>
      <div className="stat-value">{value}</div>
      <div className="stat-meta">{meta}</div>
    </div>
  );
}
