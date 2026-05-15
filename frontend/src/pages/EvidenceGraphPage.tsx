import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Badge, EmptyState, SectionCard } from "../components/Ui";
import { api } from "../services/api";
import type { MatchEvidenceGraph, MatchTask } from "../types/api";

function chainTone(status: string) {
  if (status.includes("强") || status.includes("MATCHED")) return "success" as const;
  if (status.includes("部分")) return "warning" as const;
  if (status.includes("缺失")) return "danger" as const;
  return "info" as const;
}

export function EvidenceGraphPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const taskId = Number(searchParams.get("taskId") || 0);
  const [task, setTask] = useState<MatchTask | null>(null);
  const [graph, setGraph] = useState<MatchEvidenceGraph | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!taskId) {
      setLoading(false);
      return;
    }

    async function loadGraph() {
      setLoading(true);
      try {
        const [taskData, graphData] = await Promise.all([api.getMatchTask(taskId), api.getMatchGraph(taskId)]);
        setTask(taskData);
        setGraph(graphData);
      } catch (currentError) {
        setError(currentError instanceof Error ? currentError.message : "加载证据图谱失败");
      } finally {
        setLoading(false);
      }
    }

    void loadGraph();
  }, [taskId]);

  return (
    <SectionCard
      title="证据图谱"
      subtitle="用真实图谱链路展示岗位要求、简历证据、风险点与面试验证问题"
      action={
        task ? (
          <button className="secondary-button" type="button" onClick={() => navigate(`/reports?taskId=${task.taskId}`)}>
            查看报告
          </button>
        ) : undefined
      }
    >
      <div className="stack-lg">
        {error ? <div className="form-error">{error}</div> : null}

        {!taskId ? (
          <EmptyState title="缺少任务编号" description="请从匹配分析页创建任务，或在 URL 中带上 `taskId` 后再查看证据图谱。" />
        ) : loading ? (
          <Badge tone="info">加载中</Badge>
        ) : !graph || graph.chains.length === 0 ? (
          <EmptyState title="暂无图谱数据" description="当前任务还没有生成证据图谱，可能任务尚未执行完成。" />
        ) : (
          <>
            <div className="status-row">
              <div>
                <div className="table-title">任务 #{graph.taskId}</div>
                <div className="table-meta">
                  节点 {graph.nodes.length} 个 · 边 {graph.edges.length} 条 · 当前状态 {task?.status || "-"}
                </div>
              </div>
              <button className="secondary-button" type="button" onClick={() => navigate(`/timeline?taskId=${graph.taskId}`)}>
                查看时间线
              </button>
            </div>

            {graph.chains.map((chain) => (
              <div className="evidence-chain" key={`${chain.requirement}-${chain.question || chain.risk || chain.status}`}>
                <div className="evidence-node requirement">
                  <small>JD 要求</small>
                  <strong>{chain.requirement}</strong>
                </div>
                <div className="evidence-arrow">→</div>
                <div className="evidence-node evidence">
                  <small>简历证据</small>
                  <strong>{chain.evidence || "暂无直接证据片段"}</strong>
                </div>
                <div className="evidence-arrow">→</div>
                <div className="evidence-node risk">
                  <div className="result-topline">
                    <small>风险 / 状态</small>
                    <Badge tone={chainTone(chain.status)}>{chain.status}</Badge>
                  </div>
                  <strong>{chain.risk || "当前没有额外风险提示"}</strong>
                  <p>{chain.question || "当前没有关联的面试验证问题"}</p>
                </div>
              </div>
            ))}
          </>
        )}
      </div>
    </SectionCard>
  );
}
