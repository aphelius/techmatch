import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Badge, EmptyState, SectionCard } from "../components/Ui";
import { api } from "../services/api";
import type { JobListItem, MatchCreateResult, ResumeListItem } from "../types/api";
import { formatDate } from "../utils/format";

function statusTone(status?: string) {
  switch (status) {
    case "COMPLETED":
      return "success" as const;
    case "FAILED":
      return "danger" as const;
    case "RUNNING":
      return "info" as const;
    default:
      return "warning" as const;
  }
}

export function MatchAnalysisPage() {
  const navigate = useNavigate();
  const [resumes, setResumes] = useState<ResumeListItem[]>([]);
  const [jobs, setJobs] = useState<JobListItem[]>([]);
  const [selectedResumeId, setSelectedResumeId] = useState<number | null>(null);
  const [selectedJobId, setSelectedJobId] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [lastTask, setLastTask] = useState<MatchCreateResult | null>(null);

  useEffect(() => {
    async function loadOptions() {
      setLoading(true);
      try {
        const [resumeList, jobList] = await Promise.all([api.listResumes(), api.listJobs()]);
        setResumes(resumeList);
        setJobs(jobList);
        setSelectedResumeId((current) => current ?? resumeList[0]?.resumeId ?? null);
        setSelectedJobId((current) => current ?? jobList[0]?.jobId ?? null);
      } catch (currentError) {
        setError(currentError instanceof Error ? currentError.message : "加载匹配分析选项失败");
      } finally {
        setLoading(false);
      }
    }

    void loadOptions();
  }, []);

  const selectedResume = useMemo(
    () => resumes.find((item) => item.resumeId === selectedResumeId) ?? null,
    [resumes, selectedResumeId]
  );
  const selectedJob = useMemo(
    () => jobs.find((item) => item.jobId === selectedJobId) ?? null,
    [jobs, selectedJobId]
  );

  async function handleCreateMatch() {
    if (!selectedResumeId || !selectedJobId) {
      setError("请先选择一份简历和一个岗位");
      return;
    }

    setSubmitting(true);
    setError("");
    try {
      const task = await api.createMatch({
        resumeId: selectedResumeId,
        jobDescriptionId: selectedJobId
      });
      setLastTask(task);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : "创建匹配任务失败");
    } finally {
      setSubmitting(false);
    }
  }

  const progressWidth = (() => {
    switch (lastTask?.status) {
      case "COMPLETED":
        return "100%";
      case "FAILED":
        return "100%";
      case "RUNNING":
        return "68%";
      default:
        return "16%";
    }
  })();

  return (
    <div className="stack-lg">
      {error ? <div className="form-error">{error}</div> : null}

      <SectionCard
        title="创建匹配分析"
        subtitle="选择真实简历和岗位描述，后端会立即执行完整 Agent 工作流"
        action={submitting ? <Badge tone="info">分析中</Badge> : undefined}
      >
        <div className="wizard-grid">
          <div className="wizard-step">
            <div className="wizard-index">1</div>
            <div className="stack-sm">
              <h4>选择简历</h4>
              <p>从当前账号的简历库中选择一份已经完成解析的候选人资料。</p>
              <select value={selectedResumeId ?? ""} onChange={(event) => setSelectedResumeId(Number(event.target.value))}>
                {resumes.length === 0 ? <option value="">暂无可用简历</option> : null}
                {resumes.map((resume) => (
                  <option key={resume.resumeId} value={resume.resumeId}>
                    {resume.fileName}
                  </option>
                ))}
              </select>
              {selectedResume ? (
                <div className="mini-card">
                  {selectedResume.fileName} · {selectedResume.status} · {formatDate(selectedResume.createTime)}
                </div>
              ) : null}
            </div>
          </div>

          <div className="wizard-step">
            <div className="wizard-index">2</div>
            <div className="stack-sm">
              <h4>选择岗位</h4>
              <p>把候选人资料绑定到目标岗位描述，作为检索与评分的基准。</p>
              <select value={selectedJobId ?? ""} onChange={(event) => setSelectedJobId(Number(event.target.value))}>
                {jobs.length === 0 ? <option value="">暂无可用岗位</option> : null}
                {jobs.map((job) => (
                  <option key={job.jobId} value={job.jobId}>
                    {job.title}
                  </option>
                ))}
              </select>
              {selectedJob ? (
                <div className="mini-card">
                  {[selectedJob.title, selectedJob.company].filter(Boolean).join(" · ")} · {selectedJob.status}
                </div>
              ) : null}
            </div>
          </div>

          <div className="wizard-step">
            <div className="wizard-index">3</div>
            <div className="stack-sm">
              <h4>启动匹配分析</h4>
              <p>后端会依次执行加载、质量检查、检索、评分、证据核验、问题生成和最终报告。</p>
              <button
                className="primary-button"
                type="button"
                disabled={loading || submitting || !selectedResumeId || !selectedJobId}
                onClick={() => void handleCreateMatch()}
              >
                {submitting ? "正在执行 Agent 工作流..." : "开始 AI 匹配分析"}
              </button>
            </div>
          </div>
        </div>
      </SectionCard>

      <SectionCard
        title="实时任务状态"
        subtitle="这里显示当前最新创建的匹配任务状态，供你快速跳转到时间线和报告页"
      >
        {lastTask ? (
          <div className="stack-md">
            <div className="progress-track">
              <div className="progress-fill" style={{ width: progressWidth }} />
            </div>

            <div className="status-row">
              <div>
                <div className="table-title">当前节点</div>
                <div className="table-meta">{lastTask.currentNode || "QUEUED"}</div>
              </div>
              <Badge tone={statusTone(lastTask.status)}>{lastTask.status}</Badge>
            </div>

            <div className="info-grid">
              <div className="mini-panel">
                <strong>任务编号</strong>
                <span>#{lastTask.taskId}</span>
              </div>
              <div className="mini-panel">
                <strong>简历 ID</strong>
                <span>{lastTask.resumeId}</span>
              </div>
              <div className="mini-panel">
                <strong>岗位 ID</strong>
                <span>{lastTask.jobDescriptionId}</span>
              </div>
              <div className="mini-panel">
                <strong>任务类型</strong>
                <span>{lastTask.taskType}</span>
              </div>
            </div>

            <div className="topbar-actions">
              <button
                className="secondary-button"
                type="button"
                onClick={() => navigate(`/timeline?taskId=${lastTask.taskId}`)}
              >
                查看 Agent 时间线
              </button>
              <button
                className="primary-button"
                type="button"
                onClick={() => navigate(`/reports?taskId=${lastTask.taskId}`)}
              >
                查看匹配报告
              </button>
            </div>
          </div>
        ) : (
          <EmptyState
            title="还没有匹配任务"
            description="先选择一份简历和一个岗位，创建后端真实匹配任务，这里就会显示任务状态。"
          />
        )}
      </SectionCard>
    </div>
  );
}
