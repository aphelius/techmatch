import { Badge, SectionCard } from "../components/Ui";
import { mockFeedbackQuestions } from "../services/mockData";

export function FeedbackPage() {
  return (
    <div className="content-grid two-one">
      <SectionCard title="面试问题" subtitle="由分析层生成的结构化追问">
        <div className="stack-md">
          {mockFeedbackQuestions.map((question) => (
            <div className="result-card" key={question.id}>
              <div className="result-topline">
                <strong>{question.type}</strong>
                <Badge tone={question.difficulty === "困难" ? "warning" : "info"}>{question.difficulty}</Badge>
              </div>
              <p>{question.text}</p>
              <div className="table-meta">
                {question.requirement} · 风险：{question.risk}
              </div>
            </div>
          ))}
        </div>
      </SectionCard>

      <SectionCard title="反馈录入" subtitle="在反馈接口完成前，先使用 mock 提交流程">
        <div className="form-grid">
          <label>
            <span>评分</span>
            <select defaultValue="4">
              <option value="5">5 - 很强</option>
              <option value="4">4 - 良好</option>
              <option value="3">3 - 一般</option>
              <option value="2">2 - 偏弱</option>
              <option value="1">1 - 未验证</option>
            </select>
          </label>
          <label>
            <span>反馈类型</span>
            <select defaultValue="证据充分">
              <option>证据充分</option>
              <option>技能弱于预期</option>
              <option>需要继续追问</option>
              <option>尚未验证</option>
            </select>
          </label>
          <label>
            <span>备注</span>
            <textarea rows={10} defaultValue="候选人对 Redis 一致性解释较清楚，但 Kubernetes 深度仍不够明确。" />
          </label>
          <button className="primary-button">提交反馈</button>
        </div>
      </SectionCard>
    </div>
  );
}
