import { Badge, SectionCard } from "../components/Ui";
import { mockEvidenceChains } from "../services/mockData";

export function EvidenceGraphPage() {
  return (
    <SectionCard title="证据图谱" subtitle="用卡片链路展示 JD 要求、简历证据与面试提示">
      <div className="stack-lg">
        {mockEvidenceChains.map((chain) => (
          <div className="evidence-chain" key={chain.requirement}>
            <div className="evidence-node requirement">
              <small>JD 要求</small>
              <strong>{chain.requirement}</strong>
            </div>
            <div className="evidence-arrow">→</div>
            <div className="evidence-node evidence">
              <small>简历证据</small>
              <strong>{chain.evidence}</strong>
            </div>
            <div className="evidence-arrow">→</div>
            <div className="evidence-node risk">
              <div className="result-topline">
                <small>风险</small>
                <Badge tone="warning">{chain.status}</Badge>
              </div>
              <strong>{chain.risk}</strong>
              <p>{chain.question}</p>
            </div>
          </div>
        ))}
      </div>
    </SectionCard>
  );
}
