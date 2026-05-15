import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Badge, EmptyState, SectionCard } from "../components/Ui";
import { api } from "../services/api";
import type { InterviewQuestionItem, MatchReport, MatchTask } from "../types/api";

type DraftState = Record<
  number,
  {
    score: string;
    feedbackType: string;
    notes: string;
  }
>;

const typeLabelMap: Record<string, string> = {
  TECH_BASIC: "技术基础",
  PROJECT_DEEP_DIVE: "项目深挖",
  SYSTEM_DESIGN: "系统设计",
  RISK_VERIFICATION: "风险核验"
};

export function FeedbackPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const taskId = Number(searchParams.get("taskId") || 0);
  const [task, setTask] = useState<MatchTask | null>(null);
  const [report, setReport] = useState<MatchReport | null>(null);
  const [questions, setQuestions] = useState<InterviewQuestionItem[]>([]);
  const [drafts, setDrafts] = useState<DraftState>({});
  const [loading, setLoading] = useState(true);
  const [submittingId, setSubmittingId] = useState<number | null>(null);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    if (!taskId) {
      setLoading(false);
      return;
    }

    async function loadData() {
      setLoading(true);
      setError("");
      try {
        const [taskData, reportData, questionData] = await Promise.all([
          api.getMatchTask(taskId),
          api.getMatchReport(taskId),
          api.getMatchQuestions(taskId)
        ]);
        setTask(taskData);
        setReport(reportData);
        setQuestions(questionData);
        setDrafts(
          questionData.reduce<DraftState>((accumulator, item) => {
            accumulator[item.questionId] = {
              score: String(item.feedback?.score ?? 4),
              feedbackType: item.feedback?.feedbackType ?? "证据充分",
              notes: item.feedback?.notes ?? ""
            };
            return accumulator;
          }, {})
        );
      } catch (currentError) {
        setError(currentError instanceof Error ? currentError.message : "加载面试反馈页面失败");
      } finally {
        setLoading(false);
      }
    }

    void loadData();
  }, [taskId]);

  function updateDraft(questionId: number, patch: Partial<DraftState[number]>) {
    setDrafts((current) => ({
      ...current,
      [questionId]: {
        score: current[questionId]?.score ?? "4",
        feedbackType: current[questionId]?.feedbackType ?? "证据充分",
        notes: current[questionId]?.notes ?? "",
        ...patch
      }
    }));
  }

  async function handleSubmit(question: InterviewQuestionItem) {
    const draft = drafts[question.questionId];
    if (!draft) return;

    setSubmittingId(question.questionId);
    setError("");
    setSuccessMessage("");
    try {
      await api.submitInterviewFeedback(taskId, {
        questionId: question.questionId,
        score: Number(draft.score),
        feedbackType: draft.feedbackType,
        notes: draft.notes
      });

      const [reportData, questionData] = await Promise.all([
        api.getMatchReport(taskId),
        api.getMatchQuestions(taskId)
      ]);
      setReport(reportData);
      setQuestions(questionData);
      setSuccessMessage(`问题 #${question.questionId} 的反馈已提交，报告置信度和匹配等级已更新。`);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : "提交反馈失败");
    } finally {
      setSubmittingId(null);
    }
  }

  return (
    <div className="content-grid two-one">
      <div className="stack-lg">
        {error ? <div className="form-error">{error}</div> : null}
        {successMessage ? <div className="form-success">{successMessage}</div> : null}

        {!taskId ? (
          <SectionCard title="面试反馈" subtitle="请先从匹配报告或图谱进入具体任务">
            <EmptyState title="缺少任务编号" description="请在 URL 中带上 taskId，或从匹配报告页进入面试反馈。" />
          </SectionCard>
        ) : loading ? (
          <SectionCard title="面试反馈" subtitle="正在加载真实题目与反馈状态">
            <Badge tone="info">加载中</Badge>
          </SectionCard>
        ) : questions.length === 0 ? (
          <SectionCard title="面试反馈" subtitle="当前任务还没有生成可核验的问题">
            <EmptyState title="暂无面试问题" description="请先完成匹配任务，系统生成问题后这里会展示真实数据。" />
          </SectionCard>
        ) : (
          questions.map((question) => {
            const draft = drafts[question.questionId] ?? {
              score: "4",
              feedbackType: "证据充分",
              notes: ""
            };

            return (
              <SectionCard
                key={question.questionId}
                title={`${typeLabelMap[question.type] || question.type} · ${question.target}`}
                subtitle={question.sourceRisk || "系统自动生成的结构化追问"}
              >
                <div className="stack-md">
                  <div className="result-topline">
                    <strong>{question.question}</strong>
                    <Badge tone={question.feedback ? "success" : "warning"}>
                      {question.feedback ? "已反馈" : "待反馈"}
                    </Badge>
                  </div>

                  <div className="table-meta">
                    难度：{question.difficulty}
                    {question.feedback ? ` · 最近评分：${question.feedback.score}/5` : ""}
                  </div>

                  <div className="form-grid">
                    <label>
                      <span>评分</span>
                      <select value={draft.score} onChange={(event) => updateDraft(question.questionId, { score: event.target.value })}>
                        <option value="5">5 - 非常强</option>
                        <option value="4">4 - 比较强</option>
                        <option value="3">3 - 基本符合</option>
                        <option value="2">2 - 表现偏弱</option>
                        <option value="1">1 - 未验证 / 很弱</option>
                      </select>
                    </label>

                    <label>
                      <span>反馈类型</span>
                      <select
                        value={draft.feedbackType}
                        onChange={(event) => updateDraft(question.questionId, { feedbackType: event.target.value })}
                      >
                        <option value="证据充分">证据充分</option>
                        <option value="技术扎实">技术扎实</option>
                        <option value="需要追问">需要追问</option>
                        <option value="表现偏弱">表现偏弱</option>
                        <option value="未验证">未验证</option>
                      </select>
                    </label>

                    <label>
                      <span>备注</span>
                      <textarea
                        rows={5}
                        value={draft.notes}
                        onChange={(event) => updateDraft(question.questionId, { notes: event.target.value })}
                        placeholder="记录候选人的回答亮点、漏洞、例子质量，或后续需要继续追问的点。"
                      />
                    </label>
                  </div>

                  <div className="topbar-actions">
                    <button
                      className="primary-button"
                      type="button"
                      disabled={submittingId === question.questionId}
                      onClick={() => void handleSubmit(question)}
                    >
                      {submittingId === question.questionId ? "提交中..." : "提交反馈"}
                    </button>
                  </div>
                </div>
              </SectionCard>
            );
          })
        )}
      </div>

      <div className="stack-lg">
        <SectionCard title="反馈回流结果" subtitle="反馈会回写到报告的置信度、匹配等级和建议">
          {report ? (
            <div className="stack-md">
              <div className="mini-panel">
                <strong>当前匹配等级</strong>
                <span>{report.matchLevel}</span>
              </div>
              <div className="mini-panel">
                <strong>当前置信度</strong>
                <span>{report.confidence}</span>
              </div>
              <div className="mini-panel">
                <strong>当前建议</strong>
                <span>{report.recommendation}</span>
              </div>
              {report.feedbackSummary ? (
                <>
                  <div className="mini-panel">
                    <strong>已提交反馈</strong>
                    <span>{report.feedbackSummary.feedbackCount} 条</span>
                  </div>
                  <div className="mini-panel">
                    <strong>平均评分</strong>
                    <span>{report.feedbackSummary.averageScore}</span>
                  </div>
                  <div className="mini-panel">
                    <strong>置信度修正</strong>
                    <span>{report.feedbackSummary.confidenceDelta}</span>
                  </div>
                  <div className="mini-panel">
                    <strong>系统说明</strong>
                    <span>{report.feedbackSummary.note}</span>
                  </div>
                </>
              ) : (
                <EmptyState title="暂未提交反馈" description="提交任意一条面试反馈后，这里会展示回流后的匹配结论变化。" />
              )}
            </div>
          ) : (
            <EmptyState title="暂无报告" description="匹配报告生成后，这里会显示反馈回流的结果。" />
          )}
        </SectionCard>

        <SectionCard
          title="快捷跳转"
          subtitle="方便在报告、图谱和时间线之间来回查看"
          action={
            task ? (
              <button className="secondary-button" type="button" onClick={() => navigate(`/timeline?taskId=${task.taskId}`)}>
                查看时间线
              </button>
            ) : undefined
          }
        >
          <div className="stack-sm">
            <button className="secondary-button" type="button" onClick={() => navigate(`/reports?taskId=${taskId}`)}>
              返回匹配报告
            </button>
            <button className="secondary-button" type="button" onClick={() => navigate(`/evidence-graph?taskId=${taskId}`)}>
              查看证据图谱
            </button>
          </div>
        </SectionCard>
      </div>
    </div>
  );
}
