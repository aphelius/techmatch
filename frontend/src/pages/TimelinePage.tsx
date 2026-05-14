import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Badge, EmptyState, SectionCard } from "../components/Ui";
import { api } from "../services/api";
import type { MatchTask, MatchTimelineEvent } from "../types/api";
import { formatDate } from "../utils/format";

function toneByStatus(status: string) {
  switch (status) {
    case "SUCCESS":
    case "COMPLETED":
    case "VERIFIED":
      return "success" as const;
    case "FAILED":
    case "REJECTED":
      return "danger" as const;
    case "PARTIAL":
      return "warning" as const;
    default:
      return "info" as const;
  }
}

export function TimelinePage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const taskId = Number(searchParams.get("taskId") || 0);
  const [task, setTask] = useState<MatchTask | null>(null);
  const [timeline, setTimeline] = useState<MatchTimelineEvent[]>([]);
  const [selectedEventId, setSelectedEventId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!taskId) {
      setLoading(false);
      return;
    }

    async function loadData() {
      setLoading(true);
      try {
        const [taskData, timelineData] = await Promise.all([api.getMatchTask(taskId), api.getMatchTimeline(taskId)]);
        setTask(taskData);
        setTimeline(timelineData);
        setSelectedEventId(timelineData[0]?.eventId ?? null);
      } catch (currentError) {
        setError(currentError instanceof Error ? currentError.message : "加载时间线失败");
      } finally {
        setLoading(false);
      }
    }

    void loadData();
  }, [taskId]);

  const selectedEvent = useMemo(
    () => timeline.find((item) => item.eventId === selectedEventId) ?? timeline[0] ?? null,
    [timeline, selectedEventId]
  );

  return (
    <div className="content-grid two-one">
      <SectionCard title="执行步骤" subtitle="展示当前任务的真实 Agent 执行链路">
        {error ? <div className="form-error">{error}</div> : null}
        {!taskId ? (
          <EmptyState title="缺少任务编号" description="请从匹配分析页创建任务，或在 URL 中带上 `taskId` 后再查看时间线。" />
        ) : loading ? (
          <Badge tone="info">加载中</Badge>
        ) : timeline.length === 0 ? (
          <EmptyState title="还没有时间线记录" description="这个任务暂时还没有执行事件，稍后刷新再看。" />
        ) : (
          <div className="stack-md">
            {task ? (
              <div className="status-row">
                <div>
                  <div className="table-title">任务 #{task.taskId}</div>
                  <div className="table-meta">
                    {task.status} · 当前节点 {task.currentNode || "-"} · 创建于 {formatDate(task.createTime)}
                  </div>
                </div>
                <button
                  className="secondary-button"
                  type="button"
                  onClick={() => navigate(`/reports?taskId=${task.taskId}`)}
                >
                  查看报告
                </button>
              </div>
            ) : null}

            <div className="timeline-list">
              {timeline.map((step, index) => (
                <button
                  className={`timeline-item ${selectedEvent?.eventId === step.eventId ? "active" : ""}`}
                  key={step.eventId}
                  onClick={() => setSelectedEventId(step.eventId)}
                  type="button"
                >
                  <div className="timeline-marker">{index + 1}</div>
                  <div className="timeline-body">
                    <div className="result-topline">
                      <strong>{step.nodeName}</strong>
                      <Badge tone={toneByStatus(step.status)}>{step.status}</Badge>
                    </div>
                    <div className="table-meta">
                      {step.durationMs ?? 0} ms · 重试 {step.retryCount} 次 · {formatDate(step.createTime)}
                    </div>
                  </div>
                </button>
              ))}
            </div>
          </div>
        )}
      </SectionCard>

      <SectionCard title="节点详情" subtitle="当前选中节点的输入、输出和执行元数据">
        {selectedEvent ? (
          <div className="stack-md">
            <div className="mini-panel">
              <strong>{selectedEvent.nodeName}</strong>
              <span>{selectedEvent.durationMs ?? 0} ms</span>
            </div>
            <div className="mini-panel">
              <strong>输入摘要</strong>
              <span>{selectedEvent.inputSummary || "暂无"}</span>
            </div>
            <div className="mini-panel">
              <strong>输出摘要</strong>
              <span>{selectedEvent.outputSummary || "暂无"}</span>
            </div>
            <div className="mini-panel">
              <strong>执行信息</strong>
              <span>
                状态 {selectedEvent.status} · Prompt Tokens {selectedEvent.promptTokens ?? 0} · Completion Tokens{" "}
                {selectedEvent.completionTokens ?? 0}
              </span>
            </div>
            <div className="mini-panel">
              <strong>元数据</strong>
              <span>{selectedEvent.metadataJson || selectedEvent.errorMessage || "暂无"}</span>
            </div>
          </div>
        ) : (
          <EmptyState title="暂无节点详情" description="选择左侧某个节点后，这里会展示执行摘要和元数据。" />
        )}
      </SectionCard>
    </div>
  );
}
