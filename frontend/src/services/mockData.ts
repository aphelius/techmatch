export const mockJobs = [
  {
    id: "jd-1",
    title: "高级后端工程师",
    company: "Nebula Systems",
    location: "上海 / 混合办公",
    requiredSkills: ["Java", "Spring Boot", "Redis", "PostgreSQL", "RabbitMQ"],
    preferredSkills: ["Docker", "Kubernetes", "可观测性"],
    responsibilities: [
      "为招聘智能工作流设计稳定可靠的后端服务",
      "优化 AI 辅助匹配中的检索与评分链路",
      "与产品和招聘团队协作，推进基于证据的评估流程"
    ],
    requirements: [
      "5 年以上后端工程经验",
      "具备扎实的分布式系统与缓存实战经验",
      "能够胜任数据密集型企业应用场景"
    ]
  },
  {
    id: "jd-2",
    title: "平台 AI 工程师",
    company: "Northstar Talent AI",
    location: "远程",
    requiredSkills: ["Python", "LLM", "Embeddings", "Vector Search"],
    preferredSkills: ["RAG", "评测", "Agent 编排"],
    responsibilities: [
      "构建用于分析与校验的内部 AI Agent",
      "通过护栏和证据校验机制提升模型输出质量"
    ],
    requirements: [
      "具备生产级 ML 或 LLM 系统经验",
      "能向产品和业务方清晰解释质量与效果的权衡"
    ]
  }
];

export const mockReports = [
  {
    id: "report-1",
    candidate: "Alex Chen",
    job: "高级后端工程师",
    company: "Nebula Systems",
    score: 82,
    confidence: 0.78,
    matchLevel: "强匹配",
    recommendation: "建议进入面试",
    dimensions: [
      ["技术栈匹配", 31, 35],
      ["项目经验匹配", 24, 30],
      ["业务领域匹配", 11, 15],
      ["经验与学历", 8, 10],
      ["加分项匹配", 8, 10]
    ],
    strengths: [
      "在 Spring Boot 和 Redis 相关项目中体现出较强的实战经验，并带有运维上下文。",
      "展示了在高并发场景下可量化的后端优化成果。",
      "简历中体现出架构负责人与系统所有权信号，而不仅是功能交付。"
    ],
    risks: [
      "Kubernetes 相关证据主要停留在部署接触层面，缺少集群运维信号。",
      "未直接说明分布式锁与缓存一致性方案。",
      "电商业务关键词较强，但招聘工具链相关业务经验偏弱。"
    ],
    suggestions: [
      "补充吞吐、延迟等量化指标，提升证据强度与可信度。",
      "进一步说明重试、幂等、回滚等稳定性设计经历。",
      "如果岗位涉及带人或 owner 职责，建议突出领导力相关内容。"
    ]
  }
];

export const mockTimeline = [
  {
    name: "ResumeParserNode",
    status: "SUCCESS",
    duration: "450ms",
    tokens: 612,
    retries: 0,
    input: "候选人简历 PDF",
    output: "已提取结构化简历、技能标签与亮点信息"
  },
  {
    name: "RetrievalNode",
    status: "SUCCESS",
    duration: "820ms",
    tokens: 224,
    retries: 0,
    input: "简历切片 + JD 查询",
    output: "已检索到最相关的候选人证据"
  },
  {
    name: "HybridScoringNode",
    status: "SUCCESS",
    duration: "1105ms",
    tokens: 355,
    retries: 0,
    input: "结构化简历与评分规则",
    output: "已生成维度分数与初始置信度"
  },
  {
    name: "EvidenceVerificationNode",
    status: "WARNING",
    duration: "760ms",
    tokens: 412,
    retries: 1,
    input: "初始结论与证据图谱",
    output: "拒绝 2 条缺少证据的结论，新增 3 条面试追问"
  },
  {
    name: "FinalReportNode",
    status: "SUCCESS",
    duration: "530ms",
    tokens: 288,
    retries: 0,
    input: "已验证的分析结果",
    output: "最终报告已生成并持久化"
  }
];

export const mockEvidenceChains = [
  {
    requirement: "具备较强的 Redis 缓存与高并发优化经验",
    evidence: "候选人在秒杀项目中描述了 Redis 预扣库存与基于队列的订单保护方案。",
    status: "部分匹配",
    risk: "尚未明确说明分布式锁与缓存一致性细节。",
    question: "在突发流量下，你是如何保证 Redis 库存与数据库库存最终一致的？"
  },
  {
    requirement: "能够在生产环境中运维容器化服务",
    evidence: "候选人提到 Docker 化部署与 CI 交接经验。",
    status: "弱匹配",
    risk: "缺少 Kubernetes 发布、扩缩容或可观测性 owner 的直接证据。",
    question: "如果一次发布后 Kubernetes 部署失败，你会如何排查？"
  }
];

export const mockFeedbackQuestions = [
  {
    id: "q-1",
    type: "风险验证题",
    difficulty: "中等",
    text: "当 Redis 先预扣库存、数据库稍后持久化时，你如何避免重复扣减？",
    risk: "库存一致性",
    requirement: "高并发优化"
  },
  {
    id: "q-2",
    type: "项目深挖题",
    difficulty: "困难",
    text: "在故障切换场景下，什么样的证据能够证明一个缓存策略是安全的？",
    risk: "工程成熟度",
    requirement: "分布式系统可靠性"
  }
];
