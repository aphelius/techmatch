import { getAccessToken, logout } from "./session";
import type {
  ApiResponse,
  AuthTokenResponse,
  AuthUser,
  InfraStatus,
  MatchCreateResult,
  MatchReport,
  MatchTask,
  MatchTimelineEvent,
  JobCreateResult,
  JobDetail,
  JobListItem,
  ResumeDetail,
  ResumeListItem,
  ResumeUploadResult,
  SimilarChunk
} from "../types/api";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers ?? {});
  const token = getAccessToken();
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(path, {
    ...init,
    headers
  });

  const payload = (await response.json()) as ApiResponse<T>;

  if (response.status === 401) {
    logout();
  }

  if (!response.ok || !payload.success) {
    throw new Error(payload.message || "请求失败");
  }

  return payload.data;
}

export const api = {
  login(username: string, password: string) {
    return request<AuthTokenResponse>("/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ username, password })
    });
  },
  register(username: string, password: string, nickname: string) {
    return request<AuthTokenResponse>("/api/auth/register", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ username, password, nickname })
    });
  },
  currentUser() {
    return request<AuthUser>("/api/auth/me");
  },
  ping() {
    return request<{ status: string; service: string }>("/api/system/ping");
  },
  infra() {
    return request<InfraStatus>("/api/system/infra");
  },
  listResumes() {
    return request<ResumeListItem[]>("/api/resumes");
  },
  getResume(resumeId: number) {
    return request<ResumeDetail>(`/api/resumes/${resumeId}`);
  },
  listJobs() {
    return request<JobListItem[]>("/api/jobs");
  },
  getJob(jobId: number) {
    return request<JobDetail>(`/api/jobs/${jobId}`);
  },
  createJob(payload: {
    title: string;
    company: string;
    location: string;
    rawText: string;
  }) {
    return request<JobCreateResult>("/api/jobs", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    });
  },
  uploadResume(file: File) {
    const formData = new FormData();
    formData.append("file", file);
    return request<ResumeUploadResult>("/api/resumes/upload", {
      method: "POST",
      body: formData
    });
  },
  searchChunks(query: string, topK = 5) {
    const params = new URLSearchParams({
      query,
      topK: String(topK)
    });
    return request<SimilarChunk[]>(`/api/resumes/search/chunks?${params.toString()}`);
  },
  createMatch(payload: { resumeId: number; jobDescriptionId: number }) {
    return request<MatchCreateResult>("/api/matches", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    });
  },
  getMatchTask(taskId: number) {
    return request<MatchTask>(`/api/matches/${taskId}`);
  },
  getMatchTimeline(taskId: number) {
    return request<MatchTimelineEvent[]>(`/api/matches/${taskId}/timeline`);
  },
  getMatchReport(taskId: number) {
    return request<MatchReport>(`/api/matches/${taskId}/report`);
  }
};
