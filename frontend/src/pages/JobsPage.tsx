import { useEffect, useMemo, useState } from "react";
import { Badge, EmptyState, SectionCard } from "../components/Ui";
import { api } from "../services/api";
import type { JobCreateResult, JobDetail, JobListItem } from "../types/api";

const defaultRawText = [
  "我们正在招聘一名高级后端工程师，负责搭建技术招聘场景下的核心决策平台。",
  "该岗位要求候选人具备扎实的 Java、Spring Boot、Redis、PostgreSQL 实战经验，以及对分布式系统的理解。",
  "你将与 AI 工程师协作，持续优化证据检索、结构化分析和匹配评分链路。",
  "具备 Docker、Kubernetes、可观测性建设经验者优先。"
].join("\n\n");

export function JobsPage() {
  const [jobs, setJobs] = useState<JobListItem[]>([]);
  const [selectedJobId, setSelectedJobId] = useState<number | null>(null);
  const [jobDetail, setJobDetail] = useState<JobDetail | null>(null);
  const [title, setTitle] = useState("高级后端工程师");
  const [company, setCompany] = useState("Nebula Systems");
  const [location, setLocation] = useState("上海 / 混合办公");
  const [rawText, setRawText] = useState(defaultRawText);
  const [loadingList, setLoadingList] = useState(true);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [lastCreated, setLastCreated] = useState<JobCreateResult | null>(null);
  const [error, setError] = useState("");

  async function loadJobs(preferredJobId?: number) {
    setLoadingList(true);
    try {
      const list = await api.listJobs();
      setJobs(list);
      const nextJobId = preferredJobId ?? selectedJobId ?? list[0]?.jobId ?? null;
      setSelectedJobId(nextJobId);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : "加载 JD 列表失败");
    } finally {
      setLoadingList(false);
    }
  }

  useEffect(() => {
    void loadJobs();
  }, []);

  useEffect(() => {
    if (!selectedJobId) {
      setJobDetail(null);
      return;
    }

    setLoadingDetail(true);
    api
      .getJob(selectedJobId)
      .then(setJobDetail)
      .catch((currentError) => {
        setError(currentError instanceof Error ? currentError.message : "加载 JD 详情失败");
      })
      .finally(() => {
        setLoadingDetail(false);
      });
  }, [selectedJobId]);

  async function handleCreate() {
    setSubmitting(true);
    setError("");
    try {
      const created = await api.createJob({
        title,
        company,
        location,
        rawText
      });
      setLastCreated(created);
      await loadJobs(created.jobId);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : "创建 JD 失败");
    } finally {
      setSubmitting(false);
    }
  }

  const summary = useMemo(() => {
    return jobDetail?.structured.summary || "创建一份 JD 后，这里会展示结构化摘要。";
  }, [jobDetail]);

  return (
    <div className="stack-lg">
      {error ? <div className="form-error">{error}</div> : null}

      <div className="content-grid two-one">
        <SectionCard
          title="JD 工作台"
          subtitle="创建岗位描述，实时联调后端结构化解析、切片与向量化链路"
          action={submitting ? <Badge tone="info">处理中</Badge> : undefined}
        >
          <div className="form-grid">
            <label>
              <span>岗位名称</span>
              <input value={title} onChange={(event) => setTitle(event.target.value)} />
            </label>
            <label>
              <span>公司名称</span>
              <input value={company} onChange={(event) => setCompany(event.target.value)} />
            </label>
            <label>
              <span>工作地点</span>
              <input value={location} onChange={(event) => setLocation(event.target.value)} />
            </label>
            <label>
              <span>JD 原文</span>
              <textarea
                rows={14}
                value={rawText}
                onChange={(event) => setRawText(event.target.value)}
              />
            </label>
            <button className="primary-button" type="button" disabled={submitting} onClick={() => void handleCreate()}>
              {submitting ? "创建中..." : "创建并解析 JD"}
            </button>
          </div>
        </SectionCard>

        <SectionCard title="结构化预览" subtitle="展示后端返回的结构化字段，而不是本地 mock 数据">
          {jobDetail ? (
            <div className="stack-md">
              <div className="detail-hero">
                <div>
                  <h4>{jobDetail.title}</h4>
                  <p>{summary}</p>
                </div>
                <Badge tone={jobDetail.status === "COMPLETED" ? "success" : "warning"}>{jobDetail.status}</Badge>
              </div>

              <div>
                <h4 className="subsection-title">必备技能</h4>
                <div className="tag-list">
                  {jobDetail.structured.requiredSkills.length > 0 ? (
                    jobDetail.structured.requiredSkills.map((skill) => (
                      <span className="tag strong" key={skill}>
                        {skill}
                      </span>
                    ))
                  ) : (
                    <span className="table-meta">暂未识别到必备技能。</span>
                  )}
                </div>
              </div>

              <div>
                <h4 className="subsection-title">加分技能</h4>
                <div className="tag-list">
                  {jobDetail.structured.preferredSkills.length > 0 ? (
                    jobDetail.structured.preferredSkills.map((skill) => (
                      <span className="tag" key={skill}>
                        {skill}
                      </span>
                    ))
                  ) : (
                    <span className="table-meta">暂未识别到加分技能。</span>
                  )}
                </div>
              </div>

              <div>
                <h4 className="subsection-title">岗位职责</h4>
                {jobDetail.structured.responsibilities.length > 0 ? (
                  <ul className="feature-list">
                    {jobDetail.structured.responsibilities.map((item) => (
                      <li key={item}>{item}</li>
                    ))}
                  </ul>
                ) : (
                  <p className="table-meta">暂未提取到岗位职责。</p>
                )}
              </div>

              <div>
                <h4 className="subsection-title">任职要求</h4>
                {jobDetail.structured.requirements.length > 0 ? (
                  <ul className="feature-list">
                    {jobDetail.structured.requirements.map((item) => (
                      <li key={item}>{item}</li>
                    ))}
                  </ul>
                ) : (
                  <p className="table-meta">暂未提取到任职要求。</p>
                )}
              </div>

              <div>
                <h4 className="subsection-title">业务关键词</h4>
                <div className="tag-list">
                  {jobDetail.structured.businessKeywords.length > 0 ? (
                    jobDetail.structured.businessKeywords.map((keyword) => (
                      <span className="tag" key={keyword}>
                        {keyword}
                      </span>
                    ))
                  ) : (
                    <span className="table-meta">暂未识别到业务关键词。</span>
                  )}
                </div>
              </div>
            </div>
          ) : (
            <EmptyState title="请选择一份 JD" description="创建或选择左侧列表中的岗位描述，查看结构化结果。" />
          )}
        </SectionCard>
      </div>

      <div className="content-grid two-one">
        <SectionCard title="JD 列表" subtitle="当前登录用户已创建的岗位描述" action={loadingList ? <Badge tone="info">刷新中</Badge> : undefined}>
          {jobs.length === 0 ? (
            <EmptyState title="还没有 JD" description="先创建一份岗位描述，后端会自动做结构化解析和切片入库。" />
          ) : (
            <div className="table-list">
              {jobs.map((job) => (
                <button
                  key={job.jobId}
                  className={`table-row selectable ${selectedJobId === job.jobId ? "selected" : ""}`}
                  onClick={() => setSelectedJobId(job.jobId)}
                  type="button"
                >
                  <div>
                    <div className="table-title">{job.title}</div>
                    <div className="table-meta">
                      {[job.company, job.location].filter(Boolean).join(" · ") || "未填写公司/地点"}
                    </div>
                  </div>
                  <Badge tone={job.status === "COMPLETED" ? "success" : "warning"}>{job.status}</Badge>
                </button>
              ))}
            </div>
          )}
        </SectionCard>

        <SectionCard title="解析详情" subtitle="原始 JD、切片数量和最近一次创建结果">
          {jobDetail ? (
            <div className="stack-md">
              <div className="info-grid">
                <div className="mini-panel">
                  <strong>公司</strong>
                  <span>{jobDetail.company || "-"}</span>
                </div>
                <div className="mini-panel">
                  <strong>地点</strong>
                  <span>{jobDetail.location || "-"}</span>
                </div>
                <div className="mini-panel">
                  <strong>切片数</strong>
                  <span>{jobDetail.chunks.length}</span>
                </div>
                <div className="mini-panel">
                  <strong>创建时间</strong>
                  <span>{jobDetail.createTime}</span>
                </div>
              </div>

              <div>
                <h4 className="subsection-title">原始 JD</h4>
                <pre className="code-block">{jobDetail.rawText}</pre>
              </div>

              <div>
                <h4 className="subsection-title">Chunk 摘要</h4>
                <div className="stack-sm">
                  {jobDetail.chunks.slice(0, 4).map((chunk) => (
                    <div className="result-card" key={chunk.chunkId}>
                      <div className="result-topline">
                        <strong>Chunk #{chunk.chunkIndex}</strong>
                        <span className="table-meta">{chunk.embeddingDimensions} 维</span>
                      </div>
                      <p>{chunk.chunkText}</p>
                    </div>
                  ))}
                </div>
              </div>

              {lastCreated ? (
                <Badge tone="success">
                  最近创建成功: #{lastCreated.jobId}，共生成 {lastCreated.chunkCount} 个 chunk
                </Badge>
              ) : null}
            </div>
          ) : loadingDetail ? (
            <Badge tone="info">加载中</Badge>
          ) : (
            <EmptyState title="暂无详情" description="选中一份 JD 后，这里会展示原文和切片信息。" />
          )}
        </SectionCard>
      </div>
    </div>
  );
}
