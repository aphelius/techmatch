import { useEffect, useState } from "react";
import { SectionCard, StatCard, Badge, EmptyState } from "../components/Ui";
import { api } from "../services/api";
import type { InfraStatus, ResumeListItem } from "../types/api";
import { formatDate } from "../utils/format";

export function DashboardPage() {
  const [resumes, setResumes] = useState<ResumeListItem[]>([]);
  const [infra, setInfra] = useState<InfraStatus | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let mounted = true;
    Promise.all([api.listResumes(), api.infra()])
      .then(([resumeList, infraStatus]) => {
        if (!mounted) return;
        setResumes(resumeList);
        setInfra(infraStatus);
      })
      .catch((exception) => {
        if (!mounted) return;
        setError(exception instanceof Error ? exception.message : "仪表盘加载失败");
      })
      .finally(() => {
        if (mounted) setLoading(false);
      });
    return () => {
      mounted = false;
    };
  }, []);

  const completed = resumes.filter((item) => item.status === "COMPLETED").length;
  const avgChunkHealth = resumes.length ? `${Math.round((completed / resumes.length) * 100)}%` : "0%";

  return (
    <div className="stack-lg">
      <div className="stat-grid">
        <StatCard title="已上传简历" value={String(resumes.length)} meta="实时读取 /api/resumes" />
        <StatCard title="解析成功" value={String(completed)} meta="已完成的简历解析流程" />
        <StatCard title="平均就绪度" value={avgChunkHealth} meta="基于已完成的简历记录" tone="primary" />
        <StatCard
          title="基础设施"
          value={infra ? `${Object.values(infra).filter((value) => value.available).length}/4` : "--"}
          meta="数据库、Redis、RabbitMQ、MinIO"
        />
      </div>

      <div className="content-grid two-one">
        <SectionCard
          title="最近简历"
          subtitle="最新上传并完成解析的候选人文档"
          action={loading ? <Badge tone="info">加载中</Badge> : null}
        >
          {error ? (
            <div className="form-error">{error}</div>
          ) : resumes.length === 0 ? (
            <EmptyState title="还没有简历" description="先上传一份候选人简历，开始测试完整的解析流程。" />
          ) : (
            <div className="table-list">
              {resumes.slice(0, 6).map((resume) => (
                <div className="table-row" key={resume.resumeId}>
                  <div>
                    <div className="table-title">{resume.fileName}</div>
                    <div className="table-meta">
                      {resume.fileType.toUpperCase()} · {formatDate(resume.createTime)}
                    </div>
                  </div>
                  <Badge tone={resume.status === "COMPLETED" ? "success" : "warning"}>{resume.status}</Badge>
                </div>
              ))}
            </div>
          )}
        </SectionCard>

        <SectionCard title="系统状态" subtitle="后端依赖的实时健康信号">
          {infra ? (
            <div className="stack-md">
              {Object.entries(infra).map(([name, value]) => (
                <div className="status-row" key={name}>
                  <div>
                    <div className="table-title">{name}</div>
                    <div className="table-meta">{value.detail}</div>
                  </div>
                  <Badge tone={value.available ? "success" : "danger"}>{value.available ? "可用" : "异常"}</Badge>
                </div>
              ))}
            </div>
          ) : (
            <EmptyState title="状态暂不可用" description="后端返回健康检查结果后，这里会显示实时状态。" />
          )}
        </SectionCard>
      </div>

      <div className="content-grid equal">
        <SectionCard title="当前平台能力" subtitle="这个前端现在已经可以展示的内容">
          <ul className="feature-list">
            <li>对接 Spring Boot 后端的 JWT 登录认证</li>
            <li>简历上传、列表、详情查看和 chunk 检索的真实联调</li>
            <li>基于 pgvector 的语义相似度搜索</li>
            <li>后续 AI 分析模块的高保真前端演示页</li>
          </ul>
        </SectionCard>

        <SectionCard title="推荐演示路径" subtitle="适合本地展示的一条顺畅流程">
          <ol className="feature-list ordered">
            <li>先注册或直接登录</li>
            <li>上传一份 PDF 或 DOCX 简历</li>
            <li>查看结构化解析结果与原始文本详情</li>
            <li>输入技能关键词，测试 chunk 相似度检索</li>
            <li>打开后续 mock 页面，展示完整目标产品体验</li>
          </ol>
        </SectionCard>
      </div>
    </div>
  );
}
