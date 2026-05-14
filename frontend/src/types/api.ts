export type ApiResponse<T> = {
  success: boolean;
  code: number;
  message: string;
  data: T;
  timestamp: number;
  requestId: string;
};

export type AuthUser = {
  userId: number;
  username: string;
  nickname: string;
};

export type AuthTokenResponse = {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: AuthUser;
};

export type ResumeListItem = {
  resumeId: number;
  fileName: string;
  fileType: string;
  fileSize: number;
  status: string;
  createTime: string;
};

export type ResumeChunk = {
  chunkId?: number;
  chunkIndex: number;
  chunkText: string;
  metadataJson: string;
  embedding: number[];
  embeddingDimensions: number;
};

export type StructuredResume = {
  basics: {
    name?: string;
    email?: string;
    phone?: string;
    summary?: string;
  };
  skills: string[];
  education: Array<{
    school?: string;
    degree?: string;
    major?: string;
    period?: string;
  }>;
  workExperiences: Array<{
    company?: string;
    role?: string;
    period?: string;
    highlights: string[];
  }>;
  projects: Array<{
    name?: string;
    role?: string;
    period?: string;
    highlights: string[];
  }>;
  highlights: string[];
};

export type ResumeDetail = {
  resumeId: number;
  fileName: string;
  fileType: string;
  fileSize: number;
  status: string;
  storageUrl?: string;
  rawText?: string;
  structured: StructuredResume;
  parseError?: string | null;
  createTime: string;
  chunks: ResumeChunk[];
};

export type ResumeUploadResult = {
  resumeId: number;
  fileName: string;
  status: string;
  chunkCount: number;
};

export type SimilarChunk = {
  chunkId: number;
  resumeId: number;
  fileName: string;
  chunkIndex: number;
  chunkText: string;
  metadataJson: string;
  distance: number;
  similarity: number;
};

export type InfraComponentStatus = {
  available: boolean;
  detail: string;
};

export type InfraStatus = {
  database: InfraComponentStatus;
  redis: InfraComponentStatus;
  rabbitmq: InfraComponentStatus;
  minio: InfraComponentStatus;
};

export type JobListItem = {
  jobId: number;
  title: string;
  company?: string | null;
  location?: string | null;
  status: string;
  createTime: string;
};

export type JobChunk = {
  chunkId: number;
  chunkIndex: number;
  chunkText: string;
  metadataJson: string;
  embedding: number[];
  embeddingDimensions: number;
};

export type StructuredJob = {
  title?: string | null;
  company?: string | null;
  location?: string | null;
  summary?: string | null;
  requiredSkills: string[];
  preferredSkills: string[];
  responsibilities: string[];
  requirements: string[];
  businessKeywords: string[];
};

export type JobDetail = {
  jobId: number;
  title: string;
  company?: string | null;
  location?: string | null;
  status: string;
  rawText: string;
  structured: StructuredJob;
  parseError?: string | null;
  createTime: string;
  chunks: JobChunk[];
};

export type JobCreateResult = {
  jobId: number;
  title: string;
  status: string;
  chunkCount: number;
};

export type MatchTask = {
  taskId: number;
  resumeId: number;
  jobDescriptionId: number;
  taskType: string;
  status: string;
  currentNode: string;
  errorMessage?: string | null;
  startedAt?: string | null;
  finishedAt?: string | null;
  createTime: string;
};

export type MatchCreateResult = {
  taskId: number;
  resumeId: number;
  jobDescriptionId: number;
  taskType: string;
  status: string;
  currentNode: string;
  createTime: string;
};

export type MatchTimelineEvent = {
  eventId: number;
  nodeName: string;
  status: string;
  inputSummary?: string | null;
  outputSummary?: string | null;
  modelName?: string | null;
  promptTokens?: number | null;
  completionTokens?: number | null;
  durationMs?: number | null;
  retryCount: number;
  errorMessage?: string | null;
  metadataJson?: string | null;
  createTime: string;
};

export type MatchDimensionScore = {
  dimension: string;
  score: number;
  maxScore: number;
  confidence: number;
  reason: string;
};

export type MatchInterviewQuestion = {
  type: string;
  question: string;
  target: string;
  difficulty: string;
};

export type MatchReport = {
  taskId: number;
  resumeId: number;
  jobDescriptionId: number;
  candidateName: string;
  jobTitle: string;
  totalScore: number;
  confidence: number;
  matchLevel: string;
  recommendation: string;
  summary: string;
  dimensionScores: MatchDimensionScore[];
  strengths: string[];
  risks: string[];
  suggestions: string[];
  interviewQuestions: MatchInterviewQuestion[];
};
