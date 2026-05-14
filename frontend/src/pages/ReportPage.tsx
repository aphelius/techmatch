import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Badge, EmptyState, SectionCard } from "../components/Ui";
import { api } from "../services/api";
import type { MatchReport, MatchTask } from "../types/api";

const dimensionLabelMap: Record<string, string> = {
  TECH_SKILL: "技术栈匹配",
  PROJECT_EXPERIENCE: "项目经历匹配",
  BUSINESS_DOMAIN: "业务领域匹配",
  EXPERIENCE_EDUCATION: "经验学历匹配",
  PREFERRED_SKILL: "加分项匹配"
};

function reportTone(matchLevel: string) {
  if (matchLevel.includes("强")) return "success" as const;
  if (matchLevel.includes("弱")) return "warning" as const;
  return "info" as const;
}

export function ReportPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const taskId = Number(searchParams.get("taskId") || 0);
  const [task, setTask] = useState<MatchTask | null>(null);
  const [report, setReport] = useState<MatchReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!taskId) {
      setLoading(false);
      return;
    }

    async function loadReport() {
      setLoading(true);
      try {
        const taskData = await api.getMatchTask(taskId);
        setTask(taskData);

        if (taskData.status === "COMPLETED") {
          const reportData = await api.getMatchReport(taskId);
          setReport(reportData);
        } else {
          setReport(null);
        }
      } catch (currentError) {
        setError(currentError instanceof Error ? currentError.message : "加载报告失败");
      } finally {
        setLoading(false);
      }
    }

    void loadReport();
  }, [taskId]);

  const reportSubtitle = useMemo(() => {
    if (!report) return "";
    return report.jobTitle;
  }, [report]);

  return (
    <div className="content-grid report-layout">
      <div className="stack-lg">
        {error ? <div className="form-error">{error}</div> : null}

        {!taskId ? (
          <SectionCard title="匹配报告" subtitle="请先创建匹配任务">
            <EmptyState title="缺少任务编号" description="请从匹配分析页创建任务，或在 URL 中带上 `taskId` 后再查看报告。" />
          </SectionCard>
        ) : loading ? (
          <SectionCard title="匹配报告" subtitle="正在加载后端真实报告">
            <Badge tone="info">加载中</Badge>
          </SectionCard>
        ) : !report ? (
          <SectionCard title="匹配报告" subtitle="任务尚未产出最终报告">
            <EmptyState
              title={task?.status === "FAILED" ? "任务执行失败" : "报告尚未生成"}
              description={
                task?.status === "FAILED"
                  ? task.errorMessage || "任务执行过程中出现错误，请查看时间线定位失败节点。"
                  : "当前任务还没完成，报告接口暂时没有返回结果。"
              }
            />
            {task ? (
              <div className="topbar-actions">
                <button className="secondary-button" type="button" onClick={() => navigate(`/timeline?taskId=${task.taskId}`)}>
                  查看时间线
                </button>
              </div>
            ) : null}
          </SectionCard>
        ) : (
          <>
            <SectionCard title={report.candidateName} subtitle={reportSubtitle}>
              <div className="report-hero">
                <div>
                  <div className="score-big">{Number(report.totalScore).toFixed(1)}</div>
                  <div className="table-meta">综合评分 / 100</div>
                </div>
                <div className="stack-sm align-end">
                  <Badge tone={reportTone(report.matchLevel)}>{report.matchLevel}</Badge>
                  <div className="mini-panel">
                    <strong>置信度</strong>
                    <span>{report.confidence}</span>
                  </div>
                  <div className="mini-panel">
                    <strong>建议结论</strong>
                    <span>{report.recommendation}</span>
                  </div>
                </div>
              </div>
            </SectionCard>

            <SectionCard title="维度拆解" subtitle="展示真实 Hybrid Scoring 结果中的五个评分维度">
              <div className="stack-md">
                {report.dimensionScores.map((item) => (
                  <div className="dimension-row" key={item.dimension}>
                    <div className="result-topline">
                      <strong>{dimensionLabelMap[item.dimension] || item.dimension}</strong>
                      <span>
                        {item.score} / {item.maxScore}
                      </span>
                    </div>
                    <div className="progress-track">
                      <div
                        className="progress-fill"
                        style={{ width: `${(Number(item.score) / Number(item.maxScore || 1)) * 100}%` }}
                      />
                    </div>
                    <div className="table-meta">{item.reason}</div>
                  </div>
                ))}
              </div>
            </SectionCard>

            <SectionCard title="已核验优势" subtitle="只展示经过 Evidence Verification 保留下来的结论">
              <ul className="feature-list">
                {report.strengths.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </SectionCard>

            <SectionCard title="风险与缺口" subtitle="需要在面试中重点确认的风险点与缺失证据">
              <ul className="feature-list">
                {report.risks.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </SectionCard>
          </>
        )}
      </div>

      <div className="stack-lg">
        <SectionCard title="报告摘要" subtitle="当前任务的关键结论快照">
          {report ? (
            <div className="stack-md">
              <div className="mini-panel">
                <strong>总分</strong>
                <span>{Number(report.totalScore).toFixed(1)}/100</span>
              </div>
              <div className="mini-panel">
                <strong>匹配等级</strong>
                <span>{report.matchLevel}</span>
              </div>
              <div className="mini-panel">
                <strong>置信度</strong>
                <span>{report.confidence}</span>
              </div>
              <div className="mini-panel">
                <strong>摘要</strong>
                <span>{report.summary}</span>
              </div>
              <button className="secondary-button" type="button" onClick={() => navigate(`/timeline?taskId=${taskId}`)}>
                查看 Agent 时间线
              </button>
            </div>
          ) : (
            <EmptyState title="暂无摘要" description="任务完成后，这里会显示真实报告的总分、等级与摘要。" />
          )}
        </SectionCard>

        <SectionCard title="优化建议" subtitle="面向候选人简历完善与面试准备的提示">
          {report ? (
            <ul className="feature-list">
              {report.suggestions.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          ) : (
            <EmptyState title="暂无建议" description="完成评分和证据核验后，这里会展示后端生成的建议项。" />
          )}
        </SectionCard>

        <SectionCard title="面试验证问题" subtitle="由缺失证据和风险点自动转化而来">
          {report && report.interviewQuestions.length > 0 ? (
            <ul className="feature-list">
              {report.interviewQuestions.map((item) => (
                <li key={`${item.type}-${item.target}-${item.question}`}>
                  [{item.type}] {item.question}
                </li>
              ))}
            </ul>
          ) : (
            <EmptyState title="暂无面试问题" description="后端完成证据核验后，这里会显示真实的验证问题。" />
          )}
        </SectionCard>
      </div>
    </div>
  );
}
